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
import org.apache.commons.lang3.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebServlet(name = "Searcher")
public class Searcher extends HttpServlet {

    // Index directory
    private static final String LUCENE_INDEX_DIRECTORY = "C:/Users/kivid/news-SE/index";

    /*public Searcher() {
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
        pw.println("<!doctype html>");
        pw.println("<HTML lang=\"en\">");
        pw.println("<HEAD>");
        pw.println("<meta charset=\"utf-8\">");
        pw.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">");
        pw.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
        pw.println("<TITLE>Personalized SE for News</TITLE></HEAD>");
        pw.println("<BODY>");

        //print the HTML form to search
        pw.println("<FORM ACTION=\"Searcher\" METHOD=\"POST\">");

        /*pw.println("<legend>User selection</legend>");
        pw.println("<select name=\"profile\" id=\"profile\" >");
        pw.println("<option value=\"p1\">"+profile1+"</option>"); //selected="selected"
        pw.println("<option value=\"p2\">"+profile2+"</option>");
        pw.println("<option value=\"p3\">"+profile3+"</option>");
        pw.println("<option value=\"p4\">"+profile4+"</option>");
        pw.println("<option value=\"p5\">"+profile5+"</option>");
        pw.println("</select><br><br>");

        pw.println("<INPUT TYPE=\"SUBMIT\" value=\"Load user interests\"><br><br>");*/


        pw.println("<legend>Interest selection</legend>");
        pw.println("<select name=\"interest\" id=\"interest\" >");
        String userProfile = request.getParameter("profile");

        if("p1".equals(userProfile)) {
            pw.println("<option value=\"music\" selected=\"selected\">Music</option>");
            pw.println("<option value=\"football\">Football</option>");
            pw.println("<option value=\"gun\">Guns</option>");
        }
        if("p2".equals(userProfile)) {
            pw.println("<option value=\"ship\" selected=\"selected\">Ships</option>");
            pw.println("<option value=\"italy\">Italy</option>");
            pw.println("<option value=\"health\">Health</option>");
        }
        if("p3".equals(userProfile)) {
            pw.println("<option value=\"computer\" selected=\"selected\">Computer</option>");
            pw.println("<option value=\"military\">Military</option>");
            pw.println("<option value=\"tsunami\">Tsunami</option>");
        }
        if("p4".equals(userProfile)) {
            pw.println("<option value=\"brexit\" selected=\"selected\">Brexit</option>");
            pw.println("<option value=\"olympics\">Olympics</option>");
            pw.println("<option value=\"train\">Trains</option>");
        }
        if("p5".equals(userProfile)) {
            pw.println("<option value=\"fitness\" selected=\"selected\">Fitness</option>");
            pw.println("<option value=\"depression\">Depression</option>");
            pw.println("<option value=\"mars\">Mars</option>");
        }
        pw.println("</select><br>");

        pw.println("<input type=\"hidden\" name=\"profile\" value="+userProfile+" />");

        pw.println("<fieldset>");
        pw.println("<input type=\"radio\" name=\"mode\" onclick = \\\"getAnswer('st') value=\"st\" checked=\"checked\" />Search Engine standard <br>");
        pw.println("<input type=\"radio\" name=\"mode\" onclick = \\\"getAnswer('pz') value=\"pz\" />Search Engine personalized");
        pw.println("</fieldset>");

//        pw.println("<center><TABLE BORDER=\"0\">");
//        pw.println("<TR>");
//        pw.println("<TD><INPUT NAME=\"query\" TYPE=\"TEXT\"></TD><TD><button class=\"btn btn-primary\" type=\"submit\" value=\"Search\">Search now</button></TD>");
//        pw.println("</TR>");
//        pw.println("</TABLE></center><br><br>");

        pw.println("<br><br><center><div class=\"container\"><div class=\"input-group mb-3\">" +
                "  <input type=\"text\" name=\"query\" class=\"form-control\" placeholder=\"What are you looking for?\" aria-label=\"Recipient's username\" aria-describedby=\"basic-addon2\">" +
                "  <div class=\"input-group-append\">" +
                "    <button class=\"btn btn-primary\" type=\"submit\" value=\"Search\">Search</button>" +
                "  </div>" +
                "</div></div></center><br><br>");

