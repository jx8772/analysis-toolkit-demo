package analysis.toolkit.demo.twitterpreprocess;

import java.io.*;
import java.sql.*;
import java.util.BitSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.math.*;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import process.Preprocess.init;
import com.iai.minerva.analysis.utility.Utility;
import com.memetix.mst.detect.Detect;
//import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

public class DataTransform {
	
        private static double threshold = 1;
    
	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException, LangDetectException{
		//Translate.setHttpReferrer("http://www.i-a-i.com"); //------------------------------------------ Google Translate API is paid service --------------------------------
                // Set your Windows Azure Marketplace client info - See http://msdn.microsoft.com/en-us/library/hh454950.aspx    
                //Translate.setClientId(/* Enter your Windows Azure Client Id here */);
                //Translate.setClientSecret(/* Enter your Windows Azure Client Secret here */);
                
                //Translate.setClientId("IAI");
                //Translate.setClientSecret("giIykb6GgrKr2vN/Qr6pIdbq5MXmM9Hnqt+wJpciPqE=");
                
                //DataTransform("dcflu", "C:/workspace/hscb/data/", 2013,3,29);
		//DataTransformWithRange("dcflu", "2013-03-29", "2013-06-04");
                
                //tfidfFiltering(".\\data\\analysis\\textanalytics\\topmodel_temp\\", "C:\\workspace\\data\\tf-idf\\labeled\\", 2011,03,24);
               //tfidfFilteringWithRange("libya", "2011-03-14", "2012-11-16");
                
                //tfidfTopKeywords("C:\\workspace\\data\\tf-idf\\labeled\\","C:\\workspace\\data\\tf-idf\\labeled\\", 2011,03,23,2);
                tfidfTopKeywordsWithRange("libya", "2011-03-14", "2012-11-16",0.8);
	} 
        
