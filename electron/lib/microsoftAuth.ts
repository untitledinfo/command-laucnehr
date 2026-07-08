import { postForm, postJson, getJsonAuthed } from './http';
import type { Account } from './configStore';

// IMPORTANT: Microsoft requires every application talking to Xbox Live /
// Minecraft Services to use its OWN registered Azure AD application (App
// registrations -> "Public client/native" platform, no client secret,
// redirect URI not required for device code flow). Replace CLIENT_ID below
// with the Application (client) ID from your own registration at
// https://portal.azure.com before this will work.
const CLIENT_ID = 'PUT-YOUR-AZURE-APP-CLIENT-ID-HERE';
const SCOPE = 'XboxLive.signin offline_access';

const DEVICE_CODE_URL = 'https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode';
const TOKEN_URL = 'https://login.microsoftonline.com/consumers/oauth2/v2.0/token';
const XBL_AUTH_URL = 'https://user.auth.xboxlive.com/user/authenticate';
const XSTS_AUTH_URL = 'https://xsts.auth.xboxlive.com/xsts/authorize';
const MC_LOGIN_URL = 'https://api.minecraftservices.com/authentication/login_with_xbox';
const MC_PROFILE_URL = 'https://api.minecraftservices.com/minecraft/profile';

export interface DeviceCode {
  deviceCode: string;
  userCode: string;
  verificationUri: string;
  interval: number;
  expiresIn: number;
}

export interface MsTokens {
  access_token: string;
  refresh_token: string;
}

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export async function requestDeviceCode(): Promise<DeviceCode> {
  const body = `client_id=${CLIENT_ID}&scope=${encodeURIComponent(SCOPE)}`;
  const resp = await postForm(DEVICE_CODE_URL, body, 'application/x-www-form-urlencoded');
  const json = JSON.parse(resp);
  if (json.error) throw new Error(`Device code request failed: ${json.error_description}`);
  return {
    deviceCode: json.device_code,
    userCode: json.user_code,
    verificationUri: json.verification_uri ?? 'https://microsoft.com/link',
    interval: json.interval ?? 5,
    expiresIn: json.expires_in ?? 900
  };
}

/** Blocks (polling) until the user finishes signing in, or throws on timeout/denial/cancellation. */
export async function pollForToken(
  dc: DeviceCode,
  onTick?: () => void,
  shouldCancel?: () => boolean
): Promise<MsTokens> {
  const deadline = Date.now() + dc.expiresIn * 1000;
  let intervalMs = Math.max(dc.interval, 5) * 1000;

  while (Date.now() < deadline) {
    await sleep(intervalMs);
    if (shouldCancel?.()) throw new Error('Sign-in cancelled.');
    onTick?.();
    const body =
      `grant_type=urn:ietf:params:oauth:grant-type:device_code` +
      `&client_id=${CLIENT_ID}&device_code=${dc.deviceCode}`;
    const resp = await postForm(TOKEN_URL, body, 'application/x-www-form-urlencoded');
    const json = JSON.parse(resp);
    if (json.access_token) {
      return { access_token: json.access_token, refresh_token: json.refresh_token };
    }
    const error = json.error ?? '';
    if (error === 'authorization_pending') {
      continue;
    } else if (error === 'slow_down') {
      intervalMs += 5000;
    } else {
      throw new Error(`Login failed: ${json.error_description}`);
    }
  }
  throw new Error('Login timed out. Please try again.');
}

export async function refreshMsToken(refreshToken: string): Promise<MsTokens> {
  const body =
    `grant_type=refresh_token&client_id=${CLIENT_ID}` +
    `&refresh_token=${refreshToken}&scope=${encodeURIComponent(SCOPE)}`;
  const resp = await postForm(TOKEN_URL, body, 'application/x-www-form-urlencoded');
  const json = JSON.parse(resp);
  if (!json.access_token) throw new Error(`Failed to refresh Microsoft token: ${json.error_description}`);
  return { access_token: json.access_token, refresh_token: json.refresh_token };
}

function formatUuid(raw: string): string {
  if (!raw || raw.length !== 32) return raw;
  return `${raw.slice(0, 8)}-${raw.slice(8, 12)}-${raw.slice(12, 16)}-${raw.slice(16, 20)}-${raw.slice(20)}`;
}

/** Full chain: MS access token -> Xbox Live -> XSTS -> Minecraft -> profile. */
export async function completeLogin(msAccessToken: string, msRefreshToken: string): Promise<Account> {
  // 1) Xbox Live
  const xblBody = {
    Properties: {
      AuthMethod: 'RPS',
      SiteName: 'user.auth.xboxlive.com',
      RpsTicket: `d=${msAccessToken}`
    },
    RelyingParty: 'http://auth.xboxlive.com',
    TokenType: 'JWT'
  };
  const xblResp = await postJson(XBL_AUTH_URL, JSON.stringify(xblBody));
  const xblJson = JSON.parse(xblResp);
  const xblToken = xblJson.Token;
  if (!xblToken) throw new Error(`Xbox Live auth failed: ${xblResp}`);

  // 2) XSTS
  const xstsBody = {
    Properties: { SandboxId: 'RETAIL', UserTokens: [xblToken] },
    RelyingParty: 'rp://api.minecraftservices.com/',
    TokenType: 'JWT'
  };
  const xstsResp = await postJson(XSTS_AUTH_URL, JSON.stringify(xstsBody));
  const xstsJson = JSON.parse(xstsResp);
  if (xstsJson.XErr) {
    throw new Error(
      `XSTS auth failed (this Microsoft account may need adult verification, or does not own Minecraft): ${xstsJson.XErr}`
    );
  }
  const xstsToken = xstsJson.Token;
  const uhs = xstsJson.DisplayClaims?.xui?.[0]?.uhs;
  if (!xstsToken || !uhs) throw new Error('XSTS auth response missing token/uhs');

  // 3) Minecraft Services login
  const mcBody = { identityToken: `XBL3.0 x=${uhs};${xstsToken}` };
  const mcResp = await postJson(MC_LOGIN_URL, JSON.stringify(mcBody));
  const mcJson = JSON.parse(mcResp);
  const mcAccessToken = mcJson.access_token;
  if (!mcAccessToken) throw new Error(`Minecraft services login failed: ${mcResp}`);

  // 4) Profile (name + uuid)
  const profileResp = await getJsonAuthed(MC_PROFILE_URL, mcAccessToken);
  const profileJson = JSON.parse(profileResp);
  if (profileJson.error || !profileJson.id) {
    throw new Error(`Could not load Minecraft profile (does this account own the game?): ${profileResp}`);
  }
  const formattedUuid = formatUuid(profileJson.id);

  return {
    type: 'msa',
    name: profileJson.name,
    uuid: formattedUuid,
    accessToken: mcAccessToken,
    refreshToken: msRefreshToken
  };
}
