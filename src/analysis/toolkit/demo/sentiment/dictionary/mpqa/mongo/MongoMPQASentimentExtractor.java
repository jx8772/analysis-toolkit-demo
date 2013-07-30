package analysis.toolkit.demo.sentiment.dictionary.mpqa.mongo;

import com.cybozu.labs.langdetect.LangDetectException;
import analysis.toolkit.demo.mongodb.MongoDBUtils;
import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.sentiment.utils.CluesCounter;
import analysis.toolkit.demo.sentiment.dictionary.mpqa.MPQASentimentExtractor;
import analysis.toolkit.demo.mongodb.MongoDBUtils;
import analysis.toolkit.demo.twitterprocess.DataTransform;
import analysis.toolkit.demo.twitterprocess.Preprocess;
import analysis.toolkit.demo.utility.Utility;
import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import static analysis.toolkit.demo.utility.Utility.preprocessLanguageDetect;
import static analysis.toolkit.demo.utility.Utility.preprocessTopicMining;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

/**
 * This class provides the class for sentiment analysis on mongodb
 * @version 1.0 07/25/2013
 * @author Xiang Ji
 * @email xiangji2010@gmail.com
 */
public class MongoMPQASentimentExtractor {
    
    
    /**
    * This function reads mongodb, and output a hashmap, in which the string date is the key, and the SentimentSCore Object is the value
    * @param message the text which needs to be determined the sentimental score
    * @param subjective_clues the MPQA dictionary
    * @param profanity_list the profanity list has bad words
    */
    public static SentimentSCore getSentimentMPQA(String dbName, String tableName, int year, int month, int day, HashMap<String, HashMap<String, String>> subject_clues, Set<String> profanity_list) throws FileNotFoundException, UnknownHostException {
        CluesCounter cc = new CluesCounter();
        //an object for storing final positve score and negative score
        SentimentSCore ss = new SentimentSCore();
        
        ArrayList<String> tweets = new ArrayList<>();
        
        int count = 0;
        int en_count = 0;
        String date = Utility.dateIntToString(year, month, day);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month-1, day, 0, 0, 0);
        Date d1 = cal.getTime();
        cal.set(year, month-1, day, 23, 59, 59);
        Date d2 = cal.getTime();

        //String query = "SELECT tweet_text FROM tweets WHERE created_at LIKE \"%"+ date  +  "%\"";
        BasicDBObject query = new BasicDBObject(); 
        query.put("created_at", new BasicDBObject("$gte", d1).append("$lte", d2));

        //show all document the collection: boston (currently)
        DBCollection table = MongoDBUtils.getMongoCollection(dbName, tableName);
        DBCursor cursor = table.find(query);
        try {  
          while (cursor.hasNext()) {
              BasicDBObject obj = (BasicDBObject)cursor.next();
              String raw_text = obj.getString("tweet_text");
              //System.out.println("get here");
              
              String processed_tweet = preprocessLanguageDetect(raw_text);
              String lang = "";
              if(!processed_tweet.equals("")) {
                  lang = Preprocess.detectLangCybozu(processed_tweet);
                  if(lang.equals("en")) {
                      tweets.add(processed_tweet);
                      //message += processed_tweet + " ";
                      //bw.write(preprocessTopicMining(raw_text) + "\n");
                      en_count++;
                  }
              }
             count++;
             //output the count for every 10000 new tweets added
             if(count%10000 == 0) {
                 //System.out.println(count + " tweets are procssed");
             }
          }
        } catch (LangDetectException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            ss = MPQASentimentExtractor.getSentimentMPQA(tweets, subject_clues, profanity_list);
             return ss;
        }
    }
    
    public static HashMap<String, SentimentSCore> getSentimentMPQAWithRange(String db, String table, String start, String end, HashMap<String, HashMap<String, String>> subject_clues, Set<String> profanity_list) {
        HashMap<String, SentimentSCore> dailySentiment = new HashMap<>();
        List<LocalDate> dates = new ArrayList<LocalDate>();
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        int days = Days.daysBetween(startDate, endDate).getDays();
        for (int i=0; i <= days; i++) {
            LocalDate d = startDate.withFieldAdded(DurationFieldType.days(), i);
            try {
                dailySentiment.put(d.toString(), getSentimentMPQA("data", "libya", d.getYear(),d.getMonthOfYear(),d.getDayOfMonth(), subject_clues, profanity_list));
                System.out.println(i + " days are parsed");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MongoMPQASentimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownHostException ex) {
                Logger.getLogger(MongoMPQASentimentExtractor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                
            }
         }
        return dailySentiment;
    }
}