        public static void DataTransformWithRange(String db, String start, String end) {
            String path = "c:/profiles";
            try {
                Preprocess.init(path);
            } catch (LangDetectException ex) {
                Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            List<LocalDate> dates = new ArrayList<LocalDate>();
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            int days = Days.daysBetween(startDate, endDate).getDays();
            for (int i=0; i <= days; i++) {
                LocalDate d = startDate.withFieldAdded(DurationFieldType.days(), i);
                try {
                    DataTransform(db, "C:/workspace/hscb/"+db+"/", d.getYear(),d.getMonthOfYear(),d.getDayOfMonth());
                } catch (SQLException ex) {
                    Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
                }
             }
        }
        
        public static void tfidfFilteringWithRange(String db, String start, String end) {
            String path = "c:/profiles";
            try {
                Preprocess.init(path);
            } catch (LangDetectException ex) {
                Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            List<LocalDate> dates = new ArrayList<LocalDate>();
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            int days = Days.daysBetween(startDate, endDate).getDays();
            for (int i=0; i <= days; i++) {
                LocalDate d = startDate.withFieldAdded(DurationFieldType.days(), i);
                try {
                    tfidfFiltering(".\\data\\analysis\\textanalytics\\topmodel_temp\\", "C:\\workspace\\data\\tf-idf\\" + db + "\\", d.getYear(),d.getMonthOfYear(),d.getDayOfMonth());
                } catch (IOException ex) {
                    Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
                }
             }
        }
        
        public static void tfidfTopKeywordsWithRange(String db, String start, String end, double td) {
            String path = "c:/profiles";
            try {
                Preprocess.init(path);
            } catch (LangDetectException ex) {
                Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            List<LocalDate> dates = new ArrayList<LocalDate>();
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            int days = Days.daysBetween(startDate, endDate).getDays();
            for (int i=0; i <= days; i++) {
                LocalDate d = startDate.withFieldAdded(DurationFieldType.days(), i);
                try {
                    tfidfTopKeywords("C:\\workspace\\data\\tf-idf\\labeled\\", "C:\\workspace\\data\\tf-idf\\labeled\\" ,d.getYear(),d.getMonthOfYear(),d.getDayOfMonth(), td);
                } catch (IOException ex) {
                    Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
                }
             }
        }
        
        public static void DataTransform(String db, String directory, int year, int month, int day) throws SQLException, IOException {
            String db_name = db;
            Connection conn = null;
            Statement stmt = null;
            Statement stmt2 = null;
            Statement stmt3 = null;
            ResultSet rs = null;
            
            try {
                   Class.forName("com.mysql.jdbc.Driver");
                   conn = DriverManager.getConnection("jdbc:mysql://10.5.1.126/" + db_name + "?user=twitter&password=123");
                   stmt = conn.createStatement();
                   stmt2 = conn.createStatement();
                   stmt3 = conn.createStatement();
                   String date = Utility.dateIntToString(year, month, day);
                   String filename = directory + "" + date + ".txt";
                   
                   //create a file for storing detected tweets
                   BufferedWriter bw = Utility.getBufferedWriter(filename);
//                   File file = new File(filename);
//                    FileWriter fw;
//                    BufferedWriter bw;
//                    file.createNewFile();
//                    fw = new FileWriter(file.getAbsoluteFile());
//                    bw = new BufferedWriter(fw);
                    
                    //create a file for tracking performance of data querying and language detection
                   BufferedWriter bw_log = Utility.getBufferedWriter("C:/workspace/hscb/TwitterProcessPipeline/log.txt", true);
//                   File file_log = new File("C:/workspace/hscb/TwitterProcessPipeline/log.txt");
//                    FileWriter fw_log;
//                    BufferedWriter bw_log;
//                    file_log.createNewFile();
//                    fw_log = new FileWriter(file_log.getAbsoluteFile(), true);
//                    bw_log = new BufferedWriter(fw_log);
                   
                   System.out.println(filename);
                   bw_log.write(filename + "\n");
                   
                    int count = 0;
                    int en_count = 0;
                    
                    DateTime dt1_query = new DateTime();
                   bw_log.write(dt1_query.toString()+ "\n");
                    String query = "SELECT tweet_text FROM tweets WHERE created_at LIKE \"%"+ date  +  "%\"";
                   
                    //System.out.println(query);
                   rs = stmt.executeQuery(query);
                   DateTime dt2_query = new DateTime();
                   Duration duration_query = new Interval(dt1_query, dt2_query).toDuration();
                    
                    DateTime dt1 = new DateTime();
                    
                   while (rs.next() ) {  
                       String raw_text = rs.getString("tweet_text");
                       //System.out.println("get here");
                       try {
                           //String lang = Preprocess.detectLangCybozuMSCombined(Preprocess.preprocessLanguageDetect(raw_text));
                           String processed_tweet = Preprocess.preprocessLanguageDetect(raw_text);
                           String lang = "";
                           if(!processed_tweet.equals("")) {
                                //System.out.println(processed_tweet);
                                lang = Preprocess.detectLangCybozu(processed_tweet);
                                //System.out.println(raw_text);
                                if(lang.equals("en")) {
                                   //System.out.println(Preprocess.preprocessTopicMining(raw_text) + "\n");
                                    bw.write(Preprocess.preprocessTopicMining(raw_text) + "\n");
                                    en_count++;
                                }
                           }
                           count++;
                           
                       } catch (LangDetectException ex) {
                           Logger.getLogger(DataTransform.class.getName()).log(Level.SEVERE, null, ex);
                       }
                        //System.out.println(count);
                   }
                   System.out.println(count + " records are processed");
                   bw_log.write(count + " records are processed" + "\n");
                   
                   System.out.println(en_count + " records are detected as english");
                   bw_log.write(en_count + " records are detected as english" + "\n");
                   
                   DateTime dt2 = new DateTime();
                    Duration duration = new Interval(dt1, dt2).toDuration();
                    System.out.println("querying db takes " +duration_query.getStandardSeconds() + " secs");
                    bw_log.write("querying db takes " +duration_query.getStandardSeconds() + " secs" + "\n");
                    
                    System.out.println("detect lang takes " +duration.getStandardSeconds() + " secs");
                    bw_log.write("detect lang takes " +duration.getStandardSeconds() + " secs" + "\n");
                    
                    System.out.println();
                    bw_log.write("\n");
                    
                   bw.close();
                   bw_log.close();
            } catch (ClassNotFoundException e) {
                           e.printStackTrace();
                   }catch (SQLException ex) {
                           // handle any errors
                           System.out.println("SQLException: " + ex.getMessage());
                           System.out.println("SQLState: " + ex.getSQLState());
                           System.out.println("VendorError: " + ex.getErrorCode());
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
                           if(stmt3 != null) {
                                   stmt3.close();
                                   stmt3 = null;
                           }
                           if(conn != null) {
                                   conn.close();
                                   conn = null;
                           }
                   }
        }
        
        
    public static String detectLangMS(String text) {
        com.memetix.mst.language.Language lang = com.memetix.mst.language.Language.ENGLISH;
        
        boolean badDetect = false;
            try {
                
                lang = Detect.execute(text);
                if (lang == null) {
                    //System.out.println("lang is null");
                    badDetect = true;
                }
            } catch (Exception ex) {
                //System.out.println("MS: bad detection");
                ex.printStackTrace();
                //catch bad detection
                badDetect = true;
            }
        if(badDetect == true)
            return "badDetection";
        else
            return lang.toString();
    }
       
    public static void init(String profileDirectory) throws LangDetectException {
        DetectorFactory.loadProfile(profileDirectory);
    }
    
    public static String detectLangCybozu(String text) throws LangDetectException {
        if(text.equals("") || !text.matches(".*[a-zA-Z]+.*"))
            return "badDetection";
        else {
            Detector detector = DetectorFactory.create();
            detector.append(text);
            return detector.detect();
        }
    }
    
    public static String detectLangCybozuMSCombined(String text) throws LangDetectException {
        String tweet_text = text;
        String langDetected = "";
             
        langDetected = detectLangMS(preprocessLanguageDetect(tweet_text));
        if(langDetected.equals("") || langDetected.equals("badDetection")) {
            langDetected = detectLangMS(tweet_text);
            if(langDetected.equals("") || langDetected.equals("badDetection")) {
                langDetected = detectLangCybozu(preprocessLanguageDetect(tweet_text));
                if(langDetected.equals("") || langDetected.equals("badDetection")) {
                    langDetected = detectLangCybozu(tweet_text);
                    if(langDetected.equals("") || langDetected.equals("badDetection")) {
                        langDetected = "badDetection";
                    }
                }
            }
        }
        return langDetected;
    }
    
    public static ArrayList<Language> detectLangs(String text) throws LangDetectException {
        Detector detector = DetectorFactory.create();
        detector.append(text);
        return detector.getProbabilities();
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
    
    public static void tfidfFiltering(String input_directory, String output_directory, int year, int month, int day) throws FileNotFoundException, IOException {
           String year_s = Integer.toString(year);
           String month_s = Integer.toString(month);
           String day_s = Integer.toString(day);
           if((year < 2010) || (year > 2050) || (month > 12)|| (month < 1)|| (day > 31)|| (day < 1)) {
               System.out.println("wrong date ranges");
               return;
           } 

           if(month < 10)
               month_s = "0" + month_s;
           if(day < 10)
               day_s = "0" + day_s;

           String date = year_s + "-" + month_s + "-" +day_s;
           String filename = input_directory + "" + date + ".txt";
           File file = new File(filename);
            //create a file for storing detected tweets
            
           File file_write = new File(output_directory + "tfidf-" + date  +".txt");
             FileWriter fw;
             BufferedWriter bw;
             file_write.createNewFile();
             fw = new FileWriter(file_write.getAbsoluteFile());
             bw = new BufferedWriter(fw);
           
            BufferedReader input =  new BufferedReader(new FileReader(file));
           try {
                  String line = null; //not declared within while loop
                  String line_tfidf = null;
                  int idfNumerator = countNumRow(filename);
                  int index = 0;
                  ArrayList<HashMap<String, Integer>> tfHashList = new ArrayList<HashMap<String, Integer>>();
                  
                  
                  for(int i=0; i<idfNumerator;i++) {
			tfHashList.add(new HashMap<String, Integer>());
                    }
                  
                  HashMap<String, Integer> idfDenominator = new HashMap<String, Integer>();
                  
                  ArrayList<ArrayList<Double>> tfidfRating = new ArrayList<ArrayList<Double>>();
                  for(int i=0; i<tfidfRating.size();i++) {
			tfidfRating.add(new ArrayList<Double>());
                    }
                  
                  while (( line = input.readLine()) != null){
                    //contents.append(line);
                    //contents.append(System.getProperty("line.separator"));
                      //System.out.println(line);
                      String lowercase_line = line.toLowerCase();
                      String[] splits = line.split(" ");
                      String[] lowercase_splits = lowercase_line.split(" ");
                      
                      for (String token : lowercase_splits) {
                          int temp_count = 0;
                          if (tfHashList.get(index).containsKey(token)) {
                              tfHashList.get(index).put(token, tfHashList.get(index).get(token)+1);  
                          } else {
                               tfHashList.get(index).put(token, 1);  
                          }
                      }
                      
                      for(String unique_token : tfHashList.get(index).keySet()){
			 if (idfDenominator.containsKey(unique_token)) {
                             idfDenominator.put(unique_token, idfDenominator.get(unique_token)+1);  
                         } else {
                             idfDenominator.put(unique_token, 1); 
                         }
                      }
                      index++;
                  }
                  
                  BufferedReader tfidfReader =  new BufferedReader(new FileReader(file));
                  int index_tfidf = 0;
                   while (( line_tfidf = tfidfReader.readLine()) != null){
                       String lowercase_line = line_tfidf.toLowerCase();
                      String[] splits_tfidf = line_tfidf.split(" ");
                      String[] lowercase_splits = lowercase_line.split(" ");
                       
                       //System.out.println(line_tfidf);
                       bw.write(line_tfidf + "\n");
                       
                       for(int i = 0; i < lowercase_splits.length; i++) {
                           //System.out.print(splits_tfidf[i] + "||");
                           //System.out.println(index_tfidf + ": " + splits_tfidf[i]  +": " +tfHashList.get(index_tfidf).get(splits_tfidf[i]));
                           //System.out.println(index_tfidf + ": " + splits_tfidf[i]  +": " +idfDenominator.get(splits_tfidf[i]));
                           
                           //System.out.println(index_tfidf + ": " +splits_tfidf[i] + ": " + calculateTfidf(tfHashList.get(index_tfidf).get(lowercase_splits[i]), idfNumerator, idfDenominator.get(lowercase_splits[i])));
                           if(i != (lowercase_splits.length - 1))
                               bw.write(calculateTfidf(tfHashList.get(index_tfidf).get(lowercase_splits[i]), idfNumerator, idfDenominator.get(lowercase_splits[i])) + " ");
                           else
                               bw.write(calculateTfidf(tfHashList.get(index_tfidf).get(lowercase_splits[i]), idfNumerator, idfDenominator.get(lowercase_splits[i])) + "");
                       }
                       bw.write("\n");
                        //System.out.println();
                       index_tfidf++;
                   }
                   System.out.println(filename + " finished");
                   
           }
            catch (IOException ex){
              ex.printStackTrace();
            }finally {
               bw.close();
                input.close();
                 
              }
        
    }
    
        public static int countNumRow(String filename) throws IOException {
            InputStream is = new BufferedInputStream(new FileInputStream(filename));
            try {
                byte[] c = new byte[1024];
                int count = 0;
                int readChars = 0;
                boolean empty = true;
                while ((readChars = is.read(c)) != -1) {
                    empty = false;
                    for (int i = 0; i < readChars; ++i) {
                        if (c[i] == '\n') {
                            ++count;
                        }
                    }
                }
                return (count == 0 && !empty) ? 1 : count;
            } finally {
                is.close();
            }
        }
        
        public static double calculateTfidf(double tf, double idf_numberator, double idf_denominator) {
            double idf = Math.log10(idf_numberator/idf_denominator);
            return (double)Math.round(tf*idf*10000)/10000;
        }
        
        public static void tfidfTopKeywords(String input_directory, String output_directory, int year, int month, int day, double td) throws FileNotFoundException, IOException {
            String year_s = Integer.toString(year);
           String month_s = Integer.toString(month);
           String day_s = Integer.toString(day);
           if((year < 2010) || (year > 2050) || (month > 12)|| (month < 1)|| (day > 31)|| (day < 1)) {
               System.out.println("wrong date ranges");
               return;
           } 

           if(month < 10)
               month_s = "0" + month_s;
           if(day < 10)
               day_s = "0" + day_s;

           String date = year_s + "-" + month_s + "-" +day_s;
           String filename = input_directory + "tfidf-" + date  +".txt";
          
           File file = new File(filename);
           BufferedReader input =  new BufferedReader(new FileReader(file));
           
           File file_write = new File(output_directory + td + "-tfidf-" + date  +".txt");
             FileWriter fw;
             BufferedWriter bw;
             file_write.createNewFile();
             fw = new FileWriter(file_write.getAbsoluteFile());
             bw = new BufferedWriter(fw);
           
           try {
                  String line_tokens = null; //not declared within while loop
                  String line_tfidf = null;
                  while (( line_tokens = input.readLine()) != null){
                      String new_tokens = "";
                      line_tfidf = input.readLine();
                      String[] tokens_tfidf = line_tokens.split(" ");
                      String[] tfidf_splits = line_tfidf.split(" ");
                      for (int i = 0; i < tokens_tfidf.length; i++) {
                          if(Double.parseDouble(tfidf_splits[i]) >= td) {
                              if(i != (tokens_tfidf.length-1))
                                new_tokens += tokens_tfidf[i] + " ";
                              else
                                new_tokens += tokens_tfidf[i]; 
                          }
                      }
                      bw.write(new_tokens + "\n");
                  }
           }catch (IOException ex){
              ex.printStackTrace();
            }finally {
               bw.close();
               input.close();
              }
           
        }
        
        
}