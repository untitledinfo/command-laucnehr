/**
 * Converts a maven coordinate ("group:artifact:version" or
 * "group:artifact:version:classifier") into the relative path Mojang/
 * Fabric/Forge use under a libraries/ directory.
 */
export function coordToPath(coord: string): string | null {
  const parts = coord.split(':');
  if (parts.length < 3) return null;
  const group = parts[0].replace(/\./g, '/');
  const artifact = parts[1];
  const version = parts[2];
  const classifier = parts.length > 3 ? `-${parts[3]}` : '';
  return `${group}/${artifact}/${version}/${artifact}-${version}${classifier}.jar`;
}
