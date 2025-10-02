package me.m41k0n;

import java.util.*;

public class Blockchain {

    private final List<Block> chain = new ArrayList<>();
    private final Map<String, Double> balances = new HashMap<>();
    private final int difficulty;
    private final Map<String, UTXO> utxoSet = new HashMap<>();

    public Blockchain(int difficulty, String satoshiAddress) {
        this.difficulty = difficulty;
        Transaction coinbase = Transaction.createCoinbase(satoshiAddress, 50.0);
        Block genesis = new Block(0, List.of(coinbase), "0", difficulty);
        chain.add(genesis);
        utxoSet.put(genesis.getHash() + ":0", new UTXO(genesis.getHash(), 0, satoshiAddress, 50.0));
    }

    public double getBalance(String address) {
        return utxoSet.values().stream()
                .filter(utxo -> utxo.owner().equals(address))
                .mapToDouble(UTXO::amount)
                .sum();
    }

    public List<Block> getBlocks() {
        return List.copyOf(chain);
    }

    public Block getLatestBlock() {
        return chain.getLast();
    }

    public boolean addBlock(List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            if (!isTransactionValid(tx)) {
                System.out.println("Invalid transaction: " + tx);
                return false;
            }
        }

        Block previous = getLatestBlock();
        Block newBlock = new Block(previous.getIndex() + 1, transactions, previous.getHash(), difficulty);
        chain.add(newBlock);

        int outputIndex = 0;
        for (Transaction tx : transactions) {
            if (tx.from().equals("GENESIS") || tx.from().equals("GENESIS_AUDITOR")) {
                utxoSet.put(newBlock.getHash() + ":" + outputIndex, new UTXO(newBlock.getHash(), outputIndex, tx.to(), tx.amount()));
            } else {
                double remaining = tx.amount();
                Iterator<Map.Entry<String, UTXO>> it = utxoSet.entrySet().iterator();
                while (it.hasNext() && remaining > 0) {
                    Map.Entry<String, UTXO> entry = it.next();
                    UTXO utxo = entry.getValue();
                    if (utxo.owner().equals(tx.from())) {
                        if (utxo.amount() <= remaining) {
                            remaining -= utxo.amount();
                            it.remove();
                        } else {
                            utxoSet.put(entry.getKey(), new UTXO(utxo.txId(), utxo.outputIndex(), utxo.owner(), utxo.amount() - remaining));
                            remaining = 0;
                        }
                    }
                }
                utxoSet.put(newBlock.getHash() + ":" + outputIndex, new UTXO(newBlock.getHash(), outputIndex, tx.to(), tx.amount()));
            }
            outputIndex++;
        }
        recalculateBalances();
        return true;
    }

    public void replaceChain(List<Block> newChain) {
        if (newChain.size() > chain.size()) {
            chain.clear();
            chain.addAll(newChain);
            recalculateBalances();
        }
    }

    private void recalculateBalances() {
        balances.clear();
        for (Block block : chain) {
            for (Transaction tx : block.getTransactions()) {
                if (!tx.isValid()) continue;
                if (tx.from().equals("COINBASE")) {
                    balances.put(tx.to(), balances.getOrDefault(tx.to(), 0.0) + tx.amount());
                } else {
                    balances.put(tx.from(), balances.getOrDefault(tx.from(), 0.0) - tx.amount());
                    balances.put(tx.to(), balances.getOrDefault(tx.to(), 0.0) + tx.amount());
                }
            }
        }
    }

    private boolean isTransactionValid(Transaction tx) {
        if (tx.from().equals("GENESIS") || tx.from().equals("GENESIS_AUDITOR")) return true;
        double saldo = getBalance(tx.from());
        return saldo >= tx.amount();
    }
}