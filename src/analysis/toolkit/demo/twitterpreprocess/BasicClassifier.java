package analysis.toolkit.demo.twitterpreprocess;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BasicClassifier {

	public static final String DB_NAME = "emoticon2";
	
	public static void pnClassify(String train_table, String test_table) throws Exception {
		
		 Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 Statement stmt3 = null;
		 Statement stmt4 = null;
		 Statement stmt5 = null;
		 ResultSet rs = null;
		 ResultSet rs2 = null;
		 ResultSet subrs = null; 
		 ResultSet subrs2 = null; 
		
		 try {
				Class.forName("com.mysql.jdbc.Driver");
					
				conn = DriverManager.getConnection("jdbc:mysql://localhost/" + DB_NAME + "?user=root");
				
				//in the test_table, the first appearance of obj MUST PRECEDE the first appear of subj!!! be compatible with training data
				stmt3 = conn.createStatement();
				stmt4 = conn.createStatement();
				stmt3.executeUpdate("UPDATE " + test_table + " SET subj_obj = 'subj'");
				stmt4.executeUpdate("UPDATE " + test_table + " SET subj_obj = 'obj' LIMIT 2");
				
				// retrieve train and test data
				 InstanceQuery query = new InstanceQuery();
				 query.setUsername("root");
				 query.setPassword("");
				 query.setQuery("select parsed_text, subj_obj from " + train_table);
				 Instances train_data = query.retrieveInstances();
				 query.setQuery("select parsed_text, subj_obj from " + test_table);
				 Instances test_data = query.retrieveInstances();
				 train_data.setClassIndex(train_data.numAttributes()-1);
				 test_data.setClassIndex(test_data.numAttributes()-1);
				 
				 //filter
				 String[] options = new String[2];
				 options[0] = "-C";
				 options[1] = "1";
				 NominalToString nts = new NominalToString();
				 nts.setOptions(options);
				 
				 // apply filter to train_data and test_data, nominal to string
				 nts.setInputFormat(train_data);
				 Instances string_train_data = Filter.useFilter(train_data, nts);
				 nts.setInputFormat(test_data);
				 Instances string_test_data = Filter.useFilter(test_data, nts);
				 
				 // filteredclassier
				 FilteredClassifier fc = new FilteredClassifier();
				 NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
				 StringToWordVector stwv = new StringToWordVector();
				 stwv.setAttributeIndices("1");
				 fc.setFilter(stwv);
				 fc.setClassifier(nbm);
				 
				 //train and make predictions
				 fc.buildClassifier(string_train_data);
				
				
				// put the predictive result into table 
				stmt = conn.createStatement();
				stmt2 = conn.createStatement();
				String query_test_data_w_id = "select tweet_id, parsed_text, subj_obj from " + test_table;
				rs = stmt.executeQuery(query_test_data_w_id);
				int count = 0;
				while(rs.next()) {
					String tweet_id  = rs.getString("tweet_id");
					double pred = fc.classifyInstance(string_test_data.instance(count));
					String predicted = string_test_data.classAttribute().value((int) pred);
					String updatesql = "UPDATE " + test_table + " SET predict_subj_obj = '" + predicted + "' " + "WHERE tweet_id = '" + tweet_id + "'";
					stmt2.executeUpdate(updatesql);
					count++;
				}
		 }catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException ex) {
				// handle any errors
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
			} finally {
				try {
					if(rs != null) {
						rs.close();
						rs = null;
					}
					if(stmt != null) {
						stmt.close();
						stmt = null;
					}
					if(conn != null) {
						conn.close();
						conn = null;
					}
					if(subrs != null) {
						subrs.close();
						subrs = null;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
	}
	
	public static void nnClassify(String train_table, String test_table) throws Exception {
	
		 Connection conn = null;
		 Statement stmt = null;
		 Statement stmt2 = null;
		 Statement stmt3 = null;
		 Statement stmt4 = null;
		 ResultSet rs = null;
		 ResultSet subrs = null;
		 
		 try {
			 Class.forName("com.mysql.jdbc.Driver");
				
			conn = DriverManager.getConnection("jdbc:mysql://localhost/" + DB_NAME + "?user=root");
				
			//fill some dummy value in neg_neu (existence of missing value will lead to different result of fc.classifyInstance())
			//stmt3 = conn.createStatement();
			//stmt4 = conn.createStatement();
			//stmt3.executeUpdate("UPDATE " + test_table + " SET neg_neu = 'neg'");
			//stmt4.executeUpdate("UPDATE " + test_table + " SET neg_neu = 'neu' LIMIT 2");
			
			/*
			// retrieve train and test data
			 InstanceQuery query = new InstanceQuery();
			 query.setUsername("root");
			 query.setPassword("");
			 query.setQuery("select parsed_text, neg_neu from " + train_table);
			 Instances train_data = query.retrieveInstances();
			 query.setQuery("select parsed_text, neg_neu from " + test_table);
			 Instances test_data = query.retrieveInstances();
			 train_data.setClassIndex(train_data.numAttributes()-1);
			 test_data.setClassIndex(test_data.numAttributes()-1);
			 */
			
			BufferedReader reader = new BufferedReader(new FileReader("F:\\Weka-3-6\\train_tweets_sentiment_bal_std.arff"));
			Instances train_data = new Instances(reader);
			reader.close();
			
			BufferedReader reader2 = new BufferedReader(new FileReader("F:\\Weka-3-6\\test_tweets_sentiment_bal_std.arff"));
			Instances test_data = new Instances(reader2);
			reader2.close();
			
			train_data.setClassIndex(0);
			 test_data.setClassIndex(0);
			
			/*
			 //filter
			 String[] options = new String[2];
			 options[0] = "-C";
			 options[1] = "1";
			 NominalToString nts = new NominalToString();
			 nts.setOptions(options);
			 
			 
			 // apply filter to train_data and test_data, nominal to string
			 nts.setInputFormat(train_data);
			 Instances string_train_data = Filter.useFilter(train_data, nts);
			 nts.setInputFormat(test_data);
			 Instances string_test_data = Filter.useFilter(test_data, nts);
			 
			 // filteredclassier
			 FilteredClassifier fc = new FilteredClassifier();
			 NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
			 StringToWordVector stwv = new StringToWordVector();
			 stwv.setAttributeIndices("1");
			 fc.setFilter(stwv);
			 fc.setClassifier(nbm);
			 */
			
			
			 //train and make predictions
			 //fc.buildClassifier(string_train_data);
			NaiveBayesMultinomial nbm = new NaiveBayesMultinomial();
			nbm.buildClassifier(train_data);
			
			/*
				stmt = conn.createStatement();
				stmt2 = conn.createStatement();
				//String query_test_data_w_id = "select tweet_id, parsed_text, neg_neu from " + test_table + " WHERE predict_subj_obj = 'subj'";
				String query_test_data_w_id = "select tweet_id, parsed_text, neg_neu from " + test_table;
				rs = stmt.executeQuery(query_test_data_w_id);
				int count = 0;
				while(rs.next()) {
					String tweet_id  = rs.getString("tweet_id");
					double pred = fc.classifyInstance(string_test_data.instance(count));
					String predicted = string_test_data.classAttribute().value((int) pred);
					String updatesql = "UPDATE " + test_table + " SET predict_neg_neu = '" + predicted + "' " + "WHERE tweet_id = '" + tweet_id + "'";
					stmt2.executeUpdate(updatesql);
					count++;
				}
				*/
				
			 	
			 
				Evaluation eval = new Evaluation(train_data);
				 eval.evaluateModel(nbm, test_data);
				 System.out.println(eval.toSummaryString("\nResults\n======\n", false));
				 
				
	          double[][] cmMatrix = eval.confusionMatrix();
	          for(int row_i=0; row_i<cmMatrix.length; row_i++){
	              for(int col_i=0; col_i<cmMatrix.length; col_i++){
	                  System.out.print(cmMatrix[row_i][col_i]);
	                  System.out.print("|");
	              }
	              System.out.println();
	          }
		 }catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException ex) {
				// handle any errors
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
			} finally {
				try {
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
					if(subrs != null) {
						subrs.close();
						subrs = null;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
	}
	
	public static void main(String[] args) throws Exception {
		 //pnClassify("pn_emoticon_train","pn_td_test");
		 
		 nnClassify("train_tweets_sentiment_bal", "test_tweets_sentiment_bal");
		 
		 return;
	}
	
	
	
}
