# Java Web Crawler with TF-IDF and Cosine Similarity

## Overview
This project is a Java application that:
- Crawls web pages starting from two predefined Wikipedia URLs.
- Builds an inverted index from the text content of the crawled pages.
- Processes a user query and computes cosine similarity between the query and the documents using TF-IDF weights.
- Ranks and displays the top 10 documents based on similarity scores.

## Features
- Web crawler limited to 10 Wikipedia pages starting from:
  - https://en.wikipedia.org/wiki/List_of_pharaohs
  - https://en.wikipedia.org/wiki/Pharaoh
- Inverted index construction with normalized tokens.
- TF-IDF weighting for terms.
- Cosine similarity computation between query and documents.
- Top 10 relevant documents output.

## Technologies Used
- Java
- Jsoup (for HTML parsing)
- Collections Framework (HashMap, HashSet, LinkedList)

## Project Structure
- `WikipediaCrawling.java` - Handles fetching and parsing HTML pages, managing visited URLs, and crawling links.
- `InvertedIndex.java` - Manages the inverted index creation and storage.
- `Posting.java` - Represents a posting (document ID and term frequency).
- `TFIDFCalculator.java` - Computes TF-IDF weights.
- `SimilarityCalculator.java` - Computes cosine similarity between query and documents.
- `Main.java` - Entry point of the application (handles user input and results display).

## Design Approach
- **WikipediaCrawling:**
- Uses a queue (BFS approach) to manage pages to crawl.
- Uses a HashSet to avoid visiting duplicate URLs.
- Only follows Wikipedia links and limits to 10 documents.

- **Inverted Index:**
- Normalizes text (lowercasing, splitting by non-word characters).
- Maps terms to postings containing document IDs and term frequencies.

- **TF-IDF and Cosine Similarity:**
- Term frequency weight: `1 + log10(tf)`.
- Inverse document frequency: `idf(t) = log10(N / df(t))`.
- Cosine similarity computed using dot product and vector norms.

## Requirements
- Java 8 or higher
- Jsoup Library

## Challenges Faced
- Handling duplicate Wikipedia links.
- Normalizing text data for consistent indexing.
- Ensuring efficient TF-IDF and cosine similarity calculations for multiple documents.



## Acknowledgments
- Wikipedia.org for open content.
- Jsoup for easy HTML parsing.

---

**Note:** This project is part of Assignment 2 for [Course Name] due on 29 April 2025.
