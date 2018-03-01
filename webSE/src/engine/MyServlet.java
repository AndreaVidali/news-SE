package engine;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "MyServlet")
public class MyServlet extends HttpServlet {

    private static final String LUCENE_INDEX_DIRECTORY = "C:/Users/kivid/news-SE/index";

    /*public MyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }*/

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // PrintWriter out = response.getWriter();
        // out.print("it work");
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        //print HTML header information
        pw.println("<HTML>");
        pw.println("<HEAD><TITLE>Lucene Example</TITLE></HEAD>");
        pw.println("<BODY>");

        //print the HTML form to search
        pw.println("<FORM ACTION=\"MyServlet\" METHOD=\"POST\">");
        pw.println("<TABLE BORDER=\"0\">");
        pw.println("<TR>");
        pw.println("<TD>Enter Text</TD>");
        pw.println("<TD><INPUT NAME=\"query\" TYPE=\"TEXT\"></TD>");
        pw.println("</TR>");
        pw.println("<TR>");
        pw.println("<TD COLSPAN=\"2\"><INPUT TYPE=\"SUBMIT\"></TD>");
        pw.println("</TR>");
        pw.println("</TABLE>");

        //check whether this page is opened for first time or after
        //submitting search
        if(request.getParameter("query")==null ||
                request.getParameter("query").equals("")){
            pw.println("</BODY>");
            pw.println("</HTML>");
            return;
        }

        // ---------------------------------------------------------------------------------------------------

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(LUCENE_INDEX_DIRECTORY)));
        Similarity similarity = new ClassicSimilarity();
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);
        StandardAnalyzer analyzer = new StandardAnalyzer();


        // user-document retrieval
        String queryTopic = "tax";
        Query qUser = null;
        try {
            qUser = new QueryParser("newsText", analyzer).parse(queryTopic);
        }catch (ParseException p) { System.out.println(p.getMessage()); }
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
            //System.out.println((i + 1) + ". " + "Score: " + hitsUser[i].score + " || " + dUser.get("createdAt") + "\t" + dUser.get("newsLink") + "\t" + dUser.get("tweet"));
        }

        // query
        //String querystr = "apple";
        String querystr = request.getParameter("query");
        Query q = null;
        try {
            q = new QueryParser("newsText", analyzer).parse(querystr);
        }catch (ParseException p) { System.out.println(p.getMessage()); }
        //Query q = new QueryParser("newsText", analyzer).parse(querystr);

        // search
        int hitsPerPage = 200;
        TFIDFSimilarity tfidfSIM = new ClassicSimilarity();
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        System.out.println("---------- STANDARD SE");
        System.out.println("Found " + hits.length + " hits || Query: " + querystr);

        // vector containing score for personalization
        Map<Integer, Double> combinedScoreContainer = new HashMap<>();

        // Personalization weight
        double alpha = 0.20;

        if (hits.length > 0) {
            pw.println("<P><TABLE BORDER=\1\">");
            pw.println("<TR><TD>News Link</TD><TD>Tweet</TD><TD>" +
                    "Created At</TD></TR>");

            // explore results
            for (int i = 0; i < hits.length; ++i) {

                int docId = hits[i].doc;
                Document d = searcher.doc(docId);

                Terms docVector = reader.getTermVector(docId, "newsText");
                TermsEnum itr = docVector.iterator();
                BytesRef term;

                ArrayList<Float> tfidfScores = new ArrayList<>();

                while ((term = itr.next()) != null) {

                    String termText = term.utf8ToString();
                    long termFreq = itr.totalTermFreq();
                    Query q1 = null;
                    try {
                        q1 = new QueryParser("newsText", analyzer).parse(termText);
                    } catch (ParseException p) {
                        System.out.println(p.getMessage());
                    }
                    TotalHitCountCollector collector = new TotalHitCountCollector();
                    searcher.search(q1, collector);
                    int docFreq = collector.getTotalHits();

                    if (termsUserDoc.contains(termText)) {
                        float tf = tfidfSIM.tf(termFreq);
                        float idf = tfidfSIM.idf(docFreq, reader.numDocs());
                        tfidfScores.add(tf * idf);
                    }
                }

                // combine results
                Collections.sort(tfidfScores);
                float scorePersonal = 0;
                int remaining = 150;
                for (int x = tfidfScores.size() - 1; x >= 0; --x) {
                    if (remaining != 0) {
                        scorePersonal = scorePersonal + tfidfScores.get(x);
                        remaining = remaining - 1;
                    }
                }
                double combinedScore = alpha * scorePersonal + (1 - alpha) * hits[i].score;
                combinedScoreContainer.put(docId, combinedScore);

                //System.out.println((i + 1) + ". " + "Score: " + hits[i].score + " || Pers.score: " + scorePersonal + " || Comb.score: " + combinedScore + " || " + d.get("createdAt") + "\t" + d.get("newsLink") + "\t" + d.get("tweet"));

                pw.println("<TR><TD>"+d.getField("newsLink").stringValue()+
                           "</TD><TD>"+d.getField("tweet").stringValue()+
                           "</TD><TD>"+d.getField("createdAt").stringValue()+
                           "</TR>");
            }
            pw.println("</TABLE>");
            //printSortedResults(combinedScoreContainer, searcher, hits.length);
        } else {
            pw.println("<P>No records found");
        }
        reader.close();

        // ------------------------------------------------------------------------------------------------

        pw.println("</BODY>");
        pw.println("</HTML>");

    }

    private static void printSortedResults(Map<Integer, Double> combinedResults, IndexSearcher searcher, int nHits) {

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

    private static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        return map.entrySet()
                .stream()
                .filter(entry -> Objects.equals(entry.getValue(), value))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }


}
