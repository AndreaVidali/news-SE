package testLucene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class test {

    public static void main(String[] args) {

        List<String> l1 = new ArrayList<>(3);

        l1.add("miao");
        l1.add("ciao");
        l1.add("bau");

        for (Object term : l1) {
            System.out.println(term);
        }
/*
        Iterator itr = l1.iterator();

        while(itr.hasNext()){
            String term = (String)itr.next();
            System.out.println(term);
        }*/

    }

}
