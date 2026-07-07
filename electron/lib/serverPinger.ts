import net from 'net';

export interface PingResult {
  online: boolean;
  versionName: string;
  playersOnline: number;
  playersMax: number;
  motd: string;
  error: string;
}

function writeVarInt(chunks: number[], value: number): void {
  // eslint-disable-next-line no-constant-condition
  while (true) {
    if ((value & ~0x7f) === 0) {
      chunks.push(value);
      return;
    }
    chunks.push((value & 0x7f) | 0x80);
    value >>>= 7;
  }
}

function encodeString(s: string): Buffer {
  const strBuf = Buffer.from(s, 'utf-8');
  const lenChunks: number[] = [];
  writeVarInt(lenChunks, strBuf.length);
  return Buffer.concat([Buffer.from(lenChunks), strBuf]);
}

function buildPacket(body: Buffer): Buffer {
  const lenChunks: number[] = [];
  writeVarInt(lenChunks, body.length);
  return Buffer.concat([Buffer.from(lenChunks), body]);
}

function extractMotd(desc: any): string {
  if (typeof desc === 'string') return desc;
  if (desc && typeof desc === 'object') return desc.text != null ? String(desc.text) : '';
  return '';
}

/** VarInt/string reader over a growing buffer, used while consuming the raw status response. */
class PacketReader {
  private buf = Buffer.alloc(0);
  private pos = 0;
  private waiters: Array<() => void> = [];

  feed(chunk: Buffer) {
    this.buf = Buffer.concat([this.buf, chunk]);
    const w = this.waiters;
    this.waiters = [];
    w.forEach((fn) => fn());
  }

  private async ensure(n: number): Promise<void> {
    while (this.buf.length - this.pos < n) {
      await new Promise<void>((resolve) => this.waiters.push(resolve));
    }
  }

  async readByte(): Promise<number> {
    await this.ensure(1);
    return this.buf[this.pos++];
  }

  async readVarInt(): Promise<number> {
    let numRead = 0;
    let result = 0;
    let read: number;
    do {
      read = await this.readByte();
      const value = read & 0x7f;
      result |= value << (7 * numRead);
      numRead++;
      if (numRead > 5) throw new Error('VarInt too big');
    } while ((read & 0x80) !== 0);
    return result;
  }

  async readBytes(n: number): Promise<Buffer> {
    await this.ensure(n);
    const out = this.buf.subarray(this.pos, this.pos + n);
    this.pos += n;
    return out;
  }
}

export function ping(host: string, port: number, timeoutMs: number): Promise<PingResult> {
  return new Promise((resolve) => {
    const result: PingResult = { online: false, versionName: '', playersOnline: 0, playersMax: 0, motd: '', error: '' };
    const socket = new net.Socket();
    const reader = new PacketReader();
    let settled = false;

    const finish = (r: Partial<PingResult>) => {
      if (settled) return;
      settled = true;
      socket.destroy();
      resolve({ ...result, ...r });
    };

    socket.setTimeout(timeoutMs);
    socket.on('timeout', () => finish({ online: false, error: 'Connection timed out' }));
    socket.on('error', (err) => finish({ online: false, error: err.message }));
    socket.on('data', (chunk) => reader.feed(chunk));

    socket.connect(port, host, async () => {
      try {
        // Handshake packet: id=0x00, protocol=-1, host, port, next state=1 (status)
        const hsChunks: number[] = [];
        writeVarInt(hsChunks, 0x00);
        writeVarInt(hsChunks, -1);
        const hostBuf = encodeString(host);
        const portBuf = Buffer.alloc(2);
        portBuf.writeUInt16BE(port, 0);
        const nextStateChunks: number[] = [];
        writeVarInt(nextStateChunks, 1);
        const handshakeBody = Buffer.concat([Buffer.from(hsChunks), hostBuf, portBuf, Buffer.from(nextStateChunks)]);
        socket.write(buildPacket(handshakeBody));

        // Status request packet: id=0x00, empty body
        socket.write(buildPacket(Buffer.from([0x00])));

        // Read response packet
        await reader.readVarInt(); // packet length
        const packetId = await reader.readVarInt();
        if (packetId !== 0x00) throw new Error(`Unexpected packet id: ${packetId}`);
        const strLen = await reader.readVarInt();
        const strBytes = await reader.readBytes(strLen);
        const json = strBytes.toString('utf-8');

        const root = JSON.parse(json);
        finish({
          online: true,
          versionName: root.version?.name ?? 'unknown',
          playersOnline: root.players?.online ?? 0,
          playersMax: root.players?.max ?? 0,
          motd: extractMotd(root.description)
        });
      } catch (e: any) {
        finish({ online: false, error: e.message ?? String(e) });
      }
    });
  });
}
