package me.m41k0n;

import java.util.List;

public class TwoNodeDemo {
    public static void main(String[] args) {
        Wallet satoshi = new Wallet();
        Wallet alice = new Wallet();

        Blockchain chainA = new Blockchain(4, satoshi.getAddress());
        Blockchain chainB = new Blockchain(4, satoshi.getAddress());

        SimpleNode nodeA = new SimpleNode(chainA, 5000);
        SimpleNode nodeB = new SimpleNode(chainB, 5001);

        nodeA.addPeer("localhost", 5001);
        nodeB.addPeer("localhost", 5000);

        new Thread(nodeA::start).start();
        new Thread(nodeB::start).start();

        // Wait for servers to start
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        Transaction tA1 = Transaction.createSigned(satoshi, alice.getAddress(), 25.0);
        chainA.addBlock(List.of(tA1));
        nodeA.broadcastBlock(chainA.getLatestBlock());

        // Wait for propagation
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        System.out.println("Node A chain:");
        for (Block b : chainA.getBlocks())
            System.out.println(b);

        System.out.println("Node B chain:");
        for (Block b : chainB.getBlocks())
            System.out.println(b);
    }
}