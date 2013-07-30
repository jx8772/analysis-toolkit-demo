/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.sentiment.dictionary.mpqa.mongo;

import com.cybozu.labs.langdetect.LangDetectException;
import analysis.toolkit.demo.mongodb.MongoDBUtils;
import static analysis.toolkit.demo.sentiment.dictionary.mpqa.mongo.MongoMPQASentimentExtractor.getSentimentMPQAWithRange;
import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import analysis.toolkit.demo.twitterprocess.DataTransform;
import analysis.toolkit.demo.twitterprocess.Preprocess;
import com.mongodb.DBCollection;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author xji
 */
public class Tester {
    public static void main(String[] args) throws FileNotFoundException, UnknownHostException {
        String path = "c:/profiles";
        try {
            Preprocess.init(path);
        } catch (LangDetectException ex) {
            Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        HashMap<String, HashMap<String, String>> subject_clues = SentimentAnalysisUtils.parseMPQAFile("./data/analysis/textanalytics/sentiment/mpqa/subjclueslen1-HLTEMNLP05.tff");
        Set<String> profanity_list = SentimentAnalysisUtils.parseProfanityListFile("./data/analysis/textanalytics/sentiment/profanity/profanity_list.txt");
        
        DBCollection table_libya_sentiment = MongoDBUtils.getMongoCollection("rawsentiment", "libya");
        
        //get the sentiment of a specific day
        //getSentimentMPQA("data", "libya", 2011, 3, 14, subject_clues, profanity_list);
        
        //get the sentiment of days in a range
        HashMap<String, SentimentSCore> dailySentiment = getSentimentMPQAWithRange("data", "libya", "2011-03-14", "2011-03-18", subject_clues, profanity_list);
        
        //Write the results back to mongodb
        //MongoDBUtils.writeToSentimentTable(table_libya_sentiment, dailySentiment);
        return;
    }
}
