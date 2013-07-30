package analysis.toolkit.demo.sentiment.utils;

/**
* This class stores the positive score and the negative score
* @author Xiang Ji
* @version 1.0 07.24.2013
* @email xiangji2010@gmail.com
*/
public class SentimentSCore {
    public double positiveScore;
    public double negativeScore;
    
    public SentimentSCore(double pscore, double nscore) {
        positiveScore = pscore;
        negativeScore = nscore;
    }
    
    public SentimentSCore() {
        positiveScore = 0;
        negativeScore = 0;
    }
    
    public String scoreToString() {
        return ("PScore: " + positiveScore + ", NScore: " + negativeScore);
    }
}

