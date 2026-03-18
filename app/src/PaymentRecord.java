public class PaymentRecord {
    public int id;
    public int amount;
    public String merchant;
    public String account;
    public long timestamp; // stored in milliseconds

    public PaymentRecord(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PaymentRecord{" +
                "id=" + id +
                ", amount=" + amount +
                ", merchant='" + merchant + '\'' +
                ", account='" + account + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}