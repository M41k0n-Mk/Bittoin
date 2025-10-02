package me.m41k0n;

import java.io.Serializable;
import java.security.PublicKey;

public record Transaction(String from, String to, double amount, String signature) implements Serializable {

    public static Transaction createCoinbase(String to, double amount) {
        return new Transaction("COINBASE", to, amount, null);
    }

    public static Transaction createSigned(Wallet fromWallet, String to, double amount) {
        String data = fromWallet.getAddress() + to + amount;
        String sig = fromWallet.sign(data);
        return new Transaction(fromWallet.getAddress(), to, amount, sig);
    }

    public boolean isValid() {
        if (from.equals("COINBASE")) return true;
        String data = from + to + amount;
        PublicKey pubKey = Wallet.decodePublicKey(from);
        return Wallet.verify(pubKey, data, signature);
    }

    @Override
    public String toString() {
        if (from.equals("COINBASE")) {
            return "COINBASE -> " + to + ": " + amount;
        }
        return from + " -> " + to + ": " + amount + " (valid signature? " + isValid() + ")";
    }
}