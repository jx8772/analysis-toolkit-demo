package analysis.toolkit.demo.sentiment.dictionary.ptsd;

import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.sentiment.dictionary.mpqa.MPQASentimentExtractor;
import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.utility.Utility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * This class provides is for using sentiment analysis on 200 annotated PTSD texts
 * @version 1.0 07/22/2013
 * @author Xiang Ji
 * @email xiangji2010@gmail.com
 */
public class TestInVPCA {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        BufferedWriter bw = Utility.getBufferedWriter("C:/workspace/data/roc_curve.txt");
        
        //if the PScore > threshold, the text is regarded as being from PTSD patient
        //double threshhold = 0.6;
        
        for(double threshhold = 0; threshhold <= 1; threshhold += 0.01) {
            SentimentSCore ss = new SentimentSCore();
            String filename = "./data/analysis/textanalytics/sentiment/ptsd/ptsd.txt";
            File file = new File(filename);
            BufferedReader input =  new BufferedReader(new FileReader(file));
            String line = null;
            int index = 0;
            String label = "";
            String predicted_label = "no";
            double positive_percent = 0;
            double negative_percent = 0;
            //for evaluation purpose
            double tp = 0;
            double fn = 0;
            double fp = 0;
            double tn = 0;

            //parse the file containing the sentiment dictionary
            HashMap<String, HashMap<String, String>> subject_clues = SentimentAnalysisUtils.parseMPQAFile("./data/analysis/textanalytics/sentiment/mpqa/subjclueslen1-HLTEMNLP05.tff");
            //parse the file containing a list of profanities
            Set<String> profanity_list = SentimentAnalysisUtils.parseProfanityListFile("./data/analysis/textanalytics/sentiment/profanity/profanity_list.txt");
            //parse the AFINN file
            HashMap<String, Integer> afinn_clues = SentimentAnalysisUtils.parseAFINNFile("./data/analysis/textanalytics/sentiment/afinn/AFINN-111.txt");
            //parse the taxonomy file
            Set<String> taxonomy_term_list = SentimentAnalysisUtils.parseTaxonomyTermFile("./data/analysis/textanalytics/sentiment/ptsd/vpca_taxonomy_terms.txt");
            
            MPQASentimentExtractor.adjustMPQATaxonomyTerm(subject_clues, taxonomy_term_list, profanity_list);
            
            try {
                while (( line = input.readLine()) != null){
                     String splits[] = line.split("\t");
                     String text = splits[0];
                     label = splits[1];
                     String processed_text = SentimentAnalysisUtils.preprocessSentimentAnalysis(text);
                     ss = MPQASentimentExtractor.getSentimentMPQATaxonomyTerm(text, subject_clues, profanity_list, taxonomy_term_list);
                     
                     //get the PScore part of result
                     positive_percent = ss.positiveScore;
                     negative_percent = ss.negativeScore;
                     if(positive_percent > threshhold)
                         predicted_label = "no";
                     else 
                         predicted_label = "yes";
                     
                     //System.out.println("label: " + label + ", predicted_label: " +predicted_label);
                     //System.out.println();
                     if(label.equals("yes") && predicted_label.equals("yes")) {
                         tp++;
                     } else if(label.equals("yes") && predicted_label.equals("no")) {
                         fn++;
                     } else if(label.equals("no") && predicted_label.equals("yes")) {
                         fp++;
                     } else if(label.equals("no") && predicted_label.equals("no")) {
                         tn++;
                     }
                     index++;
                }
            } finally {
                //System.out.println("tp: " + tp + ", fn: " + fn + ", fp: " + fp + ", tn: " + tn);
                double p=tp/(tp+fp);
                double r=tp/(tp+fn);
                double f= 2*p*r/(p+r);
                double fpr = Utility.getNDecimals(fp/(fp+tn),3);
                double tpr = Utility.getNDecimals(tp/(tp+fn),3);
                //display teh percentage of positive, higher the score, more positive the text is
                System.out.println("threshold: " + threshhold +", precision: " +p  + ", recall: " + r+", f: " + f);   
                
                //fpr and tpr are used to plot the ROC curve
                //bw.write(fpr + "\t" + tpr + "\n");
            }
        }
        bw.close();
    }
}
