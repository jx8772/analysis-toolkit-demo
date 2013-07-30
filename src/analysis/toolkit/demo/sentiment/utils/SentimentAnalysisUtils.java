package analysis.toolkit.demo.sentiment.utils;

import com.cybozu.labs.langdetect.LangDetectException;
import analysis.toolkit.demo.twitterprocess.OntologyProcess;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides utility function for sentiment analysis
 * @version 1.0 07/22/2013
 * @author Xiang Ji
 * @email xiangji2010@gmail.com
 */
public class SentimentAnalysisUtils {
    public static String preprocessSentimentAnalysis(String s) {
        //replace double quotes with " "
        s = s.replace("\"", " ");

        //repeating spaces
        s = s.replaceAll(" ", "    ");

        //replace "&amp" with "&", replace "&lt" with "<", replace "&gt" with ">"
        s = s.replaceAll("&amp", " ").replaceAll("&lt", " ").replaceAll("&gt", " ");

        //replce xx... with blank
        s = s.replaceAll("(\\S+)(\\.\\.\\.|…)(\\s+|$+)", " $1 ");

        //delete independent numbers "2", "34" not "1st"
        s = s.replaceAll("(^+|\\s+)(\\d+)(\\s+|$+)", " ");

        // replace actual url address with the word " "
        s = s.replaceAll("(^+|\\s+)http.*?(\\s+|$+)", " ");

        //replace double "/#<>;:.,=()~&[]{}-_"  with " "
        s = s.replaceAll("/|—|#|<|>|;|:|\\.|\\,|=|\\(|\\)|~|&|\\[|\\]|\\{|\\}|\\-|\\_", " ");

        //replace double "ab's"  with "ab"
        s = s.replaceAll("(\\S+)('|’)s*(\\s|$+)", " $1 ");

        //replace | with " "
        s = s.replace("|", " ");

        //replace / with " ", replace \ with " ", replace * with " "
        s = s.replace("\\", " ").replace("/", " ").replace("*", " ");

        //replace repeated spaces and punctuation (!?) signs by one
        s = s.replaceAll("(\\!|\\?)\\1+", " ");

        //separate !? and the preceding text
        s = s.replaceAll("(\\!|\\?)", " ");

        //remove repeating spaces
        s = s.replaceAll("( )\\1+", " ");

        //lower case
        s = s.toLowerCase();

        //removing trailing and leading spaces
        s = s.trim();

        return s;
    }
    
     /**
    * This function reads the a word, and search the word to find its prior polarity and type
    * @param word the word to be searched
    * @param subject_clues the MPQA 
    */
    public static DictionaryLabel lookUpDictionary(String word, HashMap<String, HashMap<String, String>> subject_clues) {
        DictionaryLabel dl = new DictionaryLabel();
        
        if(subject_clues.containsKey(word)) {
            dl.priorPolarity = Integer.parseInt(subject_clues.get(word).get("priorpolarity"));
            dl.type = Integer.parseInt(subject_clues.get(word).get("type"));
            dl.isSet = true;
        } 
        return dl;
    }
    
    /**
    * This function reads the MPQA file, and store it into a hash map
    * @param filename the location of MPQA file
    */
    public static HashMap<String, HashMap<String, String>> parseMPQAFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        BufferedReader input =  new BufferedReader(new FileReader(file));
        String line = "";
        int index = 0;
        HashMap<String, HashMap<String, String>> subject_clues = new HashMap<>();
        
        try {
            while (( line = input.readLine()) != null){
                String[] splits = line.split(" ");
                String word = "";
                HashMap<String, String> entry = new HashMap<>();
                
                for(String split : splits) {
                    String[] pair = split.split("=");
                    String key = pair[0];
                    String value = pair[1];
                    //use the word after "=" as the key, the entry is the value
                    if(key.equals("word1"))
                        word = value;
                    //insert a new pair into hashmap, the word before "=" is the key, the word after "=" is the value
                    entry.put(key, value);
                }
                subject_clues.put(word, entry);
                index++;
            }
        } catch (IOException ex) {
            Logger.getLogger(OntologyProcess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //System.out.println(index + "MPQA clues parsed");
            return subject_clues;
        }
    }
    
    /**
    * This function reads the profanity list file, and store it into a set of strings
    * @param filename the location of profanity file
    */
    public static Set<String> parseProfanityListFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        BufferedReader input =  new BufferedReader(new FileReader(file));
        String line = "";
        int index = 0;
        Set<String> profanity_list = new TreeSet<>();
        
        try {
            while (( line = input.readLine()) != null){
                profanity_list.add(line);
                index++;
            }
        } catch (IOException ex) {
            Logger.getLogger(OntologyProcess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return profanity_list;
        }
    }
    
    /**
    * This function reads the PTSD taxonomy term file, and store it into a set of strings
    * @param filename the location of PTSD taxonomy term file
    */
    public static Set<String> parseTaxonomyTermFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        BufferedReader input =  new BufferedReader(new FileReader(file));
        String line = "";
        int index = 0;
        Set<String> taxonomy_term_list = new TreeSet<String>();
        
        try {
            while (( line = input.readLine()) != null){
                taxonomy_term_list.add(line);
                index++;
            }
        } catch (IOException ex) {
            Logger.getLogger(OntologyProcess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return taxonomy_term_list;
        }
    }
    
    /**
    * This function reads the AFINN file, and store it into a hash map
    * @param filename the location of AFINN file
    */
    public static HashMap<String, Integer> parseAFINNFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        BufferedReader input =  new BufferedReader(new FileReader(file));
        String line = "";
        HashMap<String, Integer> afinn_clues = new HashMap<String, Integer>();
        
        try {
            while (( line = input.readLine()) != null){
                String[] splits = line.split("\t");
                String key = splits[0];
                int value = Integer.parseInt(splits[1]);
                //System.out.println("key: " + key + ", value: " + value);;   
                afinn_clues.put(key, value);
            }
        } catch (IOException ex) {
            Logger.getLogger(OntologyProcess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //System.out.println(index + "AFINN clues parsed");
            return afinn_clues;
        }
    }
}
