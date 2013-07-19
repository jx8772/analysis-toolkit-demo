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

import com.memetix.mst.detect.Detect;
//import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Preprocess {
	
	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException, LangDetectException{
		Translate.setHttpReferrer("http://www.i-a-i.com"); //------------------------------------------ Google Translate API is paid service --------------------------------
                // Set your Windows Azure Marketplace client info - See http://msdn.microsoft.com/en-us/library/hh454950.aspx    
                //Translate.setClientId(/* Enter your Windows Azure Client Id here */);
                //Translate.setClientSecret(/* Enter your Windows Azure Client Secret here */);
                Translate.setClientId("IAI");
                Translate.setClientSecret("giIykb6GgrKr2vN/Qr6pIdbq5MXmM9Hnqt+wJpciPqE=");
             String path = "c:/profiles";
                    init(path);
                //deleteAttributes("dc_", "flu");
		//joinTweetsAndUsers("dc_", "flu");
		//deteteSpamAddress("dc_", "flu");
		//normalizeTweets("dc_", "flu");
                //compareMSAndCybozu("dc_", "flu");
		//identifyLang("dc_", "flu");
                
                Class.forName("com.mysql.jdbc.Driver");
				String db_name = "dc_flu";
		Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 ResultSet rs = null;
		 try {
				Class.forName("com.mysql.jdbc.Driver");
                   conn = DriverManager.getConnection("jdbc:mysql://10.5.1.126/" + db_name + "?user=twitter&password=123");
				stmt = conn.createStatement();
				stmt2 = conn.createStatement();
				int count = 0;
				rs = stmt.executeQuery("SELECT * FROM tweets");
                                while (rs.next() ) {    
					
					String s = rs.getString("tweet_text");
                                        String msLangDetected = detectLangCybozu(s);
                                        System.out.println(msLangDetected);
                                        System.out.println(count);
                                        count++;
                                }
				
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
				if(conn != null) {
					conn.close();
					conn = null;
				}
			}
                
                
                
               
	}
	
	public static void deleteAttributes(String prefix, String disease) throws SQLException {
		String db_name = prefix + disease;
		Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 ResultSet rs = null;
		 try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://10.5.1.3/" + db_name + "?user=twitter&password=123");
				stmt = conn.createStatement();
				stmt2 = conn.createStatement();
				
				rs = stmt.executeQuery("SHOW COLUMNS FROM `tweets` LIKE 'entities'");
				// if entities attribute exists
				if (rs.isBeforeFirst() ) {    
					//drop entities
					stmt2.executeUpdate("ALTER TABLE tweets DROP entities");
				} 
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
				if(conn != null) {
					conn.close();
					conn = null;
				}
			}
	}
	
	public static void joinTweetsAndUsers(String prefix, String disease) throws SQLException {
		String db_name = prefix + disease;
		Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 Statement stmt3 = null;
		 ResultSet rs = null;
		 try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://10.5.1.3/" + db_name + "?user=twitter&password=123");
				stmt = conn.createStatement();
				stmt2 = conn.createStatement();
				stmt3 = conn.createStatement();
				
				stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + disease  +  "_tweets_users` ( `id` bigint(20) unsigned NOT NULL, `tweet_text` varchar(160) NOT NULL,`parsed_text` varchar(200) DEFAULT NULL,`created_at` datetime NOT NULL,`user_id` int(10) unsigned NOT NULL,`screen_name` char(20) NOT NULL,`name` varchar(40) DEFAULT NULL,`location` varchar(50) DEFAULT NULL,`location_granularity` varchar(20) DEFAULT NULL,`rgeocoded_location` varchar(60) DEFAULT NULL,`spam_location` bool DEFAULT false,`lat` decimal(10,5) DEFAULT NULL,`lng` decimal(10,5) DEFAULT NULL,`followers_count` int(10) unsigned DEFAULT NULL,`friends_count` int(10) unsigned DEFAULT NULL,`statuses_count` int(10) unsigned DEFAULT NULL,`lang` varchar(20) DEFAULT NULL,`subj_obj` varchar(20) DEFAULT NULL,`predict_subj_obj` varchar(20) DEFAULT NULL,`neg_neu` varchar(20) DEFAULT NULL,`predict_neg_neu` varchar(20) DEFAULT NULL,PRIMARY KEY (`id`),KEY `created_at` (`created_at`),KEY `user_id` (`user_id`),KEY `screen_name` (`screen_name`),KEY `name` (`name`),FULLTEXT KEY `tweet_text` (`tweet_text`)) ENGINE=MyISAM DEFAULT CHARSET=utf8;");
				rs = stmt2.executeQuery("SELECT * FROM `" + disease  +  "_tweets_users` LIMIT 0,1");
				//if the disease_tweets_users table is empty, insert
				if (!rs.isBeforeFirst() ) {    
					stmt3.executeUpdate("INSERT INTO " + disease  +  "_tweets_users (id, tweet_text, parsed_text, created_at, user_id,  screen_name, name, location, location_granularity, rgeocoded_location, spam_location, lat, lng, followers_count, friends_count, statuses_count, lang, subj_obj, predict_subj_obj, neg_neu, predict_neg_neu) SELECT T.tweet_id, T.tweet_text, '', T.created_at, T.user_id, T.screen_name, T.name, U.location, '', '', 0, T.geo_lat, T.geo_long, U.followers_count, U.friends_count, U.statuses_count, '', '', '', '','' FROM tweets T, users U WHERE T.user_id = U.user_id");
				}
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
	
	public static void deteteSpamAddress(String prefix, String disease) throws SQLException, IOException {
		String db_name = prefix + disease;
		Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 Statement stmt3 = null;
		 Statement stmt4 = null;
		 
		 ResultSet rs = null;
		 try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://10.5.1.3/" + db_name + "?user=twitter&password=123");
				stmt = conn.createStatement();
				stmt2 = conn.createStatement();
				stmt3 = conn.createStatement();
				stmt4 = conn.createStatement();
				
				//intilize
				stmt2.executeUpdate("UPDATE " + disease + "_tweets_users SET spam_location = 0");
				
				String query = "UPDATE " + disease + "_tweets_users SET spam_location = 1 WHERE LOWER(location) LIKE ";
				String append = "";
				FileInputStream fstream = new FileInputStream("./lib/address-deletelist.txt");
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				int count = 0;
				while((strLine = br.readLine()) != null) {
					//System.out.println(strLine);
					if(count != 0)
						append = append + "OR location LIKE " + "\"%" + strLine + "%\" ";
					else
						append = append + "\"%"+  strLine + "%\" ";
					count++;
				}
				append = append + "OR location = \"\" ";
				query = query + append;
				int rows = stmt.executeUpdate(query);
				System.out.println(disease + ": " + rows + " locations are detected as spam");
				
				ResultSet rs_rt = stmt4.executeQuery("SELECT COUNT(*) as count FROM  " + disease + "_tweets_users WHERE tweet_text LIKE 'RT%'");
				rs_rt.next();
				int rowCount = rs_rt.getInt("count");
				//delete tweets start with RT
				stmt3.execute("DELETE FROM " + disease + "_tweets_users WHERE tweet_text LIKE 'RT%'");
				System.out.println(disease + ": " + rowCount + " tweets starts with RT and are deleted");
				
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
				if(stmt4 != null) {
					stmt4.close();
					stmt4 = null;
				}
				if(conn != null) {
					conn.close();
					conn = null;
				}
			}
	}
	
	public static void normalizeTweets(String prefix, String disease) throws SQLException {
		String db_name = prefix + disease;
		Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 Statement stmt3 = null;
		 ResultSet rs = null;
		 try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://10.5.1.3/" + db_name + "?user=twitter&password=123");
				stmt = conn.createStatement();
				stmt2 = conn.createStatement();
				stmt3 = conn.createStatement();
				
				rs = stmt.executeQuery("SELECT id,tweet_text FROM `" + disease  +  "_tweets_users` WHERE parsed_text = \"\"");
				//if the disease_tweets_users table is empty, insert
				while (rs.next() ) {    
					long id = rs.getLong("id");
					String s = rs.getString("tweet_text");

					/////replace emoticons with pos or neg or neu
					//negative SELECT * FROM `total_tweets` WHERE (tweet_text LIKE '%-.-%' OR tweet_text LIKE '%-_-%' OR tweet_text LIKE '%:'(%' OR tweet_text LIKE '%:( %' OR tweet_text LIKE '%/_\%' OR tweet_text LIKE '%:\%' OR tweet_text LIKE '%: ( %' OR tweet_text LIKE '%:-(%' OR tweet_text LIKE '%=(%' OR tweet_text LIKE '%;(%' OR tweet_text LIKE '%:C %' OR tweet_text LIKE '%:c %' OR tweet_text LIKE '%;c %' OR tweet_text LIKE '%;C %' OR tweet_text LIKE '%:[ %')
					s = s.replaceAll("(-.-(\\s+|$+)|-_-(\\s+|$+)|:'\\((\\s+|$+)|\\//_\\\\(\\s+|$+)|:\\((\\s+|$+)|:\\s\\((\\s+|$+)|:-\\((\\s+|$+)|=\\((\\s+|$+)|;\\((\\s+|$+)|:C(\\s+|$+)|:c(\\s+|$+)|;C(\\s+|$+)|;c(\\s+|$+)|:\\[(\\s+|$+))", " neg ");
					//positive SELECT * FROM `total_tweets` WHERE (tweet_text LIKE '%:)%' OR tweet_text LIKE '%: )%' OR tweet_text LIKE '%:-)%' OR tweet_text LIKE '%:D %' OR tweet_text LIKE '%: D %' OR tweet_text LIKE '%:p %' OR tweet_text LIKE '%: p %' OR tweet_text LIKE '%=)%' OR tweet_text LIKE '%:o)%' OR tweet_text LIKE '%:]%' OR tweet_text LIKE '%: ]%' OR tweet_text LIKE '%:3 %' OR tweet_text LIKE '%:c)%' OR tweet_text LIKE '% C:%' OR tweet_text LIKE '% ;)%' OR tweet_text LIKE '% ; )%')
					s = s.replaceAll("(:\\)(\\s+|$+)|:\\s\\)(\\s+|$+)|:-\\)(\\s+|$+)|:D(\\s+|$+)|:\\sD(\\s+|$+)|:p(\\s+|$+)|:\\sp(\\s+|$+)|=\\)(\\s+|$+)|:o\\)(\\s+|$+)|:\\](\\s+|$+)|:\\s\\](\\s+|$+)|:3(\\s+|$+)|:c\\)(\\s+|$+)|(\\s+|^+)C:(\\s+|$+)|(\\s+|^+);\\)(\\s+|$+)|(\\s+|^+);\\s\\)(\\s+|$+))", " pos ");
					//neutral tweet_text LIKE '%-.-%' OR tweet_text LIKE '%:| %' OR tweet_text LIKE '%0_o%' OR tweet_text LIKE '%o_0%'
					s = s.replaceAll("(:\\|(\\s+|$+)|0_o(\\s+|$+)|o_0(\\s+|$+))", " neu ");
					
					//replace double quotes with " "
					s = s.replace("\"", " ");
					
					//repeating spaces
					s = s.replaceAll(" ", "    ");
					/////replace @xx with tag
					s = s.replaceAll("(^|\\s)@.*?(\\s|$)", " tag ");
					
					//replace "&amp" with "&", replace "&lt" with "<", replace "&gt" with ">"
					s = s.replaceAll("&amp", "&").replaceAll("&lt", "<").replaceAll("&gt", ">");
					
					//replce xx... with blank
					s = s.replaceAll("(\\S+)(\\.\\.\\.|…)(\\s+|$+)", " ");
					
					//delete independent numbers "2", "34" not "1st"
					s = s.replaceAll("(^+|\\s+)(\\d+)(\\s+|$+)", " ");
					
					// replace actual url address with the word "url"
					s = s.replaceAll("(^+|\\s+)http.*?(\\s+|$+)", " url ");
					
					//replace double "/—#<>;:.,=()~&[]{}-_"  with " "
					s = s.replaceAll("/|—|#|<|>|;|:|\\.|\\,|=|\\(|\\)|~|&|\\[|\\]|\\{|\\}|\\-|\\_", " ");
					
					//replace double "ab's"  with "ab"
					s = s.replaceAll("(\\S+)('|’)s*(\\s|$+)", " $1 ");
					
					//replace | with " "
					s = s.replace("|", " ");
					
                                        //replace / with " ", replace \ with " ", replace * with " "
					s = s.replace("\\", " ").replace("/", " ").replace("*", " ");
                                        
					//replace repeated spaces and punctuation (!?) signs by one
					s = s.replaceAll("(\\!|\\?)\\1+", " $1 ");
					
					//separate !? and the preceding text
					s = s.replaceAll("(\\!|\\?)", " $1 ");
					
					//lowercase
					s = s.toLowerCase();
					
					//remove repeating spaces
					s = s.replaceAll("( )\\1+", " ");
					
					//removing trailing and leading spaces
					s = s.trim();
					
					String query = "UPDATE " + disease + "_tweets_users SET parsed_text = \"" + s + "\" WHERE ID = " + id;
					stmt2.executeUpdate(query);
		
				}
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
	
	public static void identifyLang(String prefix, String disease) throws SQLException, LangDetectException {
		String db_name = prefix + disease;
		Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 ResultSet rs = null;
		 try {
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection("jdbc:mysql://10.5.1.3/" + db_name + "?user=twitter&password=123");
                    stmt = conn.createStatement();
                    stmt2 = conn.createStatement();
                    rs = stmt.executeQuery("SELECT id,tweet_text,parsed_text FROM `" + disease  +  "_tweets_users` ORDER BY id");
                     
                    int count = 0;
                     String path = "c:/profiles";
                     init(path);
                     
                    //if the disease_tweets_users table is empty, insert
                    while (rs.next() ) {    
                            long id = rs.getLong("id");
                            String tweet_text = rs.getString("tweet_text");
                            String langDetected = "";
                            
                            
                            langDetected = detectLangCybozuMSCombined(tweet_text);  
                            //System.out.println(tweet_text);
                            //System.out.println("Summary: " + langDetected);
                            //System.out.println();
                            //System.out.println(tweet_text + "||" + langDetected + "||" + count);
                            String query = "UPDATE " + disease + "_tweets_users SET lang = \"" + langDetected + "\" WHERE ID = " + id;
                            stmt2.executeUpdate(query);
                            count++;
                    } 
                    stmt2 = conn.createStatement();
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
				if(conn != null) {
					conn.close();
					conn = null;
				}
			}
	}
        
        public static void compareMSAndCybozu(String prefix, String disease) throws SQLException, LangDetectException {
		String db_name = prefix + disease;
		Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 ResultSet rs = null;
                  
		 try {
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection("jdbc:mysql://10.5.1.3/" + db_name + "?user=twitter&password=123");
                    stmt = conn.createStatement();
                    stmt2 = conn.createStatement();
                    rs = stmt.executeQuery("SELECT id,tweet_text,parsed_text FROM `" + disease  +  "_tweets_users` WHERE lang <> 'en' ORDER BY id");
                     String path = "c:/profiles";
                    init(path);
                    int count = 0;
                    
                    //if the disease_tweets_users table is empty, insert
                    while (rs.next() ) {    
                            long id = rs.getLong("id");
                            String tweet_text = rs.getString("tweet_text");
                            
                           
                            //cybozu before normalize 
                            String cybozuLangDetected = detectLangCybozu(tweet_text);
                           
                            //cybozu after normalize
                            String cybozuLangDetectedAN = detectLangCybozu(preprocessLanguageDetect(tweet_text));
                            
                            //ms before normalize
                            String msLangDetected = detectLangMS(tweet_text);
                            
                            //ms after normalize
                            String msLangDetectedAN = detectLangMS(preprocessLanguageDetect(tweet_text));
                                 
                            System.out.println(tweet_text);
                            System.out.println(preprocessLanguageDetect(tweet_text));
                            System.out.println("Cybozu: " + cybozuLangDetected);
                            System.out.println("Cybozu(AN): " + cybozuLangDetectedAN);
                            System.out.println("MS: " + msLangDetected);
                            System.out.println("MS(AN): " + msLangDetectedAN);
                            
                            count++;
                            System.out.println(id);
                            System.out.println();
                    } 
                    
                    System.out.println(count);
                    
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
}