package analysis.toolkit.demo.utils;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import com.cybozu.labs.langdetect.LangDetectException;
import com.iai.minerva.analysis.twitterprocess.Preprocess;
import static com.iai.minerva.analysis.utility.Utility.getMongoCollection;
import static com.iai.minerva.analysis.utility.Utility.preprocessLanguageDetect;
import static com.iai.minerva.analysis.utility.Utility.preprocessTopicMining;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

/**
 * @version 1.0 07.01.2013
 * @author xji
 * This class is responsible for transforming mysql data records into mongodb collections
 */
public class MongoDBUtils {
    private static int SIZE_SELECT = 200000;
    private static int SIZE_INSERT = 5000;
    private static int NUM_KEYWORD_PER_TOPIC = 10;
    
    public static void main(String[] args) throws SQLException, IOException, ParseException {
        try {
            //readTweetInMySQL("lockheed_events", "data", "lockheed_events");
            
//            for(int i = 0; i <= 19; i ++) {
//                Utility.mongoTMResultsToFile("analysisresult", "tmresult", ".\\data\\analysis\\textanalytics\\tm_results_temp\\", i, 1);
//            }
            
            Utility.lockheedEventsToFile("data", "lockheed_events", ".\\data\\analysis\\textanalytics\\lockheed_events\\");
            
        } //catch (UnknownHostException ex) {
            //Logger.getLogger(MongoDBUtils.class.getName()).log(Level.SEVERE, null, ex);
        //} 
        finally {
            
        }
    }
    
    
    
    public static void summarizeLockheedEvents() throws UnknownHostException {
        DBCollection table_events_summary = getMongoCollection("data", "lockheed_events_summary");
        DBCollection table = getMongoCollection("data", "lockheed_events");
        //String query = "SELECT tweet_text FROM tweets WHERE created_at LIKE \"%"+ date  +  "%\"";
        //BasicDBObject query = new BasicDBObject("topic_id", topic_id).append("task_id", task_id); 
        
        
        
        //DBCursor cursor = table.find(query);
        
//        while (cursor.hasNext()) {
//        
//        }
    }
    
    
    
    public static boolean writeToMongo(String task, InstanceList instances, ParallelTopicModel model) throws UnknownHostException {
        DBCollection table_task = getMongoCollection("analysisresult", "task");
        DBCollection table_topickey = getMongoCollection("analysisresult", "topickey");
        DBCollection table_document = getMongoCollection("analysisresult", "document");
        DBCollection table_tmresult = getMongoCollection("analysisresult", "tmresult");
        DBCollection table_featuretopic = getMongoCollection("analysisresult", "featuretopic");
        int task_id = getLatestTaskID(table_task) + 1;
        
        writeToTaskTable(table_task, task_id, task);
        
        writeToTopickeyTable(table_topickey, task_id, model, instances);
        
        writeToDocumentTable(table_document, task_id, model, instances);
        
        writeToTMresultTable(table_tmresult, task_id, model, instances);
        
        //writeToFeaturetopicTable(table_featuretopic, task_id, model, instances);
        
        return true;
    }
    
    
    
