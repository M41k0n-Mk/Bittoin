package me.m41k0n;

import java.io.*;
import java.net.*;
import java.util.*;

public class SimpleNode {

    private final Blockchain blockchain;
    private final int port;
    private final List<InetSocketAddress> peers = new ArrayList<>();

    public SimpleNode(Blockchain blockchain, int port) {
        this.blockchain = blockchain;
        this.port = port;
    }

    public void addPeer(String host, int peerPort) {
        peers.add(new InetSocketAddress(host, peerPort));
    }

    public void start() {
        new Thread(this::listen).start();
    }

    private void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Node listening on port " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (Exception e) {
                    System.out.println("Error accepting connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            socket.setSoTimeout(8000);
            System.out.println("Receiving connection from " + socket.getInetAddress() + ":" + socket.getPort());
            Object commandObj = in.readObject();
            System.out.println("Command received: " + commandObj);

            if ("GET_BLOCKCHAIN".equals(commandObj)) {
                out.writeObject(blockchain.getBlocks());
                out.flush();
            } else if ("NEW_BLOCK".equals(commandObj)) {
                Block newBlock = (Block) in.readObject();
                System.out.println("New block received from peer.");
                // Adaptação: Recebe qual miner address do peer minerou o bloco
                tryAddBlock(newBlock);
            }
        } catch (Exception e) {
            System.out.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    private void tryAddBlock(Block block) {
        List<Block> localChain = blockchain.getBlocks();
        // Adaptação: pega minerador do bloco recebido (coinbase transaction)
        String minerAddress = null;
        List<Transaction> txs = block.getTransactions();
        if (!txs.isEmpty() && txs.getFirst().from().equals("COINBASE")) {
            minerAddress = txs.getFirst().to();
        }
        if (block.getIndex() == localChain.size()) {
            boolean ok;
            if (minerAddress != null) {
                ok = blockchain.addBlock(txs.subList(1, txs.size()), minerAddress); // remove coinbase, reaplica com reward e halving
            } else {
                ok = blockchain.addBlock(txs, null); // fallback
            }
            if (ok) System.out.println("Added new block from peer!");
        } else if (block.getIndex() > localChain.size()) {
            List<Block> peerChain = requestChainFromPeer();
            if (peerChain != null && peerChain.size() > localChain.size()) {
                blockchain.replaceChain(peerChain);
                System.out.println("Replaced local chain with peer's longer chain!");
            }
        }
    }

    private List<Block> requestChainFromPeer() {
        for (InetSocketAddress peer : peers) {
            int attempts = 0;
            while (attempts < 3) {
                try (Socket socket = new Socket(peer.getHostName(), peer.getPort());
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                    out.writeObject("GET_BLOCKCHAIN");
                    out.flush();
                    Object response = in.readObject();
                    if (response instanceof List) {
                        System.out.println("Blockchain received from peer " + peer);
                        return (List<Block>) response;
                    }
                } catch (Exception e) {
                    attempts++;
                    System.out.println("Error requesting blockchain from peer " + peer + " attempt " + attempts);
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }
        }
        return null;
    }

    public void broadcastBlock(Block block) {
        for (InetSocketAddress peer : peers) {
            int attempts = 0;
            boolean sent = false;
            while (attempts < 3 && !sent) {
                try (Socket socket = new Socket(peer.getHostName(), peer.getPort());
                     ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    out.writeObject("NEW_BLOCK");
                    out.writeObject(block);
                    out.flush();
                    sent = true;
                    System.out.println("Block sent to peer " + peer);
                } catch (Exception e) {
                    attempts++;
                    System.out.println("Error sending block to peer " + peer + " attempt " + attempts + ": " + e.getMessage());
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }
            if (!sent) {
                System.out.println("Could not send block to peer " + peer + " after 3 attempts.");
            }
        }
    }
}