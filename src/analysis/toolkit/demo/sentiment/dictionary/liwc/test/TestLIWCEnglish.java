/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.sentiment.dictionary.liwc.test;

import analysis.toolkit.demo.sentiment.dictionary.liwc.Dictionary;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is is used for testing the LIWC English dictionary
 * @version  1.0 07.29.2013
 * @author Xiang Ji
 * 
 */
public class TestLIWCEnglish {
    public static void main(String[] args) throws IOException {
        //File file = new File("data/LIWC2007_English080730.dic");
                File file = new File("data/LIWC2007_English080730.dic");
                
                int count = 0;
                int cover_count = 0;
                int match_count = 0;
                int non_match_count = 0;
                
		try{
                    Dictionary dic = new Dictionary(file);
                    HashMap<String, HashMap<String, String>> subject_clues = dic.parseMPQAFile("data/subjclueslen1-HLTEMNLP05.tff");
                    Map<String, Set<Integer>> dictionary = dic.getDictionary();
                    Iterator it = dictionary.entrySet().iterator();
                    outerloop:
                    while (it.hasNext()) {
                        Map.Entry pairs = (Map.Entry)it.next();
                        String word = pairs.getKey().toString();
                        Set<Integer> categories = (Set<Integer>)pairs.getValue();
                        /* 
                        //count sentiment words without "*"
                        if(categories.contains(125) && (word.indexOf("*") == -1)) {
                            //System.out.println(word + ": " + categories.toString());
                            if(subject_clues.containsKey(word)) {
                                if(subject_clues.get(word).get("priorpolarity").equals("1") == categories.contains(126))
                                    match_count++;
                                else 
                                    non_match_count++;
                            }
                        }*/
                        if(categories.contains(125)) {
                            count++;
                            Iterator it_sub = subject_clues.entrySet().iterator();
                            while (it_sub.hasNext()) {
                                Map.Entry pairs_sub = (Map.Entry)it_sub.next();
                                String word_sub = pairs_sub.getKey().toString();
                                if(word_sub.matches("^" + word.replace("*", ".*") + "$")) {
                                    //System.out.println(word_sub + ": " + subject_clues.get(word_sub).get("priorpolarity").equals("1") + "||" +word + ": " + categories.contains(126));
                                    if(subject_clues.get(word_sub).get("priorpolarity").equals("1") == categories.contains(126)) {
                                        match_count++;       
                                    } else {
                                        non_match_count++;
                                    }
                                    continue outerloop;   
                                }

                            }
                            
                        }
                    }
                }finally {
                    System.out.println(count);
                    System.out.println(match_count);
                    System.out.println(non_match_count);
                }
    }
}
