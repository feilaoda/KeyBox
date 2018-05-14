package com.bitcode.agent;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Block implements Serializable, Comparable {
    private static final long serialVersionUID = 1L;

    protected int index;
    protected Long timestamp;
    protected String hash;
    protected String previousHash;
    protected String creator;

    protected Integer nonce;

    protected String data;



    private List<Transaction> transactions;


    // for jackson
    public Block() {
    }

    public Block(Block block) {
        this.index = block.index;
        this.timestamp = block.timestamp;
        this.hash = block.hash;
        this.previousHash = block.previousHash;
        this.creator = block.creator;
        this.nonce = block.nonce;
        this.data = block.getData();
        this.transactions = block.getTransactions();
    }

    @Override
    public String toString() {
        return "Block{" +
                "index=" + index +
                ", timestamp=" + timestamp +
                ", creator=" + creator +
//                ", hash='" + hash + '\'' +
//                ", previousHash='" + previousHash + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Block block = (Block) o;
        return index == block.index
                && timestamp.equals(block.timestamp)
                && hash.equals(block.hash)
                && previousHash.equals(block.previousHash)
                && creator.equals(block.creator);
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (this == o || this.equals(o)) {
            return 0;
        }

        if (o == null || getClass() != o.getClass()) {
            return -1;
        }

        final Block block = (Block) o;

        if(this.getIndex()>((Block) o).getIndex()) {
            return 1;
        }else if(this.getIndex() == ((Block) o).getIndex()) {
            return 0;
        }else {
            return -1;
        }
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + timestamp.hashCode();
        result = 31 * result + hash.hashCode();
        result = 31 * result + previousHash.hashCode();
        result = 31 * result + creator.hashCode();
        return result;
    }


    public Block(int index, String preHash, String creator) {
        this.index = index;
        this.previousHash = preHash;
        this.creator = creator;
        timestamp = System.currentTimeMillis();
        hash = Utils.hash256Str(String.valueOf(index) + previousHash + String.valueOf(timestamp));

        ProofOfWork proofOfWork = new ProofOfWork(this);
        Map<String, String> map = proofOfWork.run();

        if (map != null) {
            this.nonce = Integer.parseInt(map.get("nonce"));
            this.hash = map.get("hash");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("|-----------------------------------|" + creator + new Date());
            System.out.println("编号: \t" + index);
            System.out.println("nonce: \t" + nonce);
            System.out.println("时间:\t" + this.timestamp);
            System.out.println("数据:\t" + this.data);
            System.out.println("父Hash:\t" + this.previousHash);
            System.out.println("Hash：\t" + this.hash);
        }

    }

    public String getCreator() {
        return creator;
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}