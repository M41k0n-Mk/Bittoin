package me.m41k0n;

import java.io.*;
import java.security.*;
import java.util.*;

public class WalletUtils {
    public static void generateAndSaveWallets(String filename) throws IOException {
        Map<String, Wallet> wallets = new LinkedHashMap<>();
        wallets.put("satoshi", new Wallet());
        wallets.put("alice", new Wallet());
        wallets.put("bob", new Wallet());
        wallets.put("carol", new Wallet());

        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<String, Wallet> entry : wallets.entrySet()) {
                out.println(entry.getKey() + ":" + entry.getValue().getAddress() + ":" + entry.getValue().getPrivateKeyBase64());
            }
        }
    }

    public static Map<String, Wallet> loadWallets(String filename) throws IOException {
        Map<String, Wallet> wallets = new HashMap<>();
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 3) {
                    String name = parts[0];
                    String pub = parts[1];
                    String priv = parts[2];
                    wallets.put(name, createWalletFromKeys(pub, priv));
                }
            }
        }
        return wallets;
    }

    public static Wallet createWalletFromKeys(String pub, String priv) {
        try {
            PublicKey pubKey = Wallet.decodePublicKey(pub);
            PrivateKey privKey = Wallet.decodePrivateKey(priv);
            KeyPair kp = new KeyPair(pubKey, privKey);
            return new Wallet(kp);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}