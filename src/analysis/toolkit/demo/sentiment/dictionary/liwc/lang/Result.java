package analysis.toolkit.demo.sentiment.dictionary.liwc.lang;

import java.util.Collections;
import java.util.Map;

public class Result {
	private int wordCount;
	private Map<Category, Double> distribution;
	
	public Result(int count, Map<Category, Double> d){
		this.wordCount = count;
		this.distribution = d;
	}
	
	/**
	 * gets the word count
	 * @return
	 */
	public int getWordCount() {
		return wordCount;
	}

	/**
	 * gets the hit percentage distribution map,
	 *  whose keys are the categories and the values are the hit percentages
	 * @return the hit percentage distribution
	 */
	public Map<Category, Double> getDistribution() {
		return Collections.unmodifiableMap(distribution);
	}
}
