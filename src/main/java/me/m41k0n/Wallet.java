package me.m41k0n;

import lombok.Getter;

import java.security.*;

@Getter
public class Wallet {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public Wallet() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC"); // Aqui poderia ser RSA tb
            keyGen.initialize(256);
            KeyPair pair = keyGen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Error generating wallet keys", e);
        }
    }

    public String getAddress() {
        return java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String sign(String data) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes());
            byte[] sigBytes = signature.sign();
            return java.util.Base64.getEncoder().encodeToString(sigBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error signing data", e);
        }
    }

    public static boolean verify(PublicKey publicKey, String data, String signatureBase64) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes());
            byte[] sigBytes = java.util.Base64.getDecoder().decode(signatureBase64);
            return signature.verify(sigBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying signature", e);
        }
    }
}