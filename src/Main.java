

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

class WikipediaCrawling {

    ///  final means this varriable can not change
    private static final int MAX_NUMBER_PAGES = 10;
    private Set<String> visitedLinks = new HashSet<>();
    private Queue<String> urlsToCrawling = new LinkedList<>();
    private List<String> allTokens = new ArrayList<>();


    public void crawl(String startUrl) {


        urlsToCrawling.add(startUrl);
        int id=1 ;
        while (!urlsToCrawling.isEmpty() && visitedLinks.size() < MAX_NUMBER_PAGES) {
            String currentUrl = urlsToCrawling.poll();

            if (currentUrl == null || visitedLinks.contains(currentUrl)) {
                continue;
            }

            if (!currentUrl.startsWith("https://en.wikipedia.org")) {
                continue;
            }

            try {


                Document doc = Jsoup.connect(currentUrl).get();


                System.out.println("    ✅ "+ id +" -  Visited: " + currentUrl);
                id++;

                visitedLinks.add(currentUrl);

                String text = doc.select("#mw-content-text").text();


                List<String> tokens = List.of(tokenization(text));
                allTokens.addAll(tokens);


                ///list all links in pages
                Elements linksOnPage = doc.select("a[href]");

                for (Element link : linksOnPage) {
                    String nextUrl = link.attr("abs:href");
                    if (!visitedLinks.contains(nextUrl)) {
                        urlsToCrawling.add(nextUrl);
                    }
                }

                ///  (politeness to server )
                /// politeness  in crawling — you don't overload the server.


                Thread.sleep(1000);

            } catch (IOException | InterruptedException e) {
                System.err.println("Error: " + e.getMessage());
            }
            //printing all tokens
            System.out.println("\n\n********************************************************* All tokens collected:");
            for (String token : allTokens) {
                System.out.println(token);
            }
        }
    }

    private String[] tokenization(String text){
        if (text == null) {
            return new String[0];
        }
        String[] rawTokens =  text.toLowerCase().split("\\W+");

        List<String>cleanedTokens = new ArrayList<>();
        for (String token : rawTokens){
            if (!token.isEmpty() && token.length() > 1 && token.matches("[a-z]+") && !ISStopWord(token)){
                cleanedTokens.add(token);
            }
        }
        return cleanedTokens.toArray(new String[0]);
    }

    private Boolean ISStopWord(String word){
        String[] stopWords = {"the", "is", "at", "which", "on", "and", "a", "an", "of", "in", "to", "for"};
        for (String stopWord : stopWords){
            if (stopWord.equals(word)){
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        WikipediaCrawling crawler1 = new WikipediaCrawling();
        WikipediaCrawling crawler2 = new WikipediaCrawling();

        System.out.println("\n======== Crawling from: List of Pharaohs ======== \n");
        crawler1.crawl("https://en.wikipedia.org/wiki/List_of_pharaohs");
        System.out.println("\n======== Crawling from: Pharaoh ======== \n");
        crawler2.crawl("https://en.wikipedia.org/wiki/Pharaoh");
    }
}
