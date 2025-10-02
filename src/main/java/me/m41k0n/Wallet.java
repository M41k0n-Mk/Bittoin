package me.m41k0n;

import lombok.Getter;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

@Getter
public class Wallet {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public Wallet() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair pair = keyGen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Error generating wallet keys", e);
        }
    }

    public Wallet(KeyPair pair) {
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public String getAddress() {
        return java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String getPrivateKeyBase64() {
        return java.util.Base64.getEncoder().encodeToString(privateKey.getEncoded());
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

    public static PublicKey decodePublicKey(String base64) {
        try {
            byte[] pubBytes = java.util.Base64.getDecoder().decode(base64);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(new java.security.spec.X509EncodedKeySpec(pubBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static PrivateKey decodePrivateKey(String base64) {
        try {
            byte[] privBytes = java.util.Base64.getDecoder().decode(base64);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(privBytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}