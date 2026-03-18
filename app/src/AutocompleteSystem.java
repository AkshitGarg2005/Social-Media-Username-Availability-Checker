import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord;
    int frequency;
    String word;
}

public class AutocompleteSystem {
    private final TrieNode root = new TrieNode();
    private final Map<String, Integer> globalFrequency = new HashMap<>();

    public void insert(String query, int freq) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfWord = true;
        node.frequency += freq;
        node.word = query;
        globalFrequency.put(query, globalFrequency.getOrDefault(query, 0) + freq);
    }

    public void updateFrequency(String query) {
        insert(query, 1);
    }

    public List<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptyList();
        }
        PriorityQueue<TrieNode> pq = new PriorityQueue<>((a, b) -> b.frequency - a.frequency);
        collect(node, pq);

        List<String> results = new ArrayList<>();
        int count = 0;
        while (!pq.isEmpty() && count < 10) {
            results.add(pq.poll().word);
            count++;
        }
        return results;
    }

    private void collect(TrieNode node, PriorityQueue<TrieNode> pq) {
        if (node.isEndOfWord) pq.add(node);
        for (TrieNode child : node.children.values()) {
            collect(child, pq);
        }
    }

    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        system.insert("java tutorial", 1234567);
        system.insert("javascript", 987654);
        system.insert("java download", 456789);
        system.insert("java 21 features", 1);

        System.out.println("search(\"jav\") →");
        for (String suggestion : system.search("jav")) {
            System.out.println(suggestion + " (" + system.globalFrequency.get(suggestion) + " searches)");
        }

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");
        System.out.println("\nAfter updates:");
        System.out.println("java 21 features (" + system.globalFrequency.get("java 21 features") + " searches)");
    }
}