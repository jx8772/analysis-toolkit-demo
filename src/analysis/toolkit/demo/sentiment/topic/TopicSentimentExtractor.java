/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.sentiment.topic;

import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.sentiment.utils.CluesCounter;
import analysis.toolkit.demo.sentiment.utils.DictionaryLabel;
import analysis.toolkit.demo.utility.Utility;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author xji
 */
public class TopicSentimentExtractor {
    /**
    * This function reads the a text, and output a SentimentScore object, which has a positive score and a negative score
    * @param message the text which needs to be determined the sentimental score
    * @param subjective_clues the MPQA dictionary
    * @param profanity_list the profanity list has bad words
    * @param keyword_weight the keywords in one topic, and their associated weights
    */
    public static SentimentSCore getWeightedTopicSentimentMPQA(String message, HashMap<String, HashMap<String, String>> subject_clues, Set<String> profanity_list, HashMap<String, Double> keyword_weight) throws FileNotFoundException {
        CluesCounter cc = new CluesCounter();
        //an object for storing final positve score and negative score
        SentimentSCore ss = new SentimentSCore();
        
        try {
            //normalize the sentences
            String preprocessed = SentimentAnalysisUtils.preprocessSentimentAnalysis(message);
            //System.out.println(message);
            //System.out.println(preprocessed);
            String[] tokens = preprocessed.split(" ");
            for (String token : tokens) {
                //if token is a profanity, add one with its weight
                if(profanity_list.contains(token)) {
                    cc.Profanity += keyword_weight.get(token);
                    continue;
                }
                
                //search the sentimental clues
                DictionaryLabel dl = new DictionaryLabel();
                dl = SentimentAnalysisUtils.lookUpDictionary(token, subject_clues);
                if(dl.isSet) {
                    if(dl.priorPolarity == -1) { //negative
                        if(dl.type == 2) { //strongsubj
                            cc.Neg_strong += keyword_weight.get(token);
                            continue;
                            //System.out.println("strong_neg: " + token);
                        }
                        else if(dl.type == 1) { //weaksubj
                            cc.Neg_weak += keyword_weight.get(token);
                            continue;
                            //System.out.println("weak_neg: " + token);
                        }
                    } else if (dl.priorPolarity == 1) { //positive
                        if(dl.type == 2) { //strongsubj
                            cc.Pos_strong += keyword_weight.get(token);
                            continue;
                            //System.out.println("strong_pos: " + token);
                        }
                        else if(dl.type == 1) { //weaksubj
                            cc.Pos_weak += keyword_weight.get(token);
                            continue;
                            //System.out.println("weak_pos: " + token);
                        }
                    } else if (dl.priorPolarity == 0) { //neutral
                        if(dl.type == 2) { //strongsubj
                            cc.Neu_strong += keyword_weight.get(token);
                            continue;
                        }
                        else if(dl.type == 1) { //weaksubj
                            cc.Neu_weak += keyword_weight.get(token);
                            continue;
                        }
                    }
                }
            }
            //System.out.println(numNeg_strong + "||" + numNeg_weak + "||" + numPos_strong + "||" + numPos_weak + "||" + numNeu_strong + "||" + numNeu_weak + "||" + numProfanity);
        } finally {
            ss.positiveScore = Utility.getNDecimals((cc.Pos_strong*2 + cc.Pos_weak)/(cc.Pos_strong*2 + cc.Pos_weak + cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3 + cc.TaxonomyTerm*4), 2);
            ss.negativeScore = Utility.getNDecimals((cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3 + cc.TaxonomyTerm*4)/(cc.Pos_strong*2 + cc.Pos_weak + cc.Neg_strong*2 + cc.Neg_weak + cc.Profanity*3 + cc.TaxonomyTerm*4), 2);
        
            return ss;
        }
    }
}
