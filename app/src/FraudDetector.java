import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long timestamp; // in ms

    Transaction(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }
}

public class FraudDetector {
    private final List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // Classic Two-Sum
    public List<int[]> findTwoSum(int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<int[]> results = new ArrayList<>();
        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                results.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return results;
    }

    // Two-Sum with time window (1 hour)
    public List<int[]> findTwoSumWithWindow(int target, long windowMs) {
        List<int[]> results = new ArrayList<>();
        Map<Integer, List<Transaction>> map = new HashMap<>();
        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                for (Transaction other : map.get(complement)) {
                    if (Math.abs(t.timestamp - other.timestamp) <= windowMs) {
                        results.add(new int[]{other.id, t.id});
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return results;
    }

    // K-Sum (recursive)
    public List<List<Integer>> findKSum(int k, int target) {
        List<List<Integer>> results = new ArrayList<>();
        backtrack(transactions, k, target, 0, new ArrayList<>(), results);
        return results;
    }

    private void backtrack(List<Transaction> txs, int k, int target, int start,
                           List<Integer> current, List<List<Integer>> results) {
        if (k == 0 && target == 0) {
            results.add(new ArrayList<>(current));
            return;
        }
        if (k == 0 || target < 0) return;

        for (int i = start; i < txs.size(); i++) {
            current.add(txs.get(i).id);
            backtrack(txs, k - 1, target - txs.get(i).amount, i + 1, current, results);
            current.remove(current.size() - 1);
        }
    }

    // Duplicate detection
    public List<String> detectDuplicates() {
        Map<String, Map<Integer, Set<String>>> map = new HashMap<>();
        List<String> results = new ArrayList<>();

        for (Transaction t : transactions) {
            map.computeIfAbsent(t.merchant, k -> new HashMap<>())
                    .computeIfAbsent(t.amount, k -> new HashSet<>())
                    .add(t.account);
        }

        for (Map.Entry<String, Map<Integer, Set<String>>> merchantEntry : map.entrySet()) {
            for (Map.Entry<Integer, Set<String>> amountEntry : merchantEntry.getValue().entrySet()) {
                if (amountEntry.getValue().size() > 1) {
                    results.add("{amount:" + amountEntry.getKey() +
                            ", merchant:" + merchantEntry.getKey() +
                            ", accounts:" + amountEntry.getValue() + "}");
                }
            }
        }
        return results;
    }

    public static void main(String[] args) {
        FraudDetector fd = new FraudDetector();

        fd.addTransaction(new Transaction(1, 500, "Store A", "acc1", System.currentTimeMillis()));
        fd.addTransaction(new Transaction(2, 300, "Store B", "acc2", System.currentTimeMillis() + 900000));
        fd.addTransaction(new Transaction(3, 200, "Store C", "acc3", System.currentTimeMillis() + 1800000));
        fd.addTransaction(new Transaction(4, 500, "Store A", "acc2", System.currentTimeMillis()));

        System.out.println("findTwoSum(target=500) → " + Arrays.deepToString(fd.findTwoSum(500).toArray()));
        System.out.println("findTwoSumWithWindow(target=500, 1h) → " + Arrays.deepToString(fd.findTwoSumWithWindow(500, 3600_000).toArray()));
        System.out.println("findKSum(k=3, target=1000) → " + fd.findKSum(3, 1000));
        System.out.println("detectDuplicates() → " + fd.detectDuplicates());
    }
}