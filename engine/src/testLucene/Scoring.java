package testLucene;

import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Vector;

public class Scoring {

    public static void indexing1() throws IOException {
        Directory dir = FSDirectory.open(new File("C:\\Users\\Katerina\\workspace\\IRlabs\\lab3_2index1").toPath());
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Similarity similarity = new BM25Similarity(); //Indexing with BM25
        config.setSimilarity(similarity);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(dir, config);

        //define the structure of the document
        Field isbn = new StringField("isbn13", "", Field.Store.YES);
        Field title = new TextField("title", "", Field.Store.YES);
        Field author = new TextField("author", "", Field.Store.YES);
        Field description = new TextField("description", "", Field.Store.YES);
        Field numberOfPages = new IntPoint("numberOfPages", 0);
        Field numberOfPagesStored = new StoredField("numberOfPages", 0);
        Field price = new DoublePoint("price", 00.00);
        Field priceStored = new StoredField("price", 00.00);

        //read the collection of books - each book info in a line tab format
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Katerina\\workspace\\IRlabs\\books.txt"));
                String line;
        while((line = br.readLine()) != null){
            Document book = new Document();
            String[] tokens = line.split("\t");
            isbn.setStringValue(tokens[0]);
            title.setStringValue(tokens[1]);
            author.setStringValue(tokens[2]);
            description.setStringValue(tokens[3]);
            numberOfPages.setIntValue(Integer.valueOf(tokens[4]));
            numberOfPagesStored.setIntValue(Integer.valueOf(tokens[4]));
            price.setDoubleValue(Double.valueOf(tokens[5]));
            priceStored.setDoubleValue(Double.valueOf(tokens[5]));

            book.add(isbn);
            book.add(title);
            book.add(author);
            book.add(description);
            book.add(numberOfPages);
            book.add(numberOfPagesStored);
            book.add(price);
            book.add(priceStored);

            iwriter.addDocument(book);
            }

        iwriter.close();
        br.close();

        }

    public static void indexing2() throws IOException{

        Directory dir = FSDirectory.open(new File("C:\\Users\\Katerina\\workspace\\IRlabs\\lab3_2index2").toPath());
                Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        Similarity similarity = new ClassicSimilarity(); //original Lucene scoring function based on the highly optimized Vector Space Model
        config.setSimilarity(similarity);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        IndexWriter iwriter = new IndexWriter(dir, config);

        //define the structure of the document
        Field isbn = new StringField("isbn13", "", Field.Store.YES);
        Field title = new TextField("title", "", Field.Store.YES);
        Field author = new TextField("author", "", Field.Store.YES);
        Field description = new TextField("description", "", Field.Store.YES);
        Field numberOfPages = new IntPoint("numberOfPages", 0);
        Field numberOfPagesStored = new StoredField("numberOfPages", 0);
        Field price = new DoublePoint("price", 00.00);
        Field priceStored = new StoredField("price", 00.00);

        //read the collection of books - each book info in a line tab format
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Katerina\\workspace\\IRlabs\\books.txt"));
                String line;

        while((line = br.readLine()) != null){
            Document book = new Document();
            String[] tokens = line.split("\t");
            isbn.setStringValue(tokens[0]);
            title.setStringValue(tokens[1]);
            author.setStringValue(tokens[2]);
            description.setStringValue(tokens[3]);
            numberOfPages.setIntValue(Integer.valueOf(tokens[4]));
            numberOfPagesStored.setIntValue(Integer.valueOf(tokens[4]));
            price.setDoubleValue(Double.valueOf(tokens[5]));
            priceStored.setDoubleValue(Double.valueOf(tokens[5]));

            book.add(isbn);
            book.add(title);
            book.add(author);
            book.add(description);
            book.add(numberOfPages);
            book.add(numberOfPagesStored);
            book.add(price);
            book.add(priceStored);

            iwriter.addDocument(book);
            }

        iwriter.close();
        br.close();

    }


    public static void searching() throws ParseException, IOException{

        ArrayList<Query> queries = new ArrayList<Query>();

        //fuzzy query
        Query fq = new FuzzyQuery(new Term("description", "winter"), 2); //2 max edit distance
        // description
        queries.add(fq);

        //phrase query
        Query phq = new PhraseQuery("description", "music", "icon");
        queries.add(phq);

        //path to the lucene index1
        File index1 = new File("C:\\Users\\Katerina\\workspace\\IRlabs\\lab3_2index1");
        Path path1 = Paths.get(index1.getCanonicalPath());
        Directory dir1 = FSDirectory.open(path1);

        //path to the lucene index2
        File index2 = new File("C:\\Users\\Katerina\\workspace\\IRlabs\\lab3_2index2");
        Path path2 = Paths.get(index2.getCanonicalPath());
        Directory dir2 = FSDirectory.open(path1);

        //initialize the index reader
        DirectoryReader reader1 = DirectoryReader.open(dir1);
        //initialize the index reader
        DirectoryReader reader2 = DirectoryReader.open(dir2);

        IndexSearcher bookSearcher1 = new IndexSearcher(reader1);
        bookSearcher1.setSimilarity(new BM25Similarity());

        IndexSearcher bookSearcher2 = new IndexSearcher(reader2);
        bookSearcher2.setSimilarity(new ClassicSimilarity());

        for(Query q : queries){

            //top 100 results
            TopDocs topdocs1 = bookSearcher1.search(q, 100); //array of arrays
            TopDocs topdocs2 = bookSearcher2.search(q, 100); //array of arrays

            ScoreDoc[] resultList1 = topdocs1.scoreDocs; //array of doc ids and their ranking
            ScoreDoc[] resultList2 = topdocs2.scoreDocs; //array of doc ids and their ranking

            System.out.println("BM25Similarity results: " + topdocs1.totalHits);
            for(int i = 0; i<resultList1.length; i++){
                Document book = bookSearcher1.doc(resultList1[i].doc);
                float score = resultList1[i].score;
                String booktitle = "";
                String bookauthor = "";
                if(book.getField("title") != null){
                    booktitle = book.getField("title").stringValue();
                    }
                if(book.getField("author") != null){
                    bookauthor = book.getField("author").stringValue();
                    }
                System.out.println("author: " + bookauthor + ". title: " + booktitle +
                        " Score: " + score);
                }

            System.out.println("ClassicSimilarity results: " + topdocs2.totalHits);
            for(int i = 0; i<resultList2.length; i++){
                Document book = bookSearcher2.doc(resultList2[i].doc);
                float score = resultList2[i].score;
                String booktitle = "";
                String bookauthor = "";
                if(book.getField("title") != null){
                    booktitle = book.getField("title").stringValue();
                    }
                if(book.getField("author") != null){
                    bookauthor = book.getField("author").stringValue();
                    }
                System.out.println("author: " + bookauthor + ". title: " + booktitle +
                        " Score: " + score);
                }

            }
        }

        public static void main(String[] args) throws IOException, ParseException{
        indexing1();
        indexing2();
        searching();
        }
}

