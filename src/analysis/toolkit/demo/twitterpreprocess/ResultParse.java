package analysis.toolkit.demo.twitterpreprocess;

import com.iai.minerva.analysis.utility.Utility;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ResultParse {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        ArrayList<HashMap<String, Double>> topicHashList = parseMalletCompositionFile("C:\\mallet-2.0.7\\libya-output\\data_compostion.txt", 20);
        printTopicTimeline(topicHashList, 19);
    }
    
    public static ArrayList<HashMap<String, Double>> parseMalletCompositionFile (String filename, int num_topics) throws FileNotFoundException, IOException {
        FileInputStream fstream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        int count = -1;
        ArrayList<HashMap<String, Double>> topicHashList = new ArrayList<HashMap<String, Double>>();
        for(int i=0; i<num_topics;i++) {
              topicHashList.add(new HashMap<String, Double>());
        }
        
        while((strLine = br.readLine()) != null) {
            if(count == -1) { //skip the first line
               count++;
               continue;
            }
            String[] splits = strLine.split("\t");
            String[] fileDirectory = splits[1].split("/");
            String date = fileDirectory[fileDirectory.length-1];
            date = date.substring(0,date.indexOf('.'));
            
            //get top 10 topics for one doc
            for(int index = 2; index < 11*2; index=index+2) {
                topicHashList.get(Integer.parseInt(splits[index])).put(date, Utility.getNDecimals(Double.parseDouble(splits[index+1]), 2));
            }
            count++;
        }
        return topicHashList;
         
    }
    
    public static void printTopicTimeline(ArrayList<HashMap<String, Double>> topicHashList, int topic_id) {
        HashMap<String, Double> th = topicHashList.get(topic_id);
        String min_date = Collections.min(th.keySet());
        String max_date = Collections.max(th.keySet());
        String[] dates = Utility.getDatesBetween(min_date, max_date);
        
        for(int i = 0; i < dates.length; i++) {
            String date = dates[i];
            if(th.containsKey(date)) {
                System.out.println(date + "||" + th.get(date));
            } else {
                System.out.println(date + "||" + 0);
            }
            System.out.println();
        }
    }
}
