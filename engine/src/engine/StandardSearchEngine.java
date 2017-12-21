package engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class StandardSearchEngine {

    public static void main(String[] args) throws IOException, ParseException {

        // http://www.lucenetutorial.com/lucene-in-5-minutes.html

        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // 1. create the index
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);

        // read the news file
        BufferedReader buf = new BufferedReader(new FileReader("documents/news-bbcworld.txt"));
        String lineJustFetched;
        String[] newsArray;

        while(true){
            lineJustFetched = buf.readLine();
            if(lineJustFetched == null){
                break;
            }else{
                newsArray = lineJustFetched.split("\t");
                addDoc(w, newsArray[1], newsArray[2], newsArray[3], newsArray[4]);
            }
        }

        buf.close();
        w.close();

        // 2. query
        String querystr = args.length > 0 ? args[0] : "italy";

        // the "newsText" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser("newsText", analyzer).parse(querystr);

        // 3. search
        int hitsPerPage = 15;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for (int i=0; i<hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + "Score: " + hits[i].score + " || " + d.get("createdAt") + "\t" + d.get("newsLink") + "\t" + d.get("tweet"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
    }

    private static void addDoc(IndexWriter w, String createdAt, String newsLink, String tweet, String newsText) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("createdAt", createdAt, Field.Store.YES));
        doc.add(new StringField("newsLink", newsLink, Field.Store.YES));
        doc.add(new StringField("tweet", tweet, Field.Store.YES));
        doc.add(new TextField("newsText", newsText, Field.Store.YES));
        w.addDocument(doc);
    }
}