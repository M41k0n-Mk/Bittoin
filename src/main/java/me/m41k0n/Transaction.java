package me.m41k0n;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public record Transaction(String from, String to, double amount, String signature) {

    public static Transaction createSigned(Wallet senderWallet, String to, double amount) {
        String from = senderWallet.getAddress();
        String dataToSign = from + to + amount;
        String signature = senderWallet.sign(dataToSign);
        return new Transaction(from, to, amount, signature);
    }

    public static Transaction createCoinbase(String to, double amount) {
        return new Transaction("GENESIS", to, amount, null);
    }

    public boolean isSignatureValid() {
        if (from == null || from.equals("GENESIS") || signature == null) {
            return false;
        }
        try {
            byte[] pubBytes = java.util.Base64.getDecoder().decode(from);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            String data = from + to + amount;
            return Wallet.verify(publicKey, data, signature);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying transaction signature", e);
        }
    }

    private static String shorten(String s) {
        if (s == null) return "null";
        if (s.length() <= 12) return s;
        return s.substring(0, 8) + "..." + s.substring(s.length() - 4);
    }

    @Override
    public String toString() {
        String fromStr = shorten(from);
        String toStr = shorten(to);

        if (from == null || from.equals("GENESIS")) {
            return "COINBASE -> " + toStr + ": " + amount;
        }
        return fromStr + " -> " + toStr + ": " + amount +
                " (valid signature? " + isSignatureValid() + ")";
    }
}