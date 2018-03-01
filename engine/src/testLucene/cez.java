/*
package engine;

// TODO rimuovere documenti usati per personalizzare utente

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

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

        // initialize reader and searcher
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);

        // user-document retrieval
        String queryTopic = "tax";
        Query qUser = new QueryParser("newsText", analyzer).parse(queryTopic);
        int nResults = 3;
        TopDocs docUser = searcher.search(qUser, nResults);
        ScoreDoc[] hitsUser = docUser.scoreDocs;

        System.out.println("--------- USER DOCS");
        List<String> termsUserDoc = new ArrayList<>();
        for (int i=0; i<hitsUser.length; ++i) {
            int docUserId = hitsUser[i].doc;
            Document dUser = searcher.doc(docUserId);
            Terms docUserVector = reader.getTermVector(docUserId, "newsText");
            TermsEnum itrUs = docUserVector.iterator();
            BytesRef termUs;
            while ((termUs = itrUs.next()) != null) {
                String termTextUs = termUs.utf8ToString();
                termsUserDoc.add(termTextUs);
            }
            System.out.println((i + 1) + ". " + "Score: " + hitsUser[i].score + " || " + dUser.get("createdAt") + "\t" + dUser.get("newsLink") + "\t" + dUser.get("tweet"));
        }

        // query
        String querystr = "apple";

        // the "newsText" arg specifies the default field to use when no field is explicitly specified in the query.
        Query q = new QueryParser("newsText", analyzer).parse(querystr);

        // search
        int hitsPerPage = 200;
        TFIDFSimilarity tfidfSIM = new ClassicSimilarity();
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        System.out.println("---------- STANDARD SE");
        System.out.println("Found " + hits.length + " hits || Query: " + querystr);

        //Document dasd = searcher.doc(hitsUser[0].doc);

        // vector containing score for personalization
        //Map<Integer, Float> personalScoreContainer = new HashMap<>();
        Map<Integer, Double> combinedScoreContainer = new HashMap<>();

        // Personalization weight
        double alpha = 0.20;

        // explore results
        for (int i=0; i<hits.length; ++i) {

            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            */
/*List<String> docText = getTokenizedText(d, "newsText");
            System.out.println("Document text:"+docText);

            Collections.sort(docText);
            System.out.println("Document text sorted:"+docText);*//*


            Terms docVector = reader.getTermVector(docId, "newsText");
            TermsEnum itr = docVector.iterator();
            BytesRef term;

            //float scorePersonal = 0;
            ArrayList<Float> tfidfScores = new ArrayList<>();

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
                    tfidfScores.add(tf * idf);
                    //scorePersonal = scorePersonal + (tf * idf);
                }
            }

            //personalScoreContainer.put(docId, scorePersonal);

            // combine results
            Collections.sort(tfidfScores);
            float scorePersonal = 0;
            int remaining = 150;
            for (int x = tfidfScores.size()-1; x>=0; --x) {
                if(remaining != 0) {
                    scorePersonal = scorePersonal + tfidfScores.get(x);
                    remaining = remaining - 1;
                }
            }
            double combinedScore = alpha * scorePersonal + (1 - alpha) * hits[i].score;
            combinedScoreContainer.put(docId, combinedScore);

            System.out.println((i + 1) + ". " + "Score: " + hits[i].score + " || Pers.score: " + scorePersonal + " || Comb.score: " + combinedScore + " || " + d.get("createdAt") + "\t" + d.get("newsLink") + "\t" + d.get("tweet"));

        }

        printSortedResults(combinedScoreContainer, hits, searcher, hits.length);
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

    private static void printSortedResults(Map<Integer, Double> combinedResults, ScoreDoc[] hits, IndexSearcher searcher, int nHits) {

        Collection valueSet = combinedResults.values();
        Iterator iterator = valueSet.iterator();
        ArrayList<Double> valueList = new ArrayList<>();

        while (iterator.hasNext()) {
            Object valueObj = iterator.next();
            double value = (Double) valueObj;
            valueList.add(value);
        }

        Collections.sort(valueList);

        System.out.println("--------- PERSONALIZED SE");

        for (int i = valueList.size()-1; i>=0; --i) {
            Set docIdSet = getKeysByValue(combinedResults, valueList.get(i));
            Object[] actualKey = docIdSet.toArray();
            int docId = (Integer) actualKey[0];
            try {
                Document d = searcher.doc(docId);
                System.out.println((nHits - i) + ". " + "Score (combined): " + valueList.get(i) + " || " + d.get("createdAt") + "\t" + d.get("newsLink") + "\t" + d.get("tweet"));
            } catch (IOException e) {
                System.out.println("____Missed document " + docId + " with result = " + valueList.get(i));
            }
        }
    }

    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }




}*/
