package analysis.toolkit.demo.sentiment.dictionary.liwc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTool {
	
	/**
	 * parses the file text into a list of words, 
	 * using regular expression pattern "\w+'?\w*"
	 * @param file the text file
	 * @return a string of well-parsed words
	 * @throws IOException
	 */
	static public final List<String> parseFile(File file) throws IOException{
		List<String> ret = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = br.readLine()) != null){
			ret.addAll(parseString(line));
		}
		return ret;
	}
	/**
	 * parses the string into a list of words,
	 * using regular expression pattern "\w+'?\w*"
	 * @param s a string of well-parsed words
	 * @return
	 */

	static public final List<String> parseString(String s){
		List<String> ret = new ArrayList<String>();
		Pattern p = Pattern.compile("\\w+'?\\w*");
		Matcher m = p.matcher(s);
		while(m.find()){
			ret.add(m.group());
		}
		return ret;
	}
}
