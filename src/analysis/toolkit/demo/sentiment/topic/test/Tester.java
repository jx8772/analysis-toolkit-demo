package analysis.toolkit.demo.sentiment.topic.test;

import analysis.toolkit.demo.sentiment.dictionary.afinn.test.*;
import analysis.toolkit.demo.sentiment.dictionary.liwc.test.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import analysis.toolkit.demo.sentiment.dictionary.liwc.Dictionary;
import analysis.toolkit.demo.sentiment.dictionary.liwc.lang.Category;
import analysis.toolkit.demo.sentiment.dictionary.liwc.lang.Result;
import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import analysis.toolkit.demo.sentiment.dictionary.liwc.util.StringTool;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;
import analysis.toolkit.demo.sentiment.dictionary.afinn.AFINNSentimentExtractor;
import analysis.toolkit.demo.sentiment.topic.TopicSentimentExtractor;

/**/
public class Tester {
	public static void main(String[] args) throws FileNotFoundException {
            SentimentSCore ss = new SentimentSCore();
            
            //parse the file containing the sentiment dictionary
            //the file cna be downloaded from http://mpqa.cs.pitt.edu/lexicons/subj_lexicon/
            HashMap<String, HashMap<String, String>> subject_clues = SentimentAnalysisUtils.parseMPQAFile("./data/analysis/textanalytics/sentiment/mpqa/subjclueslen1-HLTEMNLP05.tff");
            
            //parse the file containing a list of profanities
            //the file can be downloaded from https://github.com/shutterstock/List-of-Dirty-Naughty-Obscene-and-Otherwise-Bad-Words/blob/master/en
            Set<String> profanity_list = SentimentAnalysisUtils.parseProfanityListFile("./data/analysis/textanalytics/sentiment/profanity/profanity_list.txt");
            
            //create a demo keywork weight hashmap
            HashMap<String, Double> kw = new HashMap<String, Double>();
            kw.put("lost", 100.0);
            kw.put("angry", 90.0);
            kw.put("alone", 80.0);
            kw.put("ignoring", 70.0);
            kw.put("feeling", 50.0);
            
            ss = TopicSentimentExtractor.getWeightedTopicSentimentMPQA("I am feeling lost and alone today. My girlfriend got angry at me because she thought I was ignoring her while she was talking, but really I was having a flashback and couldn't even hear her. I didn't even realize she was there. I tried to tell her that I wasn't ignoring her and that I was in a flashback but she just said I was lying and said that she is tired of my flashbacks. I hate this, I feel like nothing I do will make these flashbacks go away. I've almost accepted them as a part of myself, and I hate that. This is the first time in a very long time that I feel like I just want to give up and give in.I am feeling lost and alone today.", subject_clues, profanity_list, kw);
            //ss = AFINNSentimentExtractor.getSentimentAFINNTaxonomyTerm("I am feeling lost and alone today. My girlfriend got angry at me because she thought I was ignoring her while she was talking, but really I was having a flashback and couldn't even hear her. I didn't even realize she was there. I tried to tell her that I wasn't ignoring her and that I was in a flashback but she just said I was lying and said that she is tired of my flashbacks. I hate this, I feel like nothing I do will make these flashbacks go away. I've almost accepted them as a part of myself, and I hate that. This is the first time in a very long time that I feel like I just want to give up and give in.I am feeling lost and alone today.",afinn_clues, taxonomy_term_list);
            System.out.println(ss.scoreToString());
	}
}
