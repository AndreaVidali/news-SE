package testLucene;

import org.apache.commons.math3.linear.RealVector;

public class oldLuceneSE {

    /*

    //convert the term-frequencies extracted to a real vector
    RealVector toRealVector(Map<String, Double> map) {
        RealVector vector = new ArrayRealVector(terms.size());
        int i = 0;
        for (String term : terms) {
            double value = map.containsKey(term) ? map.get(term) : 0.0;
            vector.setEntry(i++, value);
        }
        return (RealVector) vector.mapDivide(vector.getL1Norm());
    }



    //a method to get the term frequencies from a document
    Map<String, Double> getTermFrequencies(IndexReader reader, int docId) {
        try {
            Terms vector = reader.getTermVector(docId, CONTENT);
            TermsEnum termsEnum = null;
            termsEnum = vector.iterator();
            Map<String, Double> frequencies = new HashMap<>();
            BytesRef text = null;
            TFIDFSimilarity tfidfSim = new DefaultSimilarity();
            boolean scannedDoc = scannedDocs.contains(docId);

            while ((text = termsEnum.next()) != null) {
                String term = text.utf8ToString();
                org.apache.lucene.index.Fields fields = reader.getTermVectors(0);
                Term termInstance = new Term("Content", term);
                long indexDf = reader.docFreq(termInstance);
                int docCount = reader.numDocs();

                //increment the term count in the terms count lookup if doc not scanned before
                if(!scannedDoc) {
                    if(termsCount.containsKey(termInstance.toString())) {
                        Integer cnt = termsCount.get(termInstance.toString());
                        cnt++;
                        termsCount.replace(termInstance.toString(), cnt);
                    } else {
                        termsCount.put(termInstance.toString(), 1);
                    }
                }

                DocsEnum docs = termsEnum.docs(MultiFields.getLiveDocs(reader),null,0);

                //calculate the TF-IDF of the term, as compared to all documents in the corpus (the Apache Lucene Index)
                double tfidf = 0.0;
                while(docs.nextDoc() != DocsEnum.NO_MORE_DOCS)  {
                    tfidf = tfidfSim.tf(docs.freq()) * tfidfSim.idf(docCount, indexDf);
                }
                frequencies.put(term, tfidf);
                scannedDocs.add(docId);
                terms.add(term);
            }
            return frequencies;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    Map<String, Double> getTermFrequencies2(IndexReader reader, int docId) {

        try {

            Terms vector = reader.getTermVector(docId, CONTENT);

            TermsEnum termsEnum = null;

            termsEnum = vector.iterator(termsEnum);

            Map<String, Double> frequencies = new HashMap<>();

            BytesRef text = null;

            TFIDFSimilarity tfidfSim = new DefaultSimilarity();

            boolean scannedDoc = scannedDocs.contains(docId);

            int docCount = reader.numDocs();



            while ((text = termsEnum.next()) != null) {

                String term = text.utf8ToString();

                Term termInstance = new Term("Content", term);

                long indexDf = reader.docFreq(termInstance);





                //increment the term count in the terms count lookup if doc not scanned before

                if(!scannedDoc) {

                    if(termsCount.containsKey(termInstance.toString())) {

                        Integer cnt = termsCount.get(termInstance.toString());

                        cnt++;

                        termsCount.replace(termInstance.toString(), cnt);

                    } else {

                        termsCount.put(termInstance.toString(), 1);

                    }

                }



                DocsEnum docs = termsEnum.docs(MultiFields.getLiveDocs(reader),null,0);

                double tfidf = 0.0;

                while(docs.nextDoc() != DocsEnum.NO_MORE_DOCS)  {

                    tfidf = tfidfSim.tf(docs.freq()) * tfidfSim.idf(docCount, indexDf);

                }





                frequencies.put(term, tfidf);

                scannedDocs.add(docId);



            }

            return frequencies;

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }





    //convert the term-frequencies extracted to a real vector

    RealVector toRealVector(Map<String, Double> map) {

        RealVector vector = new ArrayRealVector(terms.size());

        int i = 0;

        for (String term : terms) {

            double value = map.containsKey(term) ? map.get(term) : 0.0;

            vector.setEntry(i++, value);

        }

        return (RealVector) vector.mapDivide(vector.getL1Norm());

    }


*/
    public double getCosineSimilarity(RealVector v1, RealVector v2) {
        return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
    }


}
