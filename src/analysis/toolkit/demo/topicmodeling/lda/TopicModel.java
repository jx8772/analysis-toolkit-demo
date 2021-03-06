/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.topicmodeling.lda;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import com.cybozu.labs.langdetect.LangDetectException;
import analysis.toolkit.demo.twitterprocess.DataTransform;
import analysis.toolkit.demo.twitterprocess.Preprocess;
import analysis.toolkit.demo.utility.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

public class TopicModel {
    private static String task;
    
    public static void main(String[] args) throws IOException {
        task = "libya";
        
        InstanceList instances = readDirectory(".\\data\\analysis\\textanalytics\\topmodel_temp_sampled");
        ParallelTopicModel model = buildTopicModel(instances, 20, 1.0, 0.05, 8, 50);
        topicEstimate(model, instances);
        
        //Utility.mongoToFile("data", task, ".\\data\\analysis\\textanalytics\\topmodel_temp\\" + task, 2011, 3, 15);
        //Utility.mongoToFileWithRange("data", task, ".\\data\\analysis\\textanalytics\\topmodel_temp\\" + task, "2012-11-02", "2012-11-16");
        return;
    }
    
    public static void writeDirectory(String directory) {
        
    }
    
    public static void clearDirectory (String directory) {
        try {
            File file = new File(directory);
            FileUtils.cleanDirectory(file);
        } catch (IOException ex) {
            Logger.getLogger(TopicModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static InstanceList readDirectory(String directory) {
        MalletImportData importer = new MalletImportData();
        InstanceList instances = importer.readDirectory(new File(directory));
        return instances;
    }
    
    public static ParallelTopicModel buildTopicModel (InstanceList instances, int numTopics, double alphaSum, double beta_w, int numThreads, int numIterations) {
        ParallelTopicModel model = new ParallelTopicModel(numTopics, alphaSum, beta_w);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(numThreads);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(numIterations);
        
        return model;
    }
    
    //currently output results to the console
    public static void topicEstimate(ParallelTopicModel model, InstanceList instances) throws IOException {
        model.estimate();
        
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData(); //get the features (index) of the first document, the word-index pair can be found in tokens.dictionary.entries
        LabelSequence topics = model.getData().get(0).topicSequence;
        
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        for (int position = 0; position < tokens.getLength(); position++) { //tokens.getLength() is the length of that document
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        }
        System.out.println(out); //each feature correspondes to which topic: topics.features
       
        for(int i =0; i < 1; i++) {
            // Estimate the topic distribution of the first instance, 
            //  given the current Gibbs state.
            double[] topicDistribution = model.getTopicProbabilities(i);

            // Get an array of sorted sets of word ID/count pairs
            ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

            // Show top 5 words in topics with proportions for the first document
            for (int topic = 0; topic < model.numTopics; topic++) {
                Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

                out = new Formatter(new StringBuilder(), Locale.US);
                out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
                int rank = 0;
                while (iterator.hasNext() && rank < 10) {
                    IDSorter idCountPair = iterator.next();
                    out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                    rank++;
                }
                System.out.println(out);
                
            }
            System.out.println();
        }
    }
}
