/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iai.minerva.analysis.sentiment;

import com.cybozu.labs.langdetect.LangDetectException;
import com.iai.minerva.analysis.twitterprocess.OntologyProcess;
import com.iai.minerva.analysis.utility.Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SentimentAnalysis {
    public static void main(String[] args) throws FileNotFoundException {
        HashMap<String, HashMap<String, String>> subject_clues = parseFile(".\\data\\analysis\\textanalytics\\subjclueslen1-HLTEMNLP05.tff");
        String text = "Oh I know why I'm so angry... it's frustration. Frustration at the fact that I have to rely on someone to take care of me, frustrated that I can't stand to look in the mirror because of the scars, frustrated that I need at least 2 more surgeries to try to fix my face, frustrated that I have PTSD. That's why I'm so damn angry, and everytime I try to sit down and \"fix\" some of my frustrations, I realize that most of the issues are to huge for me to deal with right now wow... that's a load off my chest...";
        getSentiment(text, subject_clues);
        
    }
    
    private static HashMap<String, HashMap<String, String>> parseFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        BufferedReader input =  new BufferedReader(new FileReader(file));
        String line = null;
        String sub_line = null;
        int index = 0;
        String record = "";
        HashMap<String, HashMap<String, String>> subject_clues = new HashMap<String, HashMap<String, String>>();
        
        try {
            while (( line = input.readLine()) != null){
                String[] splits = line.split(" ");
                String word = "";
                HashMap<String, String> entry = new HashMap<String, String>();
                
                for(String split : splits) {
                    String key = split.split("=")[0];
                    String value = split.split("=")[1];
                    if(key.equals("word1"))
                        word = value;
                    entry.put(key, value);
                }
                subject_clues.put(word, entry);
                index++;
            }
        } catch (IOException ex) {
            Logger.getLogger(OntologyProcess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println(index + " clues parsed");
            return subject_clues;
        }
    }
    
    public static String getSentiment(String message, HashMap<String, HashMap<String, String>> subject_clues) {
        String result;
        double numPos = 0;
        double numNeg = 0;
        double numNeu = 0;
        double nonSubjective = 0;
        double pos = 0;
        double neg = 0;
        String preprocessed = "";
        
        try {
            preprocessed = Utility.preprocessSentimentAnalysisPTSD(message);
            System.out.println(message);
            System.out.println(preprocessed);
            String[] tokens = preprocessed.split(" ");
            for (String token : tokens) {
                String label = lookUpDictionary(token, subject_clues);
                if(label.equals("positive")) {
                    numPos++;
                    System.out.println("pos: " + token);
                    continue;
                } else if (label.equals("negative")) {
                    numNeg++;
                    System.out.println("neg: " + token);
                    continue;
                } else if (label.equals("neutral")) {
                    numNeu++;
                    System.out.println("neu: " + token);
                    continue;
                } else {
                    nonSubjective++;
                }
            }
            System.out.println(numPos + "||" + numNeg + "||" + numNeu + "||" + nonSubjective);
            
        } catch (LangDetectException ex) {
            Logger.getLogger(SentimentAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        pos = Utility.getNDecimals(numPos/(numPos+numNeg), 2);
        neg = Utility.getNDecimals(numNeg/(numPos+numNeg), 2);
        result = "PScore:"+pos+", NScore:"+neg;
        System.out.println(result);
        return result;
    }
    
    public static String lookUpDictionary(String word, HashMap<String, HashMap<String, String>> subject_clues) {
        if(subject_clues.containsKey(word)) {
            return subject_clues.get(word).get("priorpolarity");
        } else {
            return "";
        }
    }
}
