import {
  atou8a,
  decryptAES,
  deriveAESKey,
  encryptAES,
  generateECDHKeyPair,
  u8atoa,
} from './crypto';

describe('cryptography', () => {
  it('converting Base64 to Uint8Array works', () => {
    expect(atou8a(btoa('abcde'))).toEqual(
      new Uint8Array([0x61, 0x62, 0x63, 0x64, 0x65]),
    );
  });

  it('converting Uint8Array to Base64 works', () => {
    expect(u8atoa(new Uint8Array([0x61, 0x62, 0x63, 0x64, 0x65]))).toBe(
      btoa('abcde'),
    );
  });

  it('key exchange works', async () => {
    const a = await generateECDHKeyPair();
    const b = await generateECDHKeyPair();

    const aDerived = await deriveAESKey(a.privateKey, b.publicKey);
    const bDerived = await deriveAESKey(b.privateKey, a.publicKey);

    const plain = 'hello there';
    const msg = new TextEncoder().encode(plain);

    const { ciphertext, iv } = await encryptAES(aDerived, msg);
    expect(
      new TextDecoder().decode(await decryptAES(bDerived, ciphertext, iv)),
    ).toBe(plain);
  });
});
