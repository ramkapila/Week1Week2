import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long timestamp; // milliseconds

    public Transaction(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }
}

public class TransactionAnalyzer {

    private List<Transaction> transactions;

    public TransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // 1️⃣ Classic Two-Sum
    public List<int[]> findTwoSum(int target) {

        Map<Integer, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // 2️⃣ Two-Sum within time window (1 hour)
    public List<int[]> findTwoSumWithTimeWindow(int target, long windowMillis) {

        Map<Integer, List<Transaction>> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {

            int complement = target - t.amount;

            if (map.containsKey(complement)) {

                for (Transaction prev : map.get(complement)) {

                    if (Math.abs(t.timestamp - prev.timestamp) <= windowMillis) {
                        result.add(new int[]{prev.id, t.id});
                    }
                }
            }

            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }

        return result;
    }

    // 3️⃣ K-Sum using recursion
    public List<List<Integer>> findKSum(int k, int target) {

        List<List<Integer>> result = new ArrayList<>();
        backtrack(0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(int start, int k, int target,
                           List<Integer> current,
                           List<List<Integer>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        if (k == 0 || start >= transactions.size()) {
            return;
        }

        for (int i = start; i < transactions.size(); i++) {

            Transaction t = transactions.get(i);

            current.add(t.id);

            backtrack(i + 1, k - 1, target - t.amount, current, result);

            current.remove(current.size() - 1);
        }
    }

    // 4️⃣ Duplicate Detection
    public List<String> detectDuplicates() {

        Map<String, Set<String>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {

            String key = t.amount + "-" + t.merchant;

            map.putIfAbsent(key, new HashSet<>());

            map.get(key).add(t.account);
        }

        for (String key : map.keySet()) {

            if (map.get(key).size() > 1) {

                String[] parts = key.split("-");

                result.add("amount:" + parts[0] +
                        " merchant:" + parts[1] +
                        " accounts:" + map.get(key));
            }
        }

        return result;
    }

    // Demo
    public static void main(String[] args) {

        long now = System.currentTimeMillis();

        List<Transaction> list = Arrays.asList(
                new Transaction(1, 500, "StoreA", "acc1", now),
                new Transaction(2, 300, "StoreB", "acc2", now + 900000),
                new Transaction(3, 200, "StoreC", "acc3", now + 1800000),
                new Transaction(4, 500, "StoreA", "acc4", now + 2000000)
        );

        TransactionAnalyzer analyzer = new TransactionAnalyzer(list);

        System.out.println("Two-Sum:");
        for (int[] pair : analyzer.findTwoSum(500)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nTwo-Sum within 1 hour:");
        for (int[] pair : analyzer.findTwoSumWithTimeWindow(500, 3600000)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nK-Sum (k=3, target=1000):");
        System.out.println(analyzer.findKSum(3, 1000));

        System.out.println("\nDuplicate Transactions:");
        System.out.println(analyzer.detectDuplicates());
    }
}