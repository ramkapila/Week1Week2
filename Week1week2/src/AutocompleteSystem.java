import java.util.*;

public class AutocompleteSystem {

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> queryFreq = new HashMap<>();
    }

    private TrieNode root;

    // Global query frequency storage
    private Map<String, Integer> globalFrequency;

    public AutocompleteSystem() {
        root = new TrieNode();
        globalFrequency = new HashMap<>();
    }

    // Insert query into Trie
    public void insert(String query, int frequency) {

        globalFrequency.put(query, frequency);

        TrieNode node = root;

        for (char c : query.toCharArray()) {

            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            node.queryFreq.put(query, frequency);
        }
    }

    // Update frequency when user searches
    public void updateFrequency(String query) {

        int newFreq = globalFrequency.getOrDefault(query, 0) + 1;
        globalFrequency.put(query, newFreq);

        TrieNode node = root;

        for (char c : query.toCharArray()) {

            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            node.queryFreq.put(query, newFreq);
        }
    }

    // Search top 10 suggestions for prefix
    public List<String> search(String prefix) {

        TrieNode node = root;

        for (char c : prefix.toCharArray()) {

            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }

            node = node.children.get(c);
        }

        // Min heap for top 10 results
        PriorityQueue<Map.Entry<String, Integer>> heap =
                new PriorityQueue<>(10, Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : node.queryFreq.entrySet()) {

            heap.offer(entry);

            if (heap.size() > 10) {
                heap.poll();
            }
        }

        List<String> result = new ArrayList<>();

        while (!heap.isEmpty()) {
            result.add(heap.poll().getKey());
        }

        Collections.reverse(result);

        return result;
    }

    // Demo
    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        system.insert("java tutorial", 1234567);
        system.insert("javascript", 987654);
        system.insert("java download", 456789);
        system.insert("java 21 features", 10);

        System.out.println("Search results for 'jav':");

        List<String> suggestions = system.search("jav");

        for (String s : suggestions) {
            System.out.println(s);
        }

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println("\nUpdated frequency for: java 21 features");
    }
}