
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;


// ***in the index file***
    public void computeTFIDF() {
        for (Map.Entry<String, DictEntry> entry : index.entrySet()) {
            String term = entry.getKey();
            DictEntry dictEntry = entry.getValue();
            // IDF Calculation
            double idf = Math.log10((double) N / dictEntry.doc_freq);

            Posting posting = dictEntry.pList;
            while (posting != null) {
                int tf = posting.dtf;

                // TF weight calculation
                double tf_weight = 1 + Math.log10(tf);

                // TF-IDF calculation
                double tfidf = tf_weight * idf;

                System.out.println("Term: " + term + ", DocID: " + posting.docId + ", TF-IDF: " + tfidf);

                posting = posting.next;
            }
        }
    }

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


    public static void main(String[] args) {
        WikipediaCrawling crawler = new WikipediaCrawling();
        crawler.crawl("https://en.wikipedia.org/wiki/List_of_pharaohs");
        crawler.crawl("https://en.wikipedia.org/wiki/Pharaoh");

        // crawler.printDocumentMapping();
        crawler.printInvertedIndex();
        
        // TF-IDF Part
        Index5 index = new Index5();
        String[] files = {"file1.txt", "file2.txt", "file3.txt"};

        index.buildIndex(files);
        index.setN(files.length);

        index.computeTFIDF();
    }
}
