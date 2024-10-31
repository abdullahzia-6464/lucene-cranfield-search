package com.assignment.lucene;

import java.io.*;
import java.util.*;

class CranfieldDocument {
    private String id;
    private String title;
    private String abstractText;
    private String author;
    private String bibliography;

    // Constructors, getters, setters
    public CranfieldDocument() {}

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setBibliography(String bibliography) {
        this.bibliography = bibliography;
    }

    public String getID(){
        return this.id;
    }

    public String getTitle(){
        return this.title;
    }

    public String getAbstractText(){
        return this.abstractText;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getBibliography(){
        return this.bibliography;
    }


    @Override
    public String toString() {
        return "ID: " + id + "\nTitle: " + title + "\nAbstract: " + abstractText +
                "\nAuthor: " + author + "\nBibliography: " + bibliography + "\n";
    }
}

public class CranfieldParser {

    public static List<CranfieldDocument> parseCranfield(String filePath) throws IOException {
        List<CranfieldDocument> documents = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        CranfieldDocument currentDoc = null;
        String currentSection = null;

        while ((line = reader.readLine()) != null) { 
            line = line.trim();

            if (line.startsWith(".I")) { // Document ID
                // New document starts
                if (currentDoc != null) {
                    documents.add(currentDoc);
                }
                currentDoc = new CranfieldDocument();
                currentDoc.setId(line.substring(2).trim());  
                currentSection = null;

            } else if (line.startsWith(".T")) { // Title
                currentSection = "title";
                currentDoc.setTitle("");

            } else if (line.startsWith(".W")) { // Abstract
                currentSection = "abstract";
                currentDoc.setAbstractText("");

            } else if (line.startsWith(".A")) { // Author
                currentSection = "author";
                currentDoc.setAuthor("");

            } else if (line.startsWith(".B")) { // Bibliography
                currentSection = "bibliography";
                currentDoc.setBibliography("");

            } else {
                // Add content to the correct section
                if (currentSection != null) {
                    switch (currentSection) {
                        case "title":
                            currentDoc.setTitle(currentDoc.getTitle() + line + " ");
                            break;
                        case "abstract":
                            currentDoc.setAbstractText(currentDoc.getAbstractText() + line + " ");
                            break;
                        case "author":
                            currentDoc.setAuthor(currentDoc.getAuthor() + line + " ");
                            break;
                        case "bibliography":
                            currentDoc.setBibliography(currentDoc.getBibliography() + line + " ");
                            break;
                    }
                }
            }
        }

        // Add the last document
        if (currentDoc != null) {
            documents.add(currentDoc);
        }

        reader.close();
        return documents;
    }

    // psvm function for checking parser functionality

    // public static void main(String[] args) {
    //     try {
    //         String filePath = "/home/zia/Documents/Assignment 1/lucene-search-engine/src/resources/cran.all.1400"; // Path to the Cranfield corpus file
    //         List<CranfieldDocument> documents = parseCranfield(filePath);

    //         // Print the first document as an example
    //         if (documents.size() > 0) {
    //             System.out.println(documents.get(1).toString());
    //         }

    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}
