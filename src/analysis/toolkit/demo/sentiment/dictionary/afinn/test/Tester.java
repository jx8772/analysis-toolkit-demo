package analysis.toolkit.demo.sentiment.dictionary.afinn.test;

import analysis.toolkit.demo.sentiment.utils.SentimentAnalysisUtils;
import analysis.toolkit.demo.sentiment.utils.SentimentSCore;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;
import analysis.toolkit.demo.sentiment.dictionary.afinn.AFINNSentimentExtractor;

/**/
public class Tester {
	public static void main(String[] args) throws FileNotFoundException {
            //parse the AFINN file (an alternative of MPQA subjective clues), the file can be downloaded from 
            //https://github.com/uwescience/datasci_course_materials/blob/master/assignment1/AFINN-111.txt
            //http://www2.imm.dtu.dk/pubdb/views/publication_details.php?id=6010
            HashMap<String, Integer> afinn_clues = SentimentAnalysisUtils.parseAFINNFile("./data/analysis/textanalytics/sentiment/afinn/AFINN-111.txt");

            //parse the taxonomy file
            //for PTSD project
            Set<String> taxonomy_term_list = SentimentAnalysisUtils.parseTaxonomyTermFile("./data/analysis/textanalytics/sentiment/ptsd/vpca_taxonomy_terms.txt");
            SentimentSCore ss = new SentimentSCore();
            
            String test1 = "the url should be followed by a string,  such as “I know why I'm so angry... it's frustration. Frustration at the fact that I have to rely on someone to take care of me, frustrated that I can't stand to look in the mirror because of the scars, frustrated that I need at least 2 more surgeries to try to fix my face, frustrated that I have PTSD. That's why I'm so damn angry, and everytime I try to sit down and \"fix\" some of my frustrations, I realize that most of the issues are to huge for me to deal with right now wow... that's a load off my chest...";
        String test2 = "I am feeling lost and alone today. My girlfriend got angry at me because she thought I was ignoring her while she was talking, but really I was having a flashback and couldn't even hear her. I didn't even realize she was there. I tried to tell her that I wasn't ignoring her and that I was in a flashback but she just said I was lying and said that she is tired of my flashbacks. I hate this, I feel like nothing I do will make these flashbacks go away. I've almost accepted them as a part of myself, and I hate that. This is the first time in a very long time that I feel like I just want to give up and give in.I am feeling lost and alone today.";
        String test3 = "I am scared really truly scared my wife and also my friend's think I am joking. Also I am scared I may be banned from here if I prefix I am sorry I do not know what that word means. Funny thing most people believe that I have a college degree. Tonight is a very bad one due to lack of sleep in I don't want to be scared anymore.";
            
            ss = AFINNSentimentExtractor.getSentimentAFINN("I am feeling lost and alone today. My girlfriend got angry at me because she thought I was ignoring her while she was talking, but really I was having a flashback and couldn't even hear her. I didn't even realize she was there. I tried to tell her that I wasn't ignoring her and that I was in a flashback but she just said I was lying and said that she is tired of my flashbacks. I hate this, I feel like nothing I do will make these flashbacks go away. I've almost accepted them as a part of myself, and I hate that. This is the first time in a very long time that I feel like I just want to give up and give in.I am feeling lost and alone today.",afinn_clues);
            //ss = AFINNSentimentExtractor.getSentimentAFINNTaxonomyTerm("I am feeling lost and alone today. My girlfriend got angry at me because she thought I was ignoring her while she was talking, but really I was having a flashback and couldn't even hear her. I didn't even realize she was there. I tried to tell her that I wasn't ignoring her and that I was in a flashback but she just said I was lying and said that she is tired of my flashbacks. I hate this, I feel like nothing I do will make these flashbacks go away. I've almost accepted them as a part of myself, and I hate that. This is the first time in a very long time that I feel like I just want to give up and give in.I am feeling lost and alone today.",afinn_clues, taxonomy_term_list);
            System.out.println(ss.scoreToString());
	}
}
