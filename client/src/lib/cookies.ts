export function getCookieValue(name: string): string | null {
  if (typeof document === 'undefined' || !name) {
    return null;
  }

  const encodedName = encodeURIComponent(name);
  const parts = document.cookie ? document.cookie.split('; ') : [];
  for (const part of parts) {
    const separatorIndex = part.indexOf('=');
    if (separatorIndex <= 0) continue;

    const key = part.slice(0, separatorIndex);
    if (key !== encodedName) continue;

    const value = part.slice(separatorIndex + 1);
    return decodeURIComponent(value);
  }

  return null;
}
