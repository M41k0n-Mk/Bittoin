package me.m41k0n;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NodeRunner {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java -jar NodeRunner.jar <port> <comma_separated_peer_ports>");
            System.out.println("Example: java -jar NodeRunner.jar 5000 5001,5002,5003");
            return;
        }

        File file = new File("wallets.txt");
        if (!file.exists()) {
            WalletUtils.generateAndSaveWallets("wallets.txt");
        }
        Map<String, Wallet> wallets = WalletUtils.loadWallets("wallets.txt");
        String SATOSHI_ADDR = wallets.get("satoshi").getAddress();
        String ALICE_ADDR   = wallets.get("alice").getAddress();
        String BOB_ADDR     = wallets.get("bob").getAddress();
        String CAROL_ADDR   = wallets.get("carol").getAddress();

        int port = Integer.parseInt(args[0]);
        String[] peerPorts = args[1].split(",");

        Blockchain blockchain = new Blockchain(4, SATOSHI_ADDR);
        SimpleNode node = new SimpleNode(blockchain, port);

        for (String peerPort : peerPorts) {
            int p = Integer.parseInt(peerPort.trim());
            if (p != port) node.addPeer("localhost", p);
        }

        new Thread(node::start).start();

        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        Scanner scanner = new Scanner(System.in);
        System.out.println("Node started on port " + port);
        System.out.println("Enter transactions: <from> <to> <amount>");
        System.out.println("Or type 'state' to show the current blockchain state and wallet balances.");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) break;

            if (line.trim().equalsIgnoreCase("state")) {
                System.out.println("----- Current Blockchain State -----");
                List<Block> blocks = blockchain.getBlocks();
                for (Block block : blocks) {
                    System.out.println("Block #" + block.getIndex() + " - Hash: " + block.getHash());
                    for (Transaction tx : block.getTransactions()) {
                        System.out.println("  " + tx);
                    }
                }
                System.out.println("----- Wallet Balances -----");
                System.out.printf("satoshi: %.8f%n", blockchain.getBalance(SATOSHI_ADDR));
                System.out.printf("alice:   %.8f%n", blockchain.getBalance(ALICE_ADDR));
                System.out.printf("bob:     %.8f%n", blockchain.getBalance(BOB_ADDR));
                System.out.printf("carol:   %.8f%n", blockchain.getBalance(CAROL_ADDR));
                System.out.println("-----------------------------------");
                continue;
            }

            String[] parts = line.trim().split("\\s+");
            if (parts.length != 3) {
                System.out.println("Invalid format. Use: <from> <to> <amount>");
                continue;
            }

            String from = parts[0].toLowerCase();
            String to = parts[1].toLowerCase();
            Wallet fromWallet = wallets.get(from);
            String toAddr = switch (to) {
                case "satoshi" -> SATOSHI_ADDR;
                case "alice" -> ALICE_ADDR;
                case "bob" -> BOB_ADDR;
                case "carol" -> CAROL_ADDR;
                default -> null;
            };
            double amount;
            try { amount = Double.parseDouble(parts[2]); } catch (Exception e) {
                System.out.println("Invalid amount.");
                continue;
            }
            if (fromWallet != null && toAddr != null) {
                Transaction tx = Transaction.createSigned(fromWallet, toAddr, amount);
                // Adaptation: the sender is the miner in this simulation
                boolean ok = blockchain.addBlock(List.of(tx), fromWallet.getAddress());
                if (ok) {
                    node.broadcastBlock(blockchain.getLatestBlock());
                    System.out.println("Transaction created, signed, and block broadcasted!");
                } else {
                    System.out.println("Invalid transaction or insufficient balance.");
                }
            } else {
                System.out.println("Wallet not found.");
            }
        }
        System.out.println("Shutting down node on port " + port);
    }
}