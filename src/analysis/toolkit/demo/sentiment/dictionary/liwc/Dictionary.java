package analysis.toolkit.demo.sentiment.dictionary.liwc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analysis.toolkit.demo.sentiment.dictionary.liwc.lang.Category;
import analysis.toolkit.demo.sentiment.dictionary.liwc.lang.Result;
import analysis.toolkit.demo.sentiment.dictionary.liwc.util.StringTool;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dictionary Class
 * @author ytang
 * @author Xiang modified on 07.29.2013
 */
public class Dictionary {

	Map<String, Set<Integer>> dictionary = new HashMap<String, Set<Integer>>();
	Map<Integer, String> categoryMap = new LinkedHashMap<Integer, String>();
	List<Category> categories = new ArrayList<Category>();
	
	/**
	 * Constructor
	 * @param dictionaryFile
	 * @throws IOException
	 */
	public Dictionary(File dictionaryFile)throws IOException{
		initDictionary(dictionaryFile);
	}
	
	private void initDictionary(File file)throws IOException{
		 BufferedReader br = new BufferedReader(new FileReader(file));
		 String line = null;
		 int percentageSymbolCount = 0;
		 while((line = br.readLine()) != null){
			 line = line.trim();
			 if(line.contains("%")){
				 percentageSymbolCount ++;
				 continue;
			 }
			 String[] ss = line.replaceAll("\t", " ").split(" ");
			 //categories
			 if(percentageSymbolCount == 1){
				 int value = Integer.parseInt(ss[0].trim());
				 categoryMap.put(value, ss[1].trim());
				 continue;
			 }
			 else // entries
			 {
				 String word = ss[0].trim().toLowerCase();
				 Set<Integer> values = new HashSet<Integer>();
				
				 for(int i = 1; i < ss.length; i++){
					 values.add(Integer.parseInt(ss[i].trim()));
				 }
				 dictionary.put(word, values);
			 }
		 }
	}
	
	/**
	 * analyzes a list of well-parsed words
	 * @param strings a list of strings to analyze, which should be the parsed words.
	 * @return a Map containing the result: keys are categories; values are hits
	 */
	public Map<Category, Integer> analyze(List<String> strings){
		Map<Integer, Integer> m = new HashMap<Integer, Integer>();
		for(Integer i : categoryMap.keySet()){
			m.put(i, 0);
		}
		
		for(String s : strings){
			s = s.trim().toLowerCase();
			Set<Integer> values = dictionary.get(s);
			if(values == null){
				while(s.length() > 0){
					values = dictionary.get(s + "*");
					if(values != null){
						break;
					}
					s = s.substring(0, s.length() - 1);
				}
			}
			if(values != null){
				for(Integer i : values){
					int count = m.get(i);
					count++;
					m.put(i, count);
				}
			}
		}
		
		Map<Category, Integer> ret = new LinkedHashMap<Category, Integer>();
		for(Category c : getCategories()){
			int hit = m.get(c.getID());
			ret.put(c, hit);
		}
		
		return ret;
	}
	
	/**
	 * parses a string into words and then analyze them
	 * @param s a string to be parsed and analyzed
	 * @return the result
	 */
	public Result parseAndAnalyze(String s){
		List<String> strings = StringTool.parseString(s);
		Map<Category, Double> ret = new LinkedHashMap<Category, Double>();
		double count = strings.size();
		if(count > 0){
			Map<Category, Integer> m = analyze(strings);
			for(Category c : m.keySet()){
				int hit = m.get(c);
				ret.put(c,  (100 * hit)/count);
			}
		}else if(count == 0){ //added by Xiong
            Map<Category, Integer> m = analyze(strings);
			for(Category c : m.keySet()){
				ret.put(c,  0.);
			}
        }
		
		return new Result(strings.size(), ret);
	}
	
	/**
	 * parses the file text into strings and then analyze them
	 * @param file text file
	 * @return the result
	 * @throws IOException
	 */
	public Result parseAndAnalyze(File file)throws IOException{
		List<String> strings = StringTool.parseFile(file);
		Map<Category, Double> ret = new LinkedHashMap<Category, Double>();
		double count = strings.size();
		if(count > 0){
			Map<Category, Integer> m = analyze(strings);
			for(Category c : m.keySet()){
				int hit = m.get(c);
				ret.put(c,  (100 * hit)/count);
			}
		}else if(count == 0){ //added by Xiong
            Map<Category, Integer> m = analyze(strings);
			for(Category c : m.keySet()){
				ret.put(c,  0.);
			}
        }
		return new Result(strings.size(), ret);
	}
	
	/**
	 * gets a category
	 * @param id category id
	 * @return the category 
	 */
	public Category getCategory(int id){
		Category c = null;
		String name = categoryMap.get(id);
		if(name != null){
			c = new Category(name, id);
		}
		return c;
	}
	
	/**
	 * get all the categories defined in the dictionary
	 * @return
	 */
	public List<Category> getCategories(){
		if(categories.size() == 0){
			for(Integer i : categoryMap.keySet()){
				categories.add(new Category(categoryMap.get(i), i));
			}
		}
		return categories;
	}
        
        public Map<String, Set<Integer>> getDictionary() {
            return dictionary;
        }
        
        public static HashMap<String, HashMap<String, String>> parseMPQAFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        BufferedReader input =  new BufferedReader(new FileReader(file));
        String line = "";
        int index = 0;
        HashMap<String, HashMap<String, String>> subject_clues = new HashMap<String, HashMap<String, String>>();
        
        try {
            while (( line = input.readLine()) != null){
                String[] splits = line.split(" ");
                String word = "";
                HashMap<String, String> entry = new HashMap<String, String>();
                
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
        } finally {
            //System.out.println(index + "MPQA clues parsed");
            return subject_clues;
        }
    }
}
