/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.sentiment.dictionary.mpqa;

import analysis.toolkit.demo.sentiment.utils.CluesCounter;
import analysis.toolkit.demo.sentiment.utils.DictionaryLabel;
import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.twitterprocess.OntologyProcess;
import analysis.toolkit.demo.utility.Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author xji
 */
public class MPQASentimentExtractor {
    /**
    * This function deletes a MPQA word if it appears in PTSD keyword list or profanity list
    * @param taxonomy_term_list the PTSD key word list
    * @param subject_clues the MPQA 
    * @param profanity_list the profanity list
    */
    public static void adjustMPQATaxonomyTerm(HashMap<String, HashMap<String, String>> subject_clues, Set<String> taxonomy_term_list, Set<String> profanity_list) {
        Iterator iter = taxonomy_term_list.iterator();
        int count = 0;
        while (iter.hasNext()) {
            String term = iter.next().toString();
            if(subject_clues.containsKey(term)) {
                subject_clues.remove(term);
            }
            if(profanity_list.contains(term)) {
                profanity_list.remove(term);
            }
        } 
    }
    
    /**
    * This function reads the a text, and output a SentimentScore object, which has a positive score and a negative score
    * @param message the text which needs to be determined the sentimental score
    * @param subjective_clues the MPQA dictionary
    * @param profanity_list the profanity list has bad words
    */
    public static SentimentSCore getSentimentMPQA(ArrayList<String> messages, HashMap<String, HashMap<String, String>> subject_clues, Set<String> profanity_list) throws FileNotFoundException {
        CluesCounter cc = new CluesCounter();
        //an object for storing final positve score and negative score
        SentimentSCore ss = new SentimentSCore();
        
        try {
            for(int i = 0; i < messages.size(); i++) {
                //normalize the sentence
                String preprocessed = SentimentAnalysisUtils.preprocessSentimentAnalysis(messages.get(i));
                //tokenize the sentence
                String[] tokens = preprocessed.split(" ");
                for (String token : tokens) {
                    DictionaryLabel dl = new DictionaryLabel();
                    dl = SentimentAnalysisUtils.lookUpDictionary(token, subject_clues);
                    if(dl.isSet) {
                        if(dl.priorPolarity == -1) { //negative
                            if(dl.type == 2) { //strongsubj
                                cc.Neg_strong++;
                                continue;
                                //System.out.println("strong_neg: " + token);
                            }
                            else if(dl.type == 1) { //weaksubj
                                cc.Neg_weak++;
                                continue;
                                //System.out.println("weak_neg: " + token);
                            }
                        } else if (dl.priorPolarity == 1) { //positive
                            if(dl.type == 2) { //strongsubj
                                cc.Pos_strong++;
                                continue;
                                //System.out.println("strong_pos: " + token);
                            }
                            else if(dl.type == 1) { //weaksubj
                                cc.Pos_weak++;
                                continue;
                                //System.out.println("weak_pos: " + token);
                            }
                        } else if (dl.priorPolarity == 0) { //neutral
                            if(dl.type == 2) { //strongsubj
                                cc.Neu_strong++;
                                continue;
                            }
                            else if(dl.type == 1) { //weaksubj
                                cc.Neu_weak++;
                                continue;
                            }
                        }
                    }
                }
                //System.out.println(i + ":" + cc.Neg_strong + "||" + cc.Neg_weak + "||" + cc.Pos_strong + "||" + cc.Pos_weak + "||" + cc.Neu_strong + "||" + cc.Neu_weak + "||" + cc.Profanity + "||" +  cc.TaxonomyTerm);
            }
        } finally {
            //System.out.println("total :" + cc.Neg_strong + "||" + cc.Neg_weak + "||" + cc.Pos_strong + "||" + cc.Pos_weak + "||" + cc.Neu_strong + "||" + cc.Neu_weak + "||" + cc.Profanity + "||" +  cc.TaxonomyTerm);
            ss.positiveScore = Utility.getNDecimals((cc.Pos_strong*2 + cc.Pos_weak)/(cc.Pos_strong*2 + cc.Pos_weak + cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3), 2);
            ss.negativeScore = Utility.getNDecimals((cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3)/(cc.Pos_strong*2 + cc.Pos_weak + cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3), 2);
            return ss;
        } 
    }
    
    /**
    * This function reads the a text, and output a SentimentScore object, which has a positive score and a negative score
    * @param message the text which needs to be determined the sentimental score
    * @param subjective_clues the MPQA dictionary
    * @param profanity_list the profanity list has bad words
    * @param taxonomy_term_list the term words of PTSD symptoms
    */
    public static SentimentSCore getSentimentMPQATaxonomyTerm(String message, HashMap<String, HashMap<String, String>> subject_clues, Set<String> profanity_list, Set<String> taxonomy_term_list) throws FileNotFoundException, IOException {
        CluesCounter cc = new CluesCounter();
        //an object for storing final positve score and negative score
        SentimentSCore ss = new SentimentSCore();
        
        try {
            //normalize the sentence
            String preprocessed = SentimentAnalysisUtils.preprocessSentimentAnalysis(message);
            //tokenize the sentence
            String[] tokens = preprocessed.split(" ");
            Iterator iter = taxonomy_term_list.iterator();
            
            while (iter.hasNext()) {
                String term = iter.next().toString();
                cc.TaxonomyTerm += StringUtils.countMatches(preprocessed, term);
            }
            
            for (String token : tokens) {
                DictionaryLabel dl = new DictionaryLabel();
                dl = SentimentAnalysisUtils.lookUpDictionary(token, subject_clues);
                if(dl.isSet) {
                    if(dl.priorPolarity == -1) { //negative
                        if(dl.type == 2) { //strongsubj
                            cc.Neg_strong++;
                            continue;
                            //System.out.println("strong_neg: " + token);
                        }
                        else if(dl.type == 1) { //weaksubj
                            cc.Neg_weak++;
                            continue;
                            //System.out.println("weak_neg: " + token);
                        }
                    } else if (dl.priorPolarity == 1) { //positive
                        if(dl.type == 2) { //strongsubj
                            cc.Pos_strong++;
                            continue;
                            //System.out.println("strong_pos: " + token);
                        }
                        else if(dl.type == 1) { //weaksubj
                            cc.Pos_weak++;
                            continue;
                            //System.out.println("weak_pos: " + token);
                        }
                    } else if (dl.priorPolarity == 0) { //neutral
                        if(dl.type == 2) { //strongsubj
                            cc.Neu_strong++;
                            continue;
                        }
                        else if(dl.type == 1) { //weaksubj
                            cc.Neu_weak++;
                            continue;
                        }
                    }
                }
            }
            //System.out.println(numNeg_strong + "||" + numNeg_weak + "||" + numPos_strong + "||" + numPos_weak + "||" + numNeu_strong + "||" + numNeu_weak + "||" + numProfanity + "||" +  numTaxonomyTerm);
        } finally {
            ss.positiveScore = Utility.getNDecimals((cc.Pos_strong*2 + cc.Pos_weak)/(cc.Pos_strong*2 + cc.Pos_weak + cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3 + cc.TaxonomyTerm*4), 2);
            ss.negativeScore = Utility.getNDecimals((cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3 + cc.TaxonomyTerm*4)/(cc.Pos_strong*2 + cc.Pos_weak + cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3 + cc.TaxonomyTerm*4), 2);

            return ss;
        }
    }
}
