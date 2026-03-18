import java.util.*;
import java.nio.file.*;
import java.io.*;

class PlagiarismDetector {
    private final int n;
    private final Map<String, Set<String>> nGramIndex = new HashMap<>();

    public PlagiarismDetector(int n) {
        this.n = n;
    }

    private List<String> tokenize(String text) {
        return Arrays.asList(text.toLowerCase().split("\\s+"));
    }

    private Set<String> extractNGrams(List<String> words) {
        Set<String> nGrams = new HashSet<>();
        for (int i = 0; i <= words.size() - n; i++) {
            String nGram = String.join(" ", words.subList(i, i + n));
            nGrams.add(nGram);
        }
        return nGrams;
    }

    public void indexDocument(String docId, String content) {
        List<String> words = tokenize(content);
        Set<String> nGrams = extractNGrams(words);
        for (String nGram : nGrams) {
            nGramIndex.computeIfAbsent(nGram, k -> new HashSet<>()).add(docId);
        }
    }

    public Map<String, Double> analyzeDocument(String docId, String content) {
        List<String> words = tokenize(content);
        Set<String> nGrams = extractNGrams(words);

        Map<String, Integer> matchCounts = new HashMap<>();
        for (String nGram : nGrams) {
            if (nGramIndex.containsKey(nGram)) {
                for (String otherDoc : nGramIndex.get(nGram)) {
                    if (!otherDoc.equals(docId)) {
                        matchCounts.put(otherDoc, matchCounts.getOrDefault(otherDoc, 0) + 1);
                    }
                }
            }
        }

        Map<String, Double> similarityScores = new HashMap<>();
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            double similarity = (entry.getValue() * 100.0) / nGrams.size();
            similarityScores.put(entry.getKey(), similarity);
        }

        return similarityScores;
    }

    public static void main(String[] args) throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector(5);

        String essay1 = "This is a sample essay with some unique content.";
        String essay2 = "This is a sample essay with some plagiarized content.";
        String essay3 = "Completely different text with no overlap.";

        detector.indexDocument("essay_089.txt", essay1);
        detector.indexDocument("essay_092.txt", essay2);

        Map<String, Double> results = detector.analyzeDocument("essay_123.txt", essay2);

        for (Map.Entry<String, Double> entry : results.entrySet()) {
            System.out.println("Found similarity with " + entry.getKey() + ": " + entry.getValue() + "%");
        }
    }
}