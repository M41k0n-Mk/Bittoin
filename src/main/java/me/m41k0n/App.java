package me.m41k0n;

public class App {
    public static void main(String[] args) {

        Blockchain blockchain = new Blockchain(4);
        blockchain.addBlock("Alice pays Bob 10 coins");
        blockchain.addBlock("Bob pays Carol 5 coins");

        System.out.println("Blockchain:");
        for (Block block : blockchain.getBlocks()) {
            System.out.println("Index: " + block.getIndex());
            System.out.println("Timestamp: " + block.getTimestamp());
            System.out.println("Data: " + block.getData());
            System.out.println("Prev Hash: " + block.getPreviousHash());
            System.out.println("Hash: " + block.getHash());
            System.out.println("Nonce: " + block.getNonce());
            System.out.println("-----------------------------");
        }

        System.out.println("Is blockchain valid? " + blockchain.isValid());
    }
}