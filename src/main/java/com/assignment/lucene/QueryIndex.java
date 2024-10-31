package com.assignment.lucene;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class QueryIndex {

        public static void main(String[] args) throws IOException, ParseException {
        String searchMode = args.length > 0 ? args[0] : "batch";  // "interactive" or "batch"
        String similarityType = args.length > 1 ? args[1] : "vsm";  // "vsm" or "bm25"
        String analyzerType = args.length > 2 ? args[2] : "standard";  // "standard" or "english" or "whitespace"
        
        String index_directory = "./" + analyzerType + "_index";

        Directory directory = FSDirectory.open(Paths.get(index_directory)); // Access index directory
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        // Choose the relevant analyzer according to argument passed
        Analyzer analyzer = new StandardAnalyzer();
        if(analyzerType.equalsIgnoreCase("whitespace")){
			analyzer = new WhitespaceAnalyzer(); 
		}
		else if(analyzerType.equalsIgnoreCase("english")){
			analyzer = new EnglishAnalyzer();
		}

        if ("bm25".equalsIgnoreCase(similarityType)) {
            isearcher.setSimilarity(new BM25Similarity());
            System.out.println("Using BM25Similarity for searching");
        } 
        else if ("boolean".equalsIgnoreCase(similarityType)) {
            isearcher.setSimilarity(new BooleanSimilarity());
            System.out.println("Using Boolean Similarity for searching");
        } 
        else if ("lmd".equalsIgnoreCase(similarityType)) {
            isearcher.setSimilarity(new LMDirichletSimilarity());
            System.out.println("Using LMDirichlet Similarity for searching");
        } 
        else {
            isearcher.setSimilarity(new ClassicSimilarity());
            System.out.println("Using Classic (VSM) Similarity for searching");
        }

        // run relevant searching mode (i.e: batch vs interactive)
        if ("batch".equalsIgnoreCase(searchMode)) {
            runBatchSearch(analyzer, isearcher, analyzerType, similarityType);
        } else {
            runInteractiveSearch(analyzer, isearcher);
        }

        // Close resources
        ireader.close();
        directory.close();
    }

    private static void runInteractiveSearch(Analyzer analyzer, IndexSearcher isearcher) throws IOException, ParseException {
        Scanner scanner = new Scanner(System.in);
        QueryParser parser = new QueryParser("Abstract", analyzer);

        System.out.println("Enter your search query (type 'exit' to quit):");

        while (true) {
            System.out.print("> ");
            String searchTerm = scanner.nextLine();

            if (searchTerm.equalsIgnoreCase("exit")) {
                break;
            }

            // Parse the query and search the index
            Query query = parser.parse(searchTerm);
            TopDocs topDocs = isearcher.search(query, 10);  // Limit to top 10 results

            System.out.println("Found " + topDocs.totalHits + " results.");

            // Display results
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = isearcher.doc(scoreDoc.doc);
                System.out.println("ID: " + doc.get("ID"));
                System.out.println("Title: " + doc.get("Title"));
                System.out.println("Abstract: " + doc.get("Abstract"));
                System.out.println("------------------------------");
            }
        }

        scanner.close();
    }


    public static void runBatchSearch(Analyzer analyzer, IndexSearcher isearcher, String analyzerType, String similarityType) throws IOException, ParseException {
        QueryParser parser = new QueryParser("Abstract", analyzer);
        
        String queryFilePath = "./src/resources/cran.qry";

        BufferedReader queryReader = new BufferedReader(new FileReader(queryFilePath));

        String results_path = "./results/" + analyzerType + "_" + similarityType + "_results.txt";
        
        // Prepare to write results to file for TREC Eval
        BufferedWriter resultsWriter = Files.newBufferedWriter(
            Paths.get(results_path), 
            StandardOpenOption.CREATE, 
            StandardOpenOption.TRUNCATE_EXISTING
        );


        String line;
        StringBuilder queryText = new StringBuilder();
        int queryId = 0;
        int queryNum = 0;

        while ((line = queryReader.readLine()) != null) {
            
            line = line.trim();

            if (line.startsWith(".I")) {
                // If we encounter a new query number and there is an existing query to process
                if (queryText.length() > 0) {
                    queryNum++;
                    // System.out.println("Query ID: " + queryId + " Query Num: " + queryNum);

                    // Process the previous query before resetting
                    processQuery(queryId, queryText.toString(), parser, isearcher, resultsWriter, queryNum);
                    queryText.setLength(0); // Clear the previous query
                }
                // Extract query ID from ".I <number>"
                queryId = Integer.parseInt(line.split(" ")[1]);
            } else if (line.startsWith(".W")) {
                // Start of a query text, we skip the line since it just indicates the query content follows
                continue;
            } else {
                // Append the query content
                queryText.append(line).append(" ");
            }
        }

        // Process the last query
        if (queryText.length() > 0) {
            queryNum++;
            processQuery(queryId, queryText.toString(), parser, isearcher, resultsWriter, queryNum);
        }

        // Close readers/writers
        queryReader.close();
        resultsWriter.close();
    }

    private static void processQuery(int queryId, String queryText, QueryParser parser, IndexSearcher isearcher, BufferedWriter resultsWriter, int queryNum) throws IOException, ParseException {
        //System.out.println("Processing Query " + queryId + ": " + queryText);
        String escapedQueryText = QueryParser.escape(queryText); // Escape the query text to handle special characters like ? or *

        Query query = parser.parse(escapedQueryText);
        TopDocs topDocs = isearcher.search(query, 50); 

        // Write results in TREC Eval format:
        // <query_id> Q0 <doc_id> <rank> <score> <run_name>
        int rank = 1;
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = isearcher.doc(scoreDoc.doc);
            String docId = doc.get("ID");
            
            String resultLine = String.format("%d Q0 %s %d %f lucene-v1\n", queryNum, docId, rank, scoreDoc.score);
            //System.out.println(resultLine);
            resultsWriter.write(resultLine);

            rank++;
        }
    }
    
    
}

