package analysis.toolkit.demo.sentiment.dictionary.mpqa.test;

import analysis.toolkit.demo.sentiment.dictionary.afinn.test.*;
import analysis.toolkit.demo.sentiment.dictionary.liwc.test.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import analysis.toolkit.demo.sentiment.dictionary.liwc.Dictionary;
import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import analysis.toolkit.demo.sentiment.dictionary.liwc.util.StringTool;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;
import analysis.toolkit.demo.sentiment.dictionary.afinn.AFINNSentimentExtractor;
import analysis.toolkit.demo.sentiment.dictionary.mpqa.MPQASentimentExtractor;
import java.io.IOException;
import java.util.Arrays;

/**/
public class Tester {
	public static void main(String[] args) throws FileNotFoundException, IOException {
            //parse the file containing the sentiment dictionary
            //the file cna be downloaded from http://mpqa.cs.pitt.edu/lexicons/subj_lexicon/
            HashMap<String, HashMap<String, String>> subject_clues = SentimentAnalysisUtils.parseMPQAFile("./data/analysis/textanalytics/sentiment/mpqa/subjclueslen1-HLTEMNLP05.tff");

            //parse the taxonomy file
            //for PTSD project
            Set<String> taxonomy_term_list = SentimentAnalysisUtils.parseTaxonomyTermFile("./data/analysis/textanalytics/sentiment/ptsd/vpca_taxonomy_terms.txt");
            SentimentSCore ss = new SentimentSCore();
            SentimentSCore ss2 = new SentimentSCore();
            
            //parse the file containing a list of profanities
            //the file can be downloaded from https://github.com/shutterstock/List-of-Dirty-Naughty-Obscene-and-Otherwise-Bad-Words/blob/master/en
            Set<String> profanity_list = SentimentAnalysisUtils.parseProfanityListFile("./data/analysis/textanalytics/sentiment/profanity/profanity_list.txt");
            
            String test1 = "the url should be followed by a string,  such as “I know why I'm so angry... it's frustration. Frustration at the fact that I have to rely on someone to take care of me, frustrated that I can't stand to look in the mirror because of the scars, frustrated that I need at least 2 more surgeries to try to fix my face, frustrated that I have PTSD. That's why I'm so damn angry, and everytime I try to sit down and \"fix\" some of my frustrations, I realize that most of the issues are to huge for me to deal with right now wow... that's a load off my chest...";
            String test2 = "I am feeling lost and alone today. My girlfriend got angry at me because she thought I was ignoring her while she was talking, but really I was having a flashback and couldn't even hear her. I didn't even realize she was there. I tried to tell her that I wasn't ignoring her and that I was in a flashback but she just said I was lying and said that she is tired of my flashbacks. I hate this, I feel like nothing I do will make these flashbacks go away. I've almost accepted them as a part of myself, and I hate that. This is the first time in a very long time that I feel like I just want to give up and give in.I am feeling lost and alone today.";
            String test3 = "I am scared really truly scared my wife and also my friend's think I am joking. Also I am scared I may be banned from here if I prefix I am sorry I do not know what that word means. Funny thing most people believe that I have a college degree. Tonight is a very bad one due to lack of sleep in I don't want to be scared anymore.";
            
            ss = MPQASentimentExtractor.getSentimentMPQA(new ArrayList<String>(Arrays.asList(test3)), subject_clues, profanity_list);
            ss2 = MPQASentimentExtractor.getSentimentMPQATaxonomyTerm(test3, subject_clues, profanity_list, taxonomy_term_list);
            System.out.println(ss.scoreToString());
            System.out.println(ss2.scoreToString());
	}
}
