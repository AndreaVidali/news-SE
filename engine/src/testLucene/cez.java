package testLucene;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class cez {

    private List<String> getTerms(Document document, String field) throws IOException {
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
