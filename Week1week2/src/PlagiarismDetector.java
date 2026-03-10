import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PlagiarismDetector {

    private final int N_GRAM_SIZE = 5; // 5-grams
    // n-gram -> set of document IDs that contain it
    private HashMap<String, Set<String>> ngramIndex;

    public PlagiarismDetector() {
        ngramIndex = new HashMap<>();
    }

    // Break text into n-grams
    private List<String> extractNGrams(String text) {
        String[] words = text.split("\\s+");
        List<String> ngrams = new ArrayList<>();
        for (int i = 0; i <= words.length - N_GRAM_SIZE; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N_GRAM_SIZE; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }
        return ngrams;
    }

    // Index a document into the n-gram hash map
    public void indexDocument(String documentId, String content) {
        List<String> ngrams = extractNGrams(content);
        for (String ngram : ngrams) {
            ngramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(documentId);
        }
    }

    // Analyze a new document for similarity
    public Map<String, Double> analyzeDocument(String documentId, String content) {
        List<String> ngrams = extractNGrams(content);
        Map<String, Integer> matchCount = new HashMap<>();

        // Count matching n-grams
        for (String ngram : ngrams) {
            Set<String> docs = ngramIndex.get(ngram);
            if (docs != null) {
                for (String docId : docs) {
                    matchCount.put(docId, matchCount.getOrDefault(docId, 0) + 1);
                }
            }
        }

        // Calculate similarity percentages
        Map<String, Double> similarity = new HashMap<>();
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            double percent = (entry.getValue() * 100.0) / ngrams.size();
            similarity.put(entry.getKey(), percent);
        }

        return similarity;
    }

    public static void main(String[] args) throws IOException {
        PlagiarismDetector detector = new PlagiarismDetector();

        // Sample: index some documents
        String doc1 = "The quick brown fox jumps over the lazy dog";
        String doc2 = "A fast brown fox leaps over a lazy dog";
        String doc3 = "Completely different content with no similarity";

        detector.indexDocument("essay_001.txt", doc1);
        detector.indexDocument("essay_002.txt", doc2);
        detector.indexDocument("essay_003.txt", doc3);

        // Analyze a new submission
        String newDoc = "The quick brown fox leaps over the lazy dog";
        Map<String, Double> results = detector.analyzeDocument("essay_123.txt", newDoc);

        System.out.println("Analysis results for essay_123.txt:");
        results.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(e -> System.out.printf("→ Found %s similarity: %.1f%%\n", e.getKey(), e.getValue()));
    }
}