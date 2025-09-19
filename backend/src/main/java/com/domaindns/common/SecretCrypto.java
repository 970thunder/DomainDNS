package com.domaindns.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM encryption for secrets at rest.
 * Prefixes ciphertext with "enc:" to distinguish from plaintext.
 */
@Component
public class SecretCrypto {
    private static final Logger log = LoggerFactory.getLogger(SecretCrypto.class);
    private static final String ENC_PREFIX = "enc:";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12; // bytes

    private final byte[] keyBytes;
    private final SecureRandom secureRandom = new SecureRandom();

    public SecretCrypto(@Value("${secrets.aes-key:}") String propKey) {
        String envKey = System.getenv("SECRETS_AES_KEY");
        String source = (propKey != null && !propKey.isEmpty()) ? propKey : (envKey == null ? "" : envKey);
        byte[] kb = deriveKey(source);
        if (kb == null) {
            // Dev fallback: generate volatile key; log warning (not recommended for prod)
            kb = generateRandomKey();
            log.warn(
                    "[SecretCrypto] Using generated volatile AES key (dev only). Configure secrets.aes-key or SECRETS_AES_KEY for persistence.");
        }
        this.keyBytes = kb;
    }

    private byte[] deriveKey(String source) {
        try {
            if (source == null || source.isEmpty())
                return null;
            // Try base64 first
            try {
                byte[] decoded = Base64.getDecoder().decode(source);
                if (decoded.length == 16 || decoded.length == 24 || decoded.length == 32)
                    return decoded;
            } catch (IllegalArgumentException ignore) {
            }
            // Fallback: UTF-8 bytes hashed to 32 bytes via SHA-256
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            return md.digest(source.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("deriveKey error: {}", e.getMessage());
            return null;
        }
    }

    private byte[] generateRandomKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(256);
            SecretKey sk = kg.generateKey();
            return sk.getEncoded();
        } catch (Exception e) {
            // Fallback to 128-bit
            byte[] b = new byte[16];
            secureRandom.nextBytes(b);
            return b;
        }
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isEmpty())
            return plain;
        if (isEncrypted(plain))
            return plain; // idempotent
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(1 + iv.length + cipherText.length);
            bb.put((byte) iv.length);
            bb.put(iv);
            bb.put(cipherText);
            String b64 = Base64.getEncoder().encodeToString(bb.array());
            return ENC_PREFIX + b64;
        } catch (Exception e) {
            throw new IllegalStateException("加密失败", e);
        }
    }

    public String decrypt(String enc) {
        if (enc == null || enc.isEmpty())
            return enc;
        if (!isEncrypted(enc))
            return enc;
        try {
            String b64 = enc.substring(ENC_PREFIX.length());
            byte[] all = Base64.getDecoder().decode(b64);
            ByteBuffer bb = ByteBuffer.wrap(all);
            int ivLen = bb.get();
            byte[] iv = new byte[ivLen];
            bb.get(iv);
            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密失败", e);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENC_PREFIX);
    }

    public String decryptIfEncrypted(String value) {
        return isEncrypted(value) ? decrypt(value) : value;
    }
}
