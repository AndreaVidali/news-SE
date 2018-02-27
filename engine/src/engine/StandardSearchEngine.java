package engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.codecs.TermVectorsReader;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.codecs.*;
import org.apache.lucene.util.BytesRef;

public class StandardSearchEngine {

    public static void main(String[] args) throws IOException, ParseException {

        // http://www.lucenetutorial.com/lucene-in-5-minutes.html

        // Specify the analyzer for tokenizing text. The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // create the index
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Similarity similarity = new ClassicSimilarity();
        config.setSimilarity(similarity);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter w = new IndexWriter(index, config);

        // create the collection of terms
        Map<String, Integer> termCollection = new HashMap<String, Integer>();

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

        // query
        String querystr = args.length > 0 ? args[0] : "asteroid";

        // the "newsText" arg specifies the default field to use when no field is explicitly specified in the query.
        Query q = new QueryParser("newsText", analyzer).parse(querystr);

        // search
        int hitsPerPage = 20;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;


        // display results
        System.out.println("Found " + hits.length + " hits.");
        for (int i=0; i<hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            // lista testo doc
            if(i == 0) {
                List<String> docText = getTokenizedText(d, "newsText");
                System.out.println(docText);

                //for(term : docText)

                Collections.sort(docText);
                System.out.println(docText);

                Terms docVector = reader.getTermVector(docId, "newsText");
                System.out.println(docVector);

            }

            System.out.println((i + 1) + ". " + "Score: " + hits[i].score + " || " + d.get("createdAt") + "\t" + d.get("newsLink") + "\t" + d.get("tweet"));
        }

        // reader can only be closed when there is no need to access the documents any more.
        reader.close();
    }

    private static void addDoc(IndexWriter w, String createdAt, String newsLink, String tweet, String newsText) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("createdAt", createdAt, Field.Store.YES));
        doc.add(new StringField("newsLink", newsLink, Field.Store.YES));
        doc.add(new StringField("tweet", tweet, Field.Store.YES));

        FieldType vectorsType = new FieldType(TextField.TYPE_STORED);
        vectorsType.setStoreTermVectors(true);
        vectorsType.setStoreTermVectorPositions(true);
        vectorsType.setStoreTermVectorOffsets(true);

        doc.add(new Field("newsText", newsText, vectorsType));
        w.addDocument(doc);
    }

    private static List<String> getTokenizedText(Document document, String field) throws IOException {
        TokenStream stream = new EnglishAnalyzer().tokenStream(field, new StringReader(document.getField(field).stringValue()));
        List<String> result = new ArrayList<>();
        try {
            stream.reset();
            while (stream.incrementToken()) {
                result.add(stream.getAttribute(CharTermAttribute.class).toString());
            }
        } catch (IOException e) {
            // not thrown b/c we're using a string reader...
        }
        return result;
    }


}