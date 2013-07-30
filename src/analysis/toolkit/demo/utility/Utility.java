package analysis.toolkit.demo.utility;

import com.cybozu.labs.langdetect.LangDetectException;
import analysis.toolkit.demo.twitterprocess.DataTransform;
import analysis.toolkit.demo.twitterprocess.Preprocess;
import analysis.toolkit.demo.mongodb.MongoDBUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.time.DateFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class Utility {
   
    
    public static String dateIntToString(int year, int month, int day) {
        String year_s = Integer.toString(year);
        String month_s = Integer.toString(month);
        String day_s = Integer.toString(day);
        if((year < 2010) || (year > 2050) || (month > 12)|| (month < 1)|| (day > 31)|| (day < 1)) {
            System.out.println("wrong date ranges");
            return "";
        } 
        if(month < 10)
            month_s = "0" + month_s;
        if(day < 10)
            day_s = "0" + day_s;

        String date = year_s + "-" + month_s + "-" +day_s;
        return date;
    }
    
    public static BufferedWriter getBufferedWriter(String filename) throws IOException {
        return getBufferedWriter(filename, false);
    }
    
    public static BufferedWriter getBufferedWriter(String filename, boolean append) throws IOException {
        File file = new File(filename);
        FileWriter fw;
        BufferedWriter bw;
        file.createNewFile();
        if(append == true)
            fw = new FileWriter(file.getAbsoluteFile(),true);
        else
            fw = new FileWriter(file.getAbsoluteFile());
        bw = new BufferedWriter(fw);
        return bw;
    }
    
    public static double getNDecimals(double number, int n) {
        int multiplier = (int) Math.pow(10, n);
        return (double)Math.round(number*multiplier)/multiplier;
    }
    
    public static String[] getDatesBetween(String start, String end) {
        List<LocalDate> dates = new ArrayList<LocalDate>();
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        int days = Days.daysBetween(startDate, endDate).getDays() + 1;
        String[] strDates = new String[days];
        
        for (int i=0; i < days; i++) {
            LocalDate d = startDate.withFieldAdded(DurationFieldType.days(), i);
            strDates[i] = dateIntToString(d.getYear(),d.getMonthOfYear(),d.getDayOfMonth());
        }
        return strDates;
    }
    
    
    
    public static void mongoTMResultsToFile(String dbName, String tableName, String directory, int topic_id, int task_id) throws IOException {
        String filename = directory + "" + task_id + "-" + topic_id + ".txt";
        
        //create a file for storing detected tweets
        BufferedWriter bw = Utility.getBufferedWriter(filename);
        //String query = "SELECT tweet_text FROM tweets WHERE created_at LIKE \"%"+ date  +  "%\"";
        BasicDBObject query = new BasicDBObject("topic_id", topic_id).append("task_id", task_id); 

        //show all document the collection: boston (currently)
        DBCollection table = MongoDBUtils.getMongoCollection(dbName, tableName);
        DBCursor cursor = table.find(query);
        DBCollection doc_table = MongoDBUtils.getMongoCollection(dbName, "document");
        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject)cursor.next();
            String document_id = obj.getString("document_id");
            System.out.println(document_id);
            double proportion = obj.getDouble("topic_proportion");
            System.out.println(proportion);
            BasicDBObject doc_query = new BasicDBObject("task_id", task_id).append("document_id", Integer.parseInt(document_id)); 
            DBCursor doc_cursor = doc_table.find(doc_query);
            BasicDBObject doc_obj = (BasicDBObject)doc_cursor.next();
            //System.out.println(doc_obj);
            String document_date = doc_obj.getString("date");
            System.out.println(document_date);
            doc_cursor.close();
            bw.write(document_id + " " + document_date + " " + proportion + "\n");
        }
        cursor.close();
        bw.close();
    }
    
    public static void lockheedEventsToFile(String dbName, String tableName, String directory) throws IOException, ParseException {
        String filename = directory + tableName + ".txt";
        
        //create a file for storing detected tweets
        BufferedWriter bw = Utility.getBufferedWriter(filename);
        //String query = "SELECT tweet_text FROM tweets WHERE created_at LIKE \"%"+ date  +  "%\"";
        
        //show all document the collection: boston (currently)
        DBCollection table = MongoDBUtils.getMongoCollection(dbName, tableName);
        DBCursor cursor = table.find();
        SimpleDateFormat yourDateFormat = new SimpleDateFormat("yyyy-mm-dd hh/mm/ss");
        
        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject)cursor.next();
            int event_id = obj.getInt("event_id");
            SimpleDateFormat dateformatJava = new SimpleDateFormat("yyyy-MM-dd");

            String date = dateformatJava.format(obj.getDate("event_date"));
            int intensity =  obj.getInt("intensity");
            String country = obj.getString("country");
            
            //System.out.println(event_id + "|" + date + "|" + intensity + "|" + country);
            bw.write(event_id + "|" + date + "|" + intensity + "|" + country + "\n");
        }
        cursor.close();
        bw.close();
    }
    
        public static String preprocessLanguageDetect(String s) throws LangDetectException {
            /////replace emoticons with pos or neg or neu
            //negative SELECT * FROM `total_tweets` WHERE (tweet_text LIKE '%-.-%' OR tweet_text LIKE '%-_-%' OR tweet_text LIKE '%:'(%' OR tweet_text LIKE '%:( %' OR tweet_text LIKE '%/_\%' OR tweet_text LIKE '%:\%' OR tweet_text LIKE '%: ( %' OR tweet_text LIKE '%:-(%' OR tweet_text LIKE '%=(%' OR tweet_text LIKE '%;(%' OR tweet_text LIKE '%:C %' OR tweet_text LIKE '%:c %' OR tweet_text LIKE '%;c %' OR tweet_text LIKE '%;C %' OR tweet_text LIKE '%:[ %')
            s = s.replaceAll("(-.-(\\s+|$+)|-_-(\\s+|$+)|:'\\((\\s+|$+)|\\//_\\\\(\\s+|$+)|:\\((\\s+|$+)|:\\s\\((\\s+|$+)|:-\\((\\s+|$+)|=\\((\\s+|$+)|;\\((\\s+|$+)|:C(\\s+|$+)|:c(\\s+|$+)|;C(\\s+|$+)|;c(\\s+|$+)|:\\[(\\s+|$+))", " ");
            //positive SELECT * FROM `total_tweets` WHERE (tweet_text LIKE '%:)%' OR tweet_text LIKE '%: )%' OR tweet_text LIKE '%:-)%' OR tweet_text LIKE '%:D %' OR tweet_text LIKE '%: D %' OR tweet_text LIKE '%:p %' OR tweet_text LIKE '%: p %' OR tweet_text LIKE '%=)%' OR tweet_text LIKE '%:o)%' OR tweet_text LIKE '%:]%' OR tweet_text LIKE '%: ]%' OR tweet_text LIKE '%:3 %' OR tweet_text LIKE '%:c)%' OR tweet_text LIKE '% C:%' OR tweet_text LIKE '% ;)%' OR tweet_text LIKE '% ; )%')
            s = s.replaceAll("(:\\)(\\s+|$+)|:\\s\\)(\\s+|$+)|:-\\)(\\s+|$+)|:D(\\s+|$+)|:\\sD(\\s+|$+)|:p(\\s+|$+)|:\\sp(\\s+|$+)|=\\)(\\s+|$+)|:o\\)(\\s+|$+)|:\\](\\s+|$+)|:\\s\\](\\s+|$+)|:3(\\s+|$+)|:c\\)(\\s+|$+)|(\\s+|^+)C:(\\s+|$+)|(\\s+|^+);\\)(\\s+|$+)|(\\s+|^+);\\s\\)(\\s+|$+))", " ");
            //neutral tweet_text LIKE '%-.-%' OR tweet_text LIKE '%:| %' OR tweet_text LIKE '%0_o%' OR tweet_text LIKE '%o_0%'
            s = s.replaceAll("(:\\|(\\s+|$+)|0_o(\\s+|$+)|o_0(\\s+|$+))", " ");

            //replace double quotes with " "
            s = s.replace("\"", " ");

            //repeating spaces
            s = s.replaceAll(" ", "    ");
            /////replace @xx with tag
            s = s.replaceAll("(^|\\s)@.*?(\\s|$)", " ");

            //replace "&amp" with "&", replace "&lt" with "<", replace "&gt" with ">"
            s = s.replaceAll("&amp", " ").replaceAll("&lt", " ").replaceAll("&gt", " ");

            //replce xx... with blank
            s = s.replaceAll("(\\S+)(\\.\\.\\.|…)(\\s+|$+)", " ");

            //delete independent numbers "2", "34" not "1st"
            s = s.replaceAll("(^+|\\s+)(\\d+)(\\s+|$+)", " ");

            // replace actual url address with the word "url"
            s = s.replaceAll("(^+|\\s+)http.*?(\\s+|$+)", " ");

            //replace double "/#<>;:.,=()~&[]{}-_"  with " "
            s = s.replaceAll("/|—|#|<|>|;|:|\\.|\\,|=|\\(|\\)|~|&|\\[|\\]|\\{|\\}|\\-|\\_", " ");

            //replace double "ab's"  with "ab"
            s = s.replaceAll("(\\S+)('|’)s*(\\s|$+)", " $1 ");

            //replace RT with ""
            s = s.replace("RT", " ");

            //replace | with " "
            s = s.replace("|", " ");

            //replace / with " ", replace \ with " ", replace * with " "
            s = s.replace("\\", " ").replace("/", " ").replace("*", " ");

            //replace repeated spaces and punctuation (!?) signs by one
            s = s.replaceAll("(\\!|\\?)\\1+", " ");

            //separate !? and the preceding text
            s = s.replaceAll("(\\!|\\?)", " $1 ");

            //lowercase
            //s = s.toLowerCase();

            //remove repeating spaces
            s = s.replaceAll("( )\\1+", " ");

            //removing trailing and leading spaces
            s = s.trim();

            return s;
        }

        public static String preprocessTopicMining(String s) throws LangDetectException {
            /////replace emoticons with pos or neg or neu
            //negative SELECT * FROM `total_tweets` WHERE (tweet_text LIKE '%-.-%' OR tweet_text LIKE '%-_-%' OR tweet_text LIKE '%:'(%' OR tweet_text LIKE '%:( %' OR tweet_text LIKE '%/_\%' OR tweet_text LIKE '%:\%' OR tweet_text LIKE '%: ( %' OR tweet_text LIKE '%:-(%' OR tweet_text LIKE '%=(%' OR tweet_text LIKE '%;(%' OR tweet_text LIKE '%:C %' OR tweet_text LIKE '%:c %' OR tweet_text LIKE '%;c %' OR tweet_text LIKE '%;C %' OR tweet_text LIKE '%:[ %')
            s = s.replaceAll("(-.-(\\s+|$+)|-_-(\\s+|$+)|:'\\((\\s+|$+)|\\//_\\\\(\\s+|$+)|:\\((\\s+|$+)|:\\s\\((\\s+|$+)|:-\\((\\s+|$+)|=\\((\\s+|$+)|;\\((\\s+|$+)|:C(\\s+|$+)|:c(\\s+|$+)|;C(\\s+|$+)|;c(\\s+|$+)|:\\[(\\s+|$+))", " ");
            //positive SELECT * FROM `total_tweets` WHERE (tweet_text LIKE '%:)%' OR tweet_text LIKE '%: )%' OR tweet_text LIKE '%:-)%' OR tweet_text LIKE '%:D %' OR tweet_text LIKE '%: D %' OR tweet_text LIKE '%:p %' OR tweet_text LIKE '%: p %' OR tweet_text LIKE '%=)%' OR tweet_text LIKE '%:o)%' OR tweet_text LIKE '%:]%' OR tweet_text LIKE '%: ]%' OR tweet_text LIKE '%:3 %' OR tweet_text LIKE '%:c)%' OR tweet_text LIKE '% C:%' OR tweet_text LIKE '% ;)%' OR tweet_text LIKE '% ; )%')
            s = s.replaceAll("(:\\)(\\s+|$+)|:\\s\\)(\\s+|$+)|:-\\)(\\s+|$+)|:D(\\s+|$+)|:\\sD(\\s+|$+)|:p(\\s+|$+)|:\\sp(\\s+|$+)|=\\)(\\s+|$+)|:o\\)(\\s+|$+)|:\\](\\s+|$+)|:\\s\\](\\s+|$+)|:3(\\s+|$+)|:c\\)(\\s+|$+)|(\\s+|^+)C:(\\s+|$+)|(\\s+|^+);\\)(\\s+|$+)|(\\s+|^+);\\s\\)(\\s+|$+))", " ");
            //neutral tweet_text LIKE '%-.-%' OR tweet_text LIKE '%:| %' OR tweet_text LIKE '%0_o%' OR tweet_text LIKE '%o_0%'
            s = s.replaceAll("(:\\|(\\s+|$+)|0_o(\\s+|$+)|o_0(\\s+|$+))", " ");

            //replace double quotes with " "
            s = s.replace("\"", " ");

            //repeating spaces
            s = s.replaceAll(" ", "    ");

            /////replace @xx with " "
            s = s.replaceAll("(^|\\s)@.*?(\\s|$)", " ");

            //replace "&amp" with "&", replace "&lt" with "<", replace "&gt" with ">"
            s = s.replaceAll("&amp", " ").replaceAll("&lt", " ").replaceAll("&gt", " ");

            //replce xx... with blank
            s = s.replaceAll("(\\S+)(\\.\\.\\.|…)(\\s+|$+)", " ");

            //delete independent numbers "2", "34" not "1st"
            s = s.replaceAll("(^+|\\s+)(\\d+)(\\s+|$+)", " ");

            // replace actual url address with the word " "
            s = s.replaceAll("(^+|\\s+)http.*?(\\s+|$+)", " ");

            //replace double "/#<>;:.,=()~&[]{}-_"  with " "
            s = s.replaceAll("/|—|#|<|>|;|:|\\.|\\,|=|\\(|\\)|~|&|\\[|\\]|\\{|\\}|\\-|\\_", " ");

            //replace double "ab's"  with "ab"
            s = s.replaceAll("(\\S+)('|’)s*(\\s|$+)", " $1 ");

            //replace RT with ""
            s = s.replace("RT", " ");

            //replace | with " "
            s = s.replace("|", " ");

            //replace / with " ", replace \ with " ", replace * with " "
            s = s.replace("\\", " ").replace("/", " ").replace("*", " ");

            //replace repeated spaces and punctuation (!?) signs by one
            s = s.replaceAll("(\\!|\\?)\\1+", " ");

            //separate !? and the preceding text
            s = s.replaceAll("(\\!|\\?)", " ");

            //lowercase
            //s = s.toLowerCase();

            //remove repeating spaces
            s = s.replaceAll("( )\\1+", " ");

            //removing trailing and leading spaces
            s = s.trim();

            return s;
        }
        
        public static String Regex(String text, String pattern, int numGroup) {
            if (numGroup == 1) {
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(text);
                if (m.find()) {
                    return m.group(1).toString();
                } else {
                    return "";
                }
            }
            return "";
        }
}
