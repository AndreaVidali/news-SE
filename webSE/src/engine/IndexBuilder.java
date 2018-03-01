package engine;

import java.io.*;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import static org.apache.lucene.index.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS;


public class IndexBuilder {

    private static final String INDEX_DIRECTORY = "C:/Users/kivid/news-SE/index";

    private void buildIndex() {

        IndexWriter w = null;

        // Specify the analyzer for tokenizing text. The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // create the index
        Directory index = null;
        try {
            index = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        } catch (IOException e) { System.out.println(e.getMessage()); }
        //Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Similarity similarity = new ClassicSimilarity();
        config.setSimilarity(similarity);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        try {
            w = new IndexWriter(index, config);

        }catch (IOException e) {
            System.out.println(e.getMessage());
        }


        // read the news file
        FileReader fr = null;
        try {
            fr = new FileReader("documents/news-bbcworld.txt");
        }catch (FileNotFoundException f){
            System.out.println(f.getMessage());
        }
        BufferedReader buf = new BufferedReader(fr);
        String lineJustFetched;
        String[] newsArray;

        while(true){
            try {
                lineJustFetched = buf.readLine();
                if(lineJustFetched == null){
                    break;
                }else{
                    newsArray = lineJustFetched.split("\t");
                    addDoc(w, newsArray[1], newsArray[2], newsArray[3], newsArray[4]);
                }
            }catch (IOException e) { System.out.println(e.getMessage()); }
        }

        try {
            buf.close();
            w.close();
        } catch (IOException e) { System.out.println(e.getMessage()); }
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

    public static void main(String[] args) throws Exception {
        IndexBuilder builder = new IndexBuilder();
        builder.buildIndex();
    }

}