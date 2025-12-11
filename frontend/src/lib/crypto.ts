const generateECDHKeyPair = async () =>
  await window.crypto.subtle.generateKey(
    { name: 'ECDH', namedCurve: 'P-256' },
    true,
    ['deriveKey'],
  );

const exportPublicKey = async (keyPair: CryptoKeyPair) =>
  await window.crypto.subtle.exportKey('jwk', keyPair.publicKey);

const importPublicKey = async (jwkKey: JsonWebKey) =>
  await window.crypto.subtle.importKey(
    'jwk',
    jwkKey,
    { name: 'ECDH', namedCurve: 'P-256' },
    true,
    [],
  );

const deriveAESKey = async (
  privateKey: CryptoKey,
  receivedPublicKey: CryptoKey,
) =>
  await window.crypto.subtle.deriveKey(
    { name: 'ECDH', public: receivedPublicKey },
    privateKey,
    { name: 'AES-GCM', length: 256 },
    true,
    ['encrypt', 'decrypt'],
  );

const encryptAES = async (aesKey: CryptoKey, plaintext: BufferSource) => {
  const iv = window.crypto.getRandomValues(new Uint8Array(12));

  const ciphertext = await window.crypto.subtle.encrypt(
    { name: 'AES-GCM', iv },
    aesKey,
    plaintext,
  );

  return {
    ciphertext,
    iv,
  };
};

const atou8a = <T extends string>(b64encoded: T) =>
  new Uint8Array(
    atob(b64encoded)
      .split('')
      .map((c) => c.charCodeAt(0)),
  );

const u8atoa = (arr: Uint8Array) => {
  let str = '';
  arr.forEach((c) => {
    str += String.fromCharCode(c);
  });
  return btoa(str);
};

const decryptAES = async (
  aesKey: CryptoKey,
  ciphertext: BufferSource,
  iv: Uint8Array<ArrayBuffer>,
) =>
  await window.crypto.subtle.decrypt(
    { name: 'AES-GCM', iv },
    aesKey,
    ciphertext,
  );

export {
  atou8a,
  decryptAES,
  deriveAESKey,
  encryptAES,
  exportPublicKey,
  generateECDHKeyPair,
  importPublicKey,
  u8atoa,
};
