package com.assignment.lucene;

import java.io.IOException;
import java.util.*;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
 
public class CreateIndex
{
	public static void main(String[] args) throws IOException
	{
        String analyzerType = args.length > 0 ? args[0] : "standard";  // default analyzer is StandardAnalyser

		// Initialise appropriate analyzer
		Analyzer analyzer = new StandardAnalyzer();
		if(analyzerType.equalsIgnoreCase("whitespace")){
			analyzer = new WhitespaceAnalyzer(); 
		}
		else if(analyzerType.equalsIgnoreCase("english")){
			analyzer = new EnglishAnalyzer();
		}
		// set index directory according to analyzer being used
		String index_directory = "./" + analyzerType + "_index";
		Directory directory = FSDirectory.open(Paths.get(index_directory));

		ArrayList<Document> documents = new ArrayList<Document>();

		// Set up an index writer to add process and save documents to the index
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(directory, config);
		
        List<CranfieldDocument> cran_docs = CranfieldParser.parseCranfield("./src/resources/cran.all.1400");

		for (CranfieldDocument cran_doc : cran_docs)
		{
			// Create a new document and add the file's contents
			Document doc = new Document();
			doc.add(new StringField("ID", cran_doc.getID(), Field.Store.YES));
            doc.add(new TextField("Title", cran_doc.getTitle(), Field.Store.YES));
			doc.add(new TextField("Author", cran_doc.getAuthor(), Field.Store.YES));
            doc.add(new TextField("Abstract", cran_doc.getAbstractText(), Field.Store.YES));
            doc.add(new TextField("Author", cran_doc.getBibliography(), Field.Store.YES));

			documents.add(doc);
		}

		iwriter.addDocuments(documents);

		iwriter.close();
		directory.close();
	}
}

