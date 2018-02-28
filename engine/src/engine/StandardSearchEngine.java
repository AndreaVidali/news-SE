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
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.codecs.*;
import org.apache.lucene.util.BytesRef;

import static org.apache.lucene.index.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS;

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
        String querystr = args.length > 0 ? args[0] : "football";

        // the "newsText" arg specifies the default field to use when no field is explicitly specified in the query.
        Query q = new QueryParser("newsText", analyzer).parse(querystr);

        // search
        int hitsPerPage = 20;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);
        TFIDFSimilarity tfidfSIM = new ClassicSimilarity();
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        System.out.println("Found " + hits.length + " hits || Query: " + querystr);

        // user-document retrieval
        String queryTopic = "italy";
        Query qUser = new QueryParser("newsText", analyzer).parse(queryTopic);
        int nResults = 1;
        TopDocs docUser = searcher.search(qUser, nResults);
        ScoreDoc[] hitsUser = docUser.scoreDocs;
        int docUserId = hitsUser[0].doc;
        Terms docUserVector = reader.getTermVector(docUserId, "newsText");

        Document dasd = searcher.doc(hitsUser[0].doc);

        List<String> termsUserDoc = new ArrayList<>();
        TermsEnum itrUs = docUserVector.iterator();
        BytesRef termUs;
        while ((termUs = itrUs.next()) != null) {
            String termTextUs = termUs.utf8ToString();
            termsUserDoc.add(termTextUs);
        }

        // vector containing score for personalization
        Map<Integer, Float> personalScoreContainer = new HashMap<>();
        Map<Integer, Double> combinedScoreContainer = new HashMap<>();
        double alpha = 0.3;

        // explore results
        for (int i=0; i<hits.length; ++i) {

            // data structure to manage results document
            HashMap<String,Integer> docWeights = new HashMap<>();

            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            /*List<String> docText = getTokenizedText(d, "newsText");
            System.out.println("Document text:"+docText);

            Collections.sort(docText);
            System.out.println("Document text sorted:"+docText);*/

            Terms docVector = reader.getTermVector(docId, "newsText");
            TermsEnum itr = docVector.iterator();
            BytesRef term;

            float scorePersonal = 0;

            while ((term = itr.next()) != null) {

                String termText = term.utf8ToString();
                long termFreq = itr.totalTermFreq();

                Query q1 = new QueryParser("newsText", analyzer).parse(termText);
                TotalHitCountCollector collector = new TotalHitCountCollector();
                searcher.search(q1, collector);
                int docFreq = collector.getTotalHits();

                // System.out.println("term: "+termText+" -- term frequency = "+termFreq+", document frequency = "+docFreq);

                if(termsUserDoc.contains(termText)) {
                    float tf = tfidfSIM.tf(termFreq);
                    float idf = tfidfSIM.idf(docFreq, reader.numDocs());
                    scorePersonal = scorePersonal + (tf * idf);
                }
            }

            personalScoreContainer.put(docId, scorePersonal);

            // combine results
            double combinedScore = alpha * scorePersonal + (1 - alpha) * hits[i].score;
            combinedScoreContainer.put(docId, combinedScore);

            System.out.println((i + 1) + ". " + "Score: " + hits[i].score + " || Pers.score: " + scorePersonal + " || Comb.score: " + combinedScore + " || " + d.get("createdAt") + "\t" + d.get("newsLink") + "\t" + d.get("tweet"));

        }

        printSortedResults(combinedScoreContainer, hits, searcher);

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
        vectorsType.setStoreTermVectorPayloads(true);
        vectorsType.setStored(true);
        vectorsType.setIndexOptions(DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        vectorsType.setTokenized(true);

        doc.add(new Field("newsText", newsText, vectorsType));
        w.addDocument(doc);
    }

    private static List<String> getTokenizedText(Document document, String field) throws IOException {
        TokenStream stream = new StandardAnalyzer().tokenStream(field, new StringReader(document.getField(field).stringValue()));
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

    private static float getScoreByDocID(ScoreDoc[] hits, int docID) {
        for (ScoreDoc hit : hits) {
            if (hit.doc == docID)
                return hit.score;
        }
        return 0;
    }

    private static void printSortedResults(Map<Integer, Double> combinedResults, ScoreDoc[] hits, IndexSearcher searcher) {

        Set<Integer> keySet = combinedResults.keySet();
        Iterator iterator = keySet.iterator();

        ArrayList<Integer> keyList = new ArrayList<>();

        while (iterator.hasNext()){
            //Integer key = iterator.next();
            //keyList.add(key);
        }

        Collections.sort(keyList);

    }

    public LinkedHashMap<Integer, Double> sortHashMapByValues(HashMap<Integer, Double> passedMap) {
        List<Integer> mapKeys = new ArrayList<>(passedMap.keySet());
        List<String> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<Integer, String> sortedMap =
                new LinkedHashMap<>();

        Iterator<String> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            String val = valueIt.next();
            Iterator<Integer> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Integer key = keyIt.next();
                String comp1 = passedMap.get(key);
                String comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }


}