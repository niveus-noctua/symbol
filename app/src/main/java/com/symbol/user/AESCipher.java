package com.symbol.user;

import android.util.Base64;
import android.util.Log;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

public class AESCipher {

    private String line;
    private String encodedLine;
    private String decodedLine;
    private SecretKeySpec sks;
    private SecureRandom sr;
    private KeyGenerator kg;
    private byte[] encodedBytes;
    private byte[] decodedBytes;

    public AESCipher(String line) {
        this.line = line;
    }

    public void seed() {
        sks = null;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("roses are red violets are blue".getBytes());
            kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec((kg.generateKey()).getEncoded(), "AES");
        } catch (Exception e) {
            Log.e("ChatsFragment", "AES error");
        }
    }

    public void encode(String TAG) {
        encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(line.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }
    }

    public void decode(String TAG) {
        decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, sks);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            Log.e(TAG, "AES decryption error");
        }
    }

    public String getEncodedLine() {
        encodedLine = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
        if (encodedLine.contains("\\"))
            encodedLine.replace("\\", "s");
        if (encodedLine.contains("/"))
            encodedLine.replace("/", "w");
        return encodedLine;
    }

    public String getDecodedLine() {
        decodedLine = Base64.encodeToString(decodedBytes, Base64.DEFAULT);
        if (decodedLine.contains("\\"))
            decodedLine.replace("\\", "s");
        if (decodedLine.contains("/"))
            decodedLine.replace("/", "w");
        return decodedLine;
    }
}