        //check whether this page is opened for first time or after
        //submitting search
        if(request.getParameter("query")==null ||
                request.getParameter("query").equals("")){
            pw.println("<script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\" integrity=\"sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN\" crossorigin=\"anonymous\"></script>\n");
            pw.println("<script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js\" integrity=\"sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q\" crossorigin=\"anonymous\"></script>\n");
            pw.println("<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js\" integrity=\"sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl\" crossorigin=\"anonymous\"></script>\n");

            pw.println("</BODY>");
            pw.println("</HTML>");
            return;
        }


        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(LUCENE_INDEX_DIRECTORY)));
        Similarity similarity = new ClassicSimilarity();
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);
        StandardAnalyzer analyzer = new StandardAnalyzer();

        //String profile = request.getParameter("profile");
        //String topic


        // user-document retrieval
        String queryTopic = request.getParameter("interest");
        Query qUser = null;
        try {
            qUser = new QueryParser("newsText", analyzer).parse(queryTopic);
        }catch (ParseException p) { System.out.println(p.getMessage()); }
        int nResults = 3;
        TopDocs docUser = searcher.search(qUser, nResults);
        ScoreDoc[] hitsUser = docUser.scoreDocs;

        //System.out.println("--------- USER DOCS");
        List<String> termsUserDoc = new ArrayList<>();
        List<Integer> docIdToRemove = new ArrayList<>();
        for (ScoreDoc aHitsUser : hitsUser) {
            int docUserId = aHitsUser.doc;
            docIdToRemove.add(docUserId);
            //Document dUser = searcher.doc(docUserId);
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
        String mode = request.getParameter("mode");
        TFIDFSimilarity tfidfSIM = new ClassicSimilarity();
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        //System.out.println("---------- STANDARD SE");
        //System.out.println("Found " + hits.length + " hits || Query: " + querystr);

        // vector containing score for personalization
        Map<Integer, Double> combinedScoreContainer = new HashMap<>();



        if (hits.length > 0) {
//            pw.println("<P><TABLE BORDER=\0\">");
//            pw.println("<TR><TD>News Link</TD><TD>Tweet</TD><TD>" +
//                    "Created At</TD></TR>");

            // explore results
            for (ScoreDoc hit : hits) {

                int docId = hit.doc;
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
                double alpha = 0.20;
                double combinedScore = alpha * scorePersonal + (1 - alpha) * hit.score;
                combinedScoreContainer.put(docId, combinedScore);

                //System.out.println((i + 1) + ". " + "Score: " + hits[i].score + " || Pers.score: " + scorePersonal + " || Comb.score: " + combinedScore + " || " + d.get("createdAt") + "\t" + d.get("newsLink") + "\t" + d.get("tweet"));

                if ("st".equals(mode)) {

                    String tweetText = d.getField("tweet").stringValue();
                    String tweetTextCleaned = removeUrl(tweetText);

                    String newsText = d.getField("newsText").stringValue();

//                    pw.println("<TR><TD><a href=\"" + d.getField("newsLink").stringValue() +
//                            "\">" + d.getField("newsLink").stringValue() + "</a></TD><TD>" + tweetTextCleaned +
//                            "</TD><TD>" + d.getField("createdAt").stringValue() +
//                            "</TR>");

//                    pw.println("<h3>" + tweetTextCleaned + "</h3>");
//                    pw.println("<p>");
//                    pw.println(StringUtils.substring(newsText, 0, 300) + " ... <a href=\"" + d.getField("newsLink").stringValue() + "\">" + d.getField("newsLink").stringValue()+ "</a>");
//                    pw.println("</p>");

                    pw.println("<div class=\"container\"><blockquote class=\"blockquote\">");
                    pw.println("<h4><a href=\"" + d.getField("newsLink").stringValue() + "\">" + tweetTextCleaned + "</a></h4>");
                    pw.println("<footer class=\"blockquote-footer\">Tweeted at <cite title = \"Source Title\">" + d.getField("createdAt").stringValue() + "</cite></footer>");
                    pw.println("</blockquote>");
                    pw.println("<p>");
                    pw.println(StringUtils.substring(newsText, 0, 300) + " ...");
                    pw.println("</p></div><br>");

                }
            }

            if("pz".equals(mode)) {
                for (Integer docIdRemove : docIdToRemove) {
                    combinedScoreContainer.remove(docIdRemove);
                }
                printSortedResults(combinedScoreContainer, searcher, hits.length, pw);
            }

//            pw.println("</TABLE>");


        } else {
            pw.println("<P>No records found");
        }
        reader.close();

        pw.println("<script src=\"https://code.jquery.com/jquery-3.2.1.slim.min.js\" integrity=\"sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN\" crossorigin=\"anonymous\"></script>\n");
        pw.println("<script src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js\" integrity=\"sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q\" crossorigin=\"anonymous\"></script>\n");
        pw.println("<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js\" integrity=\"sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl\" crossorigin=\"anonymous\"></script>\n");

        pw.println("</BODY>");
        pw.println("</HTML>");

    }

    private static void printSortedResults(Map<Integer, Double> combinedResults, IndexSearcher searcher, int nHits, PrintWriter pw) {

        Collection valueSet = combinedResults.values();
        Iterator iterator = valueSet.iterator();
        ArrayList<Double> valueList = new ArrayList<>();

        while (iterator.hasNext()) {
            Object valueObj = iterator.next();
            double value = (Double) valueObj;
            valueList.add(value);
        }

        Collections.sort(valueList);

        //System.out.println("--------- PERSONALIZED SE");

        for (int i = valueList.size()-1; i>=0; --i) {
            Set docIdSet = getKeysByValue(combinedResults, valueList.get(i));
            Object[] actualKey = docIdSet.toArray();
            int docId = (Integer) actualKey[0];
            try {
                Document d = searcher.doc(docId);
                //System.out.println((nHits - i) + ". " + "Score (combined): " + valueList.get(i) + " || " + d.get("createdAt") + "\t" + d.get("newsLink") + "\t" + d.get("tweet"));

                String tweetText = d.getField("tweet").stringValue();
                String tweetTextCleaned = removeUrl(tweetText);

                pw.println("<TR><TD><a href=\"" + d.getField("newsLink").stringValue() +
                        "\">" + d.getField("newsLink").stringValue() + "</a></TD><TD>" + tweetTextCleaned +
                        "</TD><TD>" + d.getField("createdAt").stringValue() +
                        "</TR>");
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

    private static String removeUrl(String commentstr)
    {
        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(commentstr);
        int i = 0;
        while (m.find()) {
            commentstr = commentstr.replaceAll(m.group(i),"").trim();
            i++;
        }
        return commentstr;
    }


}
