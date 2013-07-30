/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.sentiment.dictionary.afinn;

import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import analysis.toolkit.demo.twitterprocess.OntologyProcess;
import analysis.toolkit.demo.utility.Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @This class is used for utilizing AFINN dictionary to compute sentiment
 * @version 1.0 07.29.2013
 * @author Xiang Ji
 * @email: xiangji2010@gmail.com
 */
public class AFINNSentimentExtractor {
    /**
    * This function reads the a text, and output a SentimentScore object, which has a positive score and a negative score
    * @param message the text which needs to be determined the sentimental score
    * @param afinn_clues file that contains afinn words in format "word\tsentimental_score"
    */
    public static SentimentSCore getSentimentAFINN(String message, HashMap<String, Integer> afinn_clues) throws FileNotFoundException {
        //an object for storing final positve score and negative score
        SentimentSCore ss = new SentimentSCore();
        double positiveScore = 0;
        double negativeScore = 0;
        
        try {
            //normalize the sentences
            String preprocessed = SentimentAnalysisUtils.preprocessSentimentAnalysis(message);
            
            //System.out.println(message);
            //System.out.println(preprocessed);
            String[] tokens = preprocessed.split(" ");
            for (String token : tokens) {
                //get the label, in the format "priorpolarity#type"   
                if(!afinn_clues.containsKey(token))
                    continue;
                else {
                    int score = afinn_clues.get(token);
                    if(score > 0)
                        positiveScore += score;
                    else if (score < 0)
                        negativeScore += Math.abs(score);
                }
            }
        } finally {
            ss.positiveScore = Utility.getNDecimals(positiveScore / (positiveScore + negativeScore),2);
            ss.negativeScore = Utility.getNDecimals(negativeScore / (positiveScore + negativeScore),2);
            return ss;
        }
    }
    
    /**
    * This function reads the a text, and output a SentimentScore object, which has a positive score and a negative score
    * @param message the text which needs to be determined the sentimental score
    * @param afinn_clues the AFINN dictionary of sentimental words
    * @param profanity_list the profanity list has bad words
    * @param taxonomy_term_list the term words of PTSD symptoms
    */
    public static SentimentSCore getSentimentAFINNTaxonomyTerm(String message, HashMap<String, Integer> afinn_clues, Set<String> taxonomy_term_list) throws FileNotFoundException {
         //an object for storing final positve score and negative score
        SentimentSCore ss = new SentimentSCore();
        double positiveScore = 0;
        double negativeScore = 0;
       
        try {
            //normalize the sentences
            String preprocessed = SentimentAnalysisUtils.preprocessSentimentAnalysis(message);
            Iterator iter = taxonomy_term_list.iterator();
            while (iter.hasNext()) {
                String term = iter.next().toString();
                if(preprocessed.indexOf(term) > -1) {
                    ss.negativeScore += 5;
                }
            }
            //System.out.println(message);
            //System.out.println(preprocessed);
            String[] tokens = preprocessed.split(" ");
            for (String token : tokens) {
                //get the label, in the format "priorpolarity#type"   
                if(!afinn_clues.containsKey(token))
                    continue;
                else {
                    int score = afinn_clues.get(token);
                    if(score > 0)
                        positiveScore += score;
                    else if (score < 0)
                        negativeScore += Math.abs(score);
                }
            }
        } finally {
            ss.positiveScore = Utility.getNDecimals(positiveScore / (positiveScore + negativeScore),2);
            ss.negativeScore = Utility.getNDecimals(negativeScore / (positiveScore + negativeScore),2);
            return ss;
        }
    }
}
