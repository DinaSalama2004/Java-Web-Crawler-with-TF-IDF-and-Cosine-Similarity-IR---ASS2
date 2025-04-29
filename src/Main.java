
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

class WikipediaCrawling {

    private static final int MAX_NUMBER_PAGES = 10;
    private Set<String> visitedLinks = new HashSet<>();
    private Queue<String> urlsToCrawl = new LinkedList<>();
    private List<String> allTokens = new ArrayList<>();

    private Map<Integer, String> docIdToUrl = new HashMap<>();
    private Map<String, Map<Integer, Integer>> invertedIndex = new HashMap<>();
    private int docIdCounter = 1;

    public void crawl(String startUrl) {
        urlsToCrawl.add(startUrl);
        while (!urlsToCrawl.isEmpty() && visitedLinks.size() < MAX_NUMBER_PAGES) {
            String currentUrl = urlsToCrawl.poll();

            if (currentUrl == null || visitedLinks.contains(currentUrl)) continue;
            if (!currentUrl.startsWith("https://en.wikipedia.org")) continue;

            try {
                Document doc = Jsoup.connect(currentUrl).get();
                System.out.println("    ✅  DocID:  "+ docIdCounter +" -  Visited: " + currentUrl);

//                System.out.println("✅ Visited: " + currentUrl);

                visitedLinks.add(currentUrl);

                int currentDocId = docIdCounter++;
                docIdToUrl.put(currentDocId, currentUrl);


//                extracts only the words inside i
                String text = doc.select("#mw-content-text").text();
                List<String> tokens = List.of(tokenization(text));
                allTokens.addAll(tokens);

                updateInvertedIndex(currentDocId, tokens);

                Elements linksOnPage = doc.select("a[href]");
                for (Element link : linksOnPage) {
                    String nextUrl = link.attr("abs:href");
                    if (!visitedLinks.contains(nextUrl) && nextUrl.startsWith("https://en.wikipedia.org")) {
                        urlsToCrawl.add(nextUrl);
                    }
                }


                ///  (politeness to server )
               /// politeness  in crawling — you don't overload the server.


                Thread.sleep(1000);

            } catch (IOException | InterruptedException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private void updateInvertedIndex(int docId, List<String> tokens) {
        for (String token : tokens) {


//            If token is already inside invertedIndex, get its map. If not, create a new empty map (new HashMap<>) for that token.
            invertedIndex.computeIfAbsent(token, k -> new HashMap<>())
                    .merge(docId, 1, Integer::sum);
        }
    }

    private String[] tokenization(String text) {
        if (text == null) {
            return new String[0];
        }
        String[] rawTokens = text.toLowerCase().split("\\W+");
        List<String> cleanedTokens = new ArrayList<>();
        for (String token : rawTokens) {
            if (!token.isEmpty() && token.length() > 1 && token.matches("[a-z]+") && !ISStopWord(token)) {
                cleanedTokens.add(token);
            }
        }
        return cleanedTokens.toArray(new String[0]);
    }

    private Boolean ISStopWord(String word) {
        String[] stopWords = {"the", "is", "at", "which", "on", "and", "a", "an", "of", "in", "to", "for"};
        for (String stopWord : stopWords) {
            if (stopWord.equals(word)) {
                return true;
            }
        }
        return false;
    }

    public void printInvertedIndex() {
        System.out.println("\n======= Inverted Index =======\n");
        for (Map.Entry<String, Map<Integer, Integer>> entry : invertedIndex.entrySet()) {
            System.out.println("Token: " + entry.getKey());
            for (Map.Entry<Integer, Integer> posting : entry.getValue().entrySet()) {
                System.out.println("\tDocID: " + posting.getKey() + " (TF=" + posting.getValue() + ")");
            }
        }
    }

    // TF-IDF weighted
    public Map<Integer, Map<String, Double>> buildDocumentVectors(Map<String, Map<Integer, Integer>> invertedIndex, int totalDocuments) {
        Map<Integer, Map<String, Double>> docVectors = new HashMap<>();

        for (String term : invertedIndex.keySet()) {    // iterate over all terms in the inverted index.
            Map<Integer, Integer> postings = invertedIndex.get(term);   // postings get its values from the inverted index (docId, tf)
            int df = postings.size();   // number of documents where the term appears in it
            // IDF calculation
            double idf = Math.log10((double) totalDocuments / df);

            for (Map.Entry<Integer, Integer> el : postings.entrySet()) {
                int docId = el.getKey();
                int tf = el.getValue();     // get the term frequency from each term in the posting list
                // TF weight calculation
                double tfWeight = 1 + Math.log10(tf);
                // TF-IDF calculation
                double tfidf = tfWeight * idf;

                // if the docId is not in the array then creat hash map and put the docId into it
                // else (the docId exists) put the term and its TF-IDF in the array with the docId as the key
                docVectors.computeIfAbsent(docId, k -> new HashMap<>()).put(term, tfidf);
            }
        }

        return docVectors;
        // output in format
        // docId
        // (term, TF-IDF)
    }


    public static void main(String[] args) {
        WikipediaCrawling crawler = new WikipediaCrawling();
        crawler.crawl("https://en.wikipedia.org/wiki/List_of_pharaohs");
        crawler.crawl("https://en.wikipedia.org/wiki/Pharaoh");

        // crawler.printDocumentMapping();
        crawler.printInvertedIndex();
        
        // TF-IDF Part
        Map<Integer, Map<String, Double>> docVectors = crawler.buildDocumentVectors(
                crawler.invertedIndex, crawler.docIdToUrl.size());

        // print document TF-IDF
        System.out.println("\n======= Document Vectors (TF-IDF) =======\n");
        for (Map.Entry<Integer, Map<String, Double>> entry : docVectors.entrySet()) {
            int docId = entry.getKey();     // the docId from the posting list
            System.out.println("DocID: " + docId + " - URL: " + crawler.docIdToUrl.get(docId));
            for (Map.Entry<String, Double> res : entry.getValue().entrySet()) {     // (term, TF-IDF)
                System.out.printf("\tTerm: %-15s TF-IDF: %.5f%n", res.getKey(), res.getValue());
            }
        }

    }
    // Method to process the user query: tokenize it, clean it, and build a TF-IDF weighted vector
public Map<String, Double> processUserQuery(Map<String, Map<Integer, Integer>> invertedIndex, int totalDocuments) {
    
    //Take query input from user
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter your search query:");
    String userInput = scanner.nextLine();

    //Tokenize and clean the input query same as in the documents
    // - Convert to lowercase
    // - Split based on non-word characters
    String[] queryTokens = userInput.toLowerCase().split("\\W+");

    // Prepare a list to hold cleaned tokens
    List<String> cleanedTokens = new ArrayList<>();
    for (String token : queryTokens) {
        // Keep only alphabetic tokens, at least 2 characters, not a stopword
        if (!token.isEmpty() && token.length() > 1 && token.matches("[a-z]+") && !isStopWord(token)) {
            cleanedTokens.add(token);
        }
    }

    //Build the term frequency (TF) map for the query
    // - Count how many times each token appears inside the query
    Map<String, Integer> queryTF = new HashMap<>();
    for (String token : cleanedTokens) {
        queryTF.merge(token, 1, Integer::sum);
    }

    //Build the TF-IDF weighted vector for the query
    // - TF-IDF = (1 + log10(tf)) × log10(N / df)
    Map<String, Double> queryVector = new HashMap<>();

    for (Map.Entry<String, Integer> entry : queryTF.entrySet()) {
        String token = entry.getKey();
        int tf = entry.getValue();

        // Calculate TF weight
        double tfWeight = 1 + Math.log10(tf);

        // Get the document frequency (df) of the token from the inverted index
        int df = 0;
        if (invertedIndex.containsKey(token)) {
            df = invertedIndex.get(token).size(); // Number of documents containing this token
        }

        // Only calculate IDF if the token appears in at least one document
        if (df > 0) {
            double idf = Math.log10((double) totalDocuments / df); // Inverse Document Frequency
            double tfidf = tfWeight * idf; // Final TF-IDF value
            queryVector.put(token, tfidf);
        }
    }

    // Return the final query vector: token -> TF-IDF weight
    return queryVector;
}

}