    public static void readTweetInMySQL(String mysqlDB, String mongoDB, String mongoCollection) throws SQLException, UnknownHostException{
        String db_name = mysqlDB;
        Connection conn = null;
        Statement stmt = null;
        Statement stmt2 = null;
        ResultSet rs = null;
        int count = 0;
        int numIteration = 0;
        int index = 0;
        DBCollection table = null;
        List<DBObject> itemList = new ArrayList<DBObject>();
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://10.5.36.25/" + db_name + "?user=twitter&password=123");
            stmt = conn.createStatement();
            stmt2 = conn.createStatement();
            table = Utility.getMongoCollection(mongoDB, mongoCollection);
            
            if(mysqlDB.equals("libya")) {
                //get the number of records
                ResultSet rs_temp =  stmt.executeQuery("SELECT count(*) AS count FROM tweets");
                rs_temp.next();
                count = rs_temp.getInt("count");

                //insert the record into mongodb with the chunk size be SIZE_ITERATION;
                numIteration = count/SIZE_SELECT;
                while(index <= numIteration) {
                    String selectQuery = "SELECT tweet_id, tweet_text, created_at, geo_lat, geo_long, user_id, screen_name, name FROM tweets LIMIT " 
                                         + String.valueOf(index*SIZE_SELECT) + ", " + String.valueOf(SIZE_SELECT);
                    rs = stmt.executeQuery(selectQuery);
                    //System.out.println(selectQuery);

                    while(rs.next()) {
                        DBObject document = new BasicDBObject();

                        document.put("tweet_id", rs.getLong("tweet_id"));
                        document.put("tweet_text", rs.getString("tweet_text"));
                        document.put("created_at", rs.getTimestamp("created_at"));
                        //System.out.println(rs.getTimestamp("created_at"));
                        //System.out.println(rs.getLong("tweet_id"));
                        document.put("geo_lat", rs.getString("geo_lat"));
                        document.put("geo_long", rs.getString("geo_long"));
                        document.put("user_id", rs.getLong("user_id"));
                        document.put("screen_name", rs.getString("screen_name"));
                        document.put("name", rs.getString("name"));
                        itemList.add(document);

                        if(itemList.size() == SIZE_INSERT) {
                            table.insert(itemList);
                            itemList.clear();
                        }
                    }
                    if(!itemList.isEmpty()) {
                        table.insert(itemList);
                        itemList.clear();
                    }
                    index++;
                    System.out.println("for " + mysqlDB + ": inserted " + index*SIZE_SELECT + " records into mongodb");
                }
            } else if (mysqlDB.equals("lockheed_events")) {
                //get the number of records
                ResultSet rs_temp =  stmt.executeQuery("SELECT count(*) AS count FROM egypt_libya_events");
                rs_temp.next();
                count = rs_temp.getInt("count");

                //insert the record into mongodb with the chunk size be SIZE_ITERATION;
                numIteration = count/SIZE_SELECT;
                while(index <= numIteration) {
                    String selectQuery = "SELECT * FROM egypt_libya_events LIMIT " 
                                         + String.valueOf(index*SIZE_SELECT) + ", " + String.valueOf(SIZE_SELECT);
                    rs = stmt.executeQuery(selectQuery);
                    //System.out.println(selectQuery);

                    while(rs.next()) {
                        DBObject document = new BasicDBObject();

                        document.put("event_id", rs.getLong("event_id"));
                        document.put("source_name", rs.getString("source_name"));
                        document.put("source_sectors", rs.getString("source_sectors"));
                        document.put("event_text", rs.getString("event_text"));
                        document.put("intensity", rs.getDouble("intensity"));
                        document.put("target_name", rs.getString("target_name"));
                        document.put("target_sectors", rs.getString("target_sectors"));
                        document.put("story_id", rs.getLong("story_id"));
                        document.put("sentence_num", rs.getInt("sentence_num"));
                        document.put("event_sentence", rs.getString("event_sentence"));
                        document.put("headline", rs.getString("headline"));
                        document.put("story_excerpt", rs.getString("story_excerpt"));
                        document.put("event_date", rs.getTimestamp("event_date"));
                        document.put("city", rs.getString("city"));
                        document.put("district", rs.getString("district"));
                        document.put("province", rs.getString("province"));//
                        document.put("country", rs.getString("country"));
                        document.put("latitude", rs.getDouble("latitude"));
                        document.put("longitude", rs.getDouble("longitude"));
                        itemList.add(document);

                        if(itemList.size() == SIZE_INSERT) {
                            table.insert(itemList);
                            itemList.clear();
                        }
                    }
                    if(!itemList.isEmpty()) {
                        table.insert(itemList);
                        itemList.clear();
                    }
                    index++;
                    System.out.println("for " + mysqlDB + ": inserted " + index*SIZE_SELECT + " records into mongodb");
                }
            }
        } catch (ClassNotFoundException e) {
                e.printStackTrace();
        } catch (SQLException ex) {

        } finally {
            if(rs != null) {
                rs.close();
                rs = null;
            }
            if(stmt != null) {
                stmt.close();
                stmt = null;
            }
            if(stmt2 != null) {
                stmt2.close();
                stmt2 = null;
            }
            if(conn != null) {
                conn.close();
                conn = null;
            }
        }
    }
    
    /**
    get the maximum task_id in the table
    */
    public static int getLatestTaskID(DBCollection table) {
        BasicDBObject keys = new BasicDBObject();  
        keys.put("task_id", -1);  
        DBCursor cursor = table.find().sort(keys).limit(1);
        if(cursor.size() == 0)
            return 0;
        else {
            BasicDBObject dbObject = (BasicDBObject)cursor.next();  
            return dbObject.getInt("task_id");   
        }
    }
    
    public static void writeToTaskTable(DBCollection table, int task_id, String task_name) {
        
        Date now = new Date();
        DBObject document = new BasicDBObject();
                    
        document.put("task_id", task_id);
        document.put("task_name", task_name);
        document.put("created_at", now);
        table.insert(document);
    }
    
    public static void writeToTopickeyTable(DBCollection table, int task_id, ParallelTopicModel model, InstanceList instances) {
        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        Alphabet dataAlphabet = instances.getDataAlphabet();
        String keyword = "";
        double weight = 0;
         
        for (int topic = 0; topic < model.numTopics; topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            int rank = 0;
            while (iterator.hasNext() && rank < NUM_KEYWORD_PER_TOPIC) {
                IDSorter idCountPair = iterator.next();
                keyword = dataAlphabet.lookupObject(idCountPair.getID()).toString();
                weight = idCountPair.getWeight();
                //System.out.println(topic + "||" +keyword + "||" + weight);
                DBObject document = new BasicDBObject();
                document.put("task_id", task_id);
                document.put("topic_id", topic);
                document.put("keyword", keyword);
                document.put("weight", weight);
                table.insert(document);
                rank++;
            }
        }
    }
    
    public static void writeToDocumentTable(DBCollection table, int task_id, ParallelTopicModel model, InstanceList instances) {
        for(int i =0; i < instances.size(); i++) {
            String filename = instances.get(i).getName().toString();
            String date = "";
            Pattern p = Pattern.compile(".*/(.*).txt");
            Matcher m = p.matcher(filename);
            if (m.find()) {
                //get the date from filename
                date = m.group(1).toString();
            }
            DBObject document = new BasicDBObject();
            document.put("task_id", task_id);
            document.put("document_id", i);
            document.put("date", date);
            table.insert(document);
        }
    }
    
    public static void writeToTMresultTable(DBCollection table, int task_id, ParallelTopicModel model, InstanceList instances) {
        for(int i =0; i < instances.size(); i++) {
            double[] topicDistribution = model.getTopicProbabilities(i);

            // Get an array of sorted sets of word ID/count pairs
            ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

            for (int topic = 0; topic < model.numTopics; topic++) {
                Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
                DBObject document = new BasicDBObject();
                document.put("task_id", task_id);
                document.put("document_id", i);
                document.put("topic_id", topic);
                document.put("topic_proportion", Utility.getNDecimals(topicDistribution[topic],3));
                table.insert(document);
            }
        }
    }
    
    public static void writeToFeaturetopicTable(DBCollection table, int task_id, ParallelTopicModel model, InstanceList instances) {
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        for(int i =0; i < instances.size(); i++) {
            FeatureSequence tokens = (FeatureSequence) model.getData().get(i).instance.getData(); //get the features (index) of the first document, the word-index pair can be found in tokens.dictionary.entries
            LabelSequence topics = model.getData().get(i).topicSequence;
            Formatter out = new Formatter(new StringBuilder(), Locale.US);

            for (int position = 0; position < tokens.getLength(); position++) { //tokens.getLength() is the length of that document
                //out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
                DBObject document = new BasicDBObject();
                document.put("task_id", task_id);
                document.put("document_id", i);
                document.put("topic_id", topics.getIndexAtPosition(position));
                document.put("feature_text", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)));
                table.insert(document);
            }
        }
    }
}
