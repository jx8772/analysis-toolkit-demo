/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package analysis.toolkit.demo.twitterprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OntologyProcess {
    
    public static final String keyword = "polio";
    
    static public void main(String[] args) throws FileNotFoundException {
        HashMap<String, HashMap<String, ArrayList<String>>> ontology = parseOboFile("C:/workspace/hscb/TwitterProcessPipeline/HumanDO_xp.obo");
        Set<String> keywords = new TreeSet<String>();
        expandKeyword(ontology, keyword, keywords);
        Set<String> tokens = splitKeyword(keywords);
        System.out.println("keywords:");
        printSet(keywords);
        System.out.println();
        printSet(tokens);
        
        return;
    }
    
    private static HashMap<String, HashMap<String, ArrayList<String>>> parseOboFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        BufferedReader input =  new BufferedReader(new FileReader(file));
        String line = null;
        String sub_line = null;
        int index = 0;
        String record = "";
        HashMap<String, HashMap<String, ArrayList<String>>> ontology = new HashMap<String, HashMap<String, ArrayList<String>>>();
        
        try {
            while (( line = input.readLine()) != null){
                if(line.equals("[Term]")) {
                    //line = input.readLine();
                    while(!( line = input.readLine()).equals("")) {
                        record += line + "##";
                    }
                    record = record.substring(0, record.length()-2);
                    //System.out.println(record);
                    populateHashMap(ontology, record);
                    record = "";
                    index++;
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(OntologyProcess.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return ontology;
        }
    }
    
    private static void populateHashMap(HashMap<String, HashMap<String, ArrayList<String>>> ontology, String record) {
        String[] splits = record.split("##");
        Pattern p = Pattern.compile("id:\\s(.*)");
        Matcher m = p.matcher(splits[0]);
        HashMap<String, ArrayList<String>> hm = new HashMap<String, ArrayList<String>>();
        String id = "";
        
        if (m.find()) {
            //System.out.println(m.group(1));
            id = m.group(1);
            //ontology.put(m.group(1), new HashMap<String, ArrayList<String>>());
        }
        
        for(int i = 0; i < splits.length; i++) {
            p = Pattern.compile("(.*):\\s(.*)");
            m = p.matcher(splits[i]);
            
            if (m.find()) {
                String key = m.group(1);
                String value = m.group(2);
                //System.out.println(m.group(1) + "||" + m.group(2));
                if(hm.containsKey(key)) {
                    hm.get(key).add(value);
                } else {
                    hm.put(key, new ArrayList<String>());
                    hm.get(key).add(value);
                }
                ontology.put(id, hm);
            }
        }    
        return;    
    }
    
    private static void expandKeyword(HashMap<String, HashMap<String, ArrayList<String>>> ontology, String keyword, Set<String> keywords) {
//        if(getChildrenRecords(keyword, ontology).isEmpty()) {
//            return;
//        }
        ArrayList<HashMap<String, ArrayList<String>>> childrenRecords = new ArrayList<HashMap<String, ArrayList<String>>>();
        for (HashMap<String, ArrayList<String>> record : ontology.values()) {
           
            if(record.get("id").get(0).contains("DOID") && !record.containsKey("is_obsolete")) {
                if(record.get("name").get(0).toLowerCase().contains(keyword.toLowerCase())) {
                    addNameToKeywords(record, keywords);
                     childrenRecords = getChildrenRecords(record.get("name").get(0), ontology);
                    //System.out.println(childrenRecords.size());
                    for(HashMap<String, ArrayList<String>> childrenRecord : childrenRecords) {
                        addNameToKeywords(childrenRecord, keywords);
                    }
                }
            }
        }
    }
    
    private static void addNameToKeywords(HashMap<String, ArrayList<String>> record, Set<String> keywords) {
        keywords.add(record.get("name").get(0));
        //System.out.println("name: " + record.get("name").get(0));
        
        ArrayList<String> sys = record.get("synonym");
        if(sys != null) {
            for (String sy : sys) {
                if(sy.contains(" EXACT ")) {
                    Pattern p = Pattern.compile("\"(.*)\"\\s*EXACT\\s*");
                    Matcher m = p.matcher(sy);
                    if (m.find()) {
                        //System.out.println("synonym: " + m.group(1).trim());
                        keywords.add(m.group(1).trim());
                    }
                }
            }
        }
    }
    
    private static ArrayList<HashMap<String, ArrayList<String>>> getChildrenRecords(String name, HashMap<String, HashMap<String, ArrayList<String>>> ontology) {
        String id = "";
        ArrayList<HashMap<String, ArrayList<String>>> childrenRecords = new ArrayList<HashMap<String, ArrayList<String>>>();
        
        for (HashMap<String, ArrayList<String>> record : ontology.values()) {
            if(record.get("name").get(0).toLowerCase().equals(name.toLowerCase())) {
                //addNameToKeywords(record, keywords);
                //ArrayList<HashMap<String, ArrayList<String>>> childrenRecords = getChildrenRecords(record.get("name").get(0), ontology);
                id = record.get("id").get(0);
                break;
            }
        }
        String childRelation = id + " ! " + name;
        
        for (HashMap<String, ArrayList<String>> record : ontology.values()) {
            if(record.containsKey("is_a")) {
                if(record.get("is_a").get(0).equals(childRelation)) {
                    childrenRecords.add(record);
                }
            }
        }
        return childrenRecords;
    }
    
    private static void printSet(Set<String> set) {
        Iterator<String> it = set.iterator();
        while(it.hasNext()) {
            System.out.println(it.next());
        }
    }
    
    private static Set<String> splitKeyword(Set<String> keywords) throws FileNotFoundException {
        Set<String> tokens = new TreeSet<String>();
        Set<String> stoplist = new TreeSet<String>();
        stoplist = parseStopListFile("C:/workspace/hscb/TwitterProcessPipeline/stoplist_en.txt");
        
        Iterator<String> it = keywords.iterator();
        while(it.hasNext()) {
            //System.out.println(it.next());
           String[] splits = splitString(it.next());
           for(String split : splits) {
               if(!stoplist.contains(split.toLowerCase())) // is not a stopword
                    tokens.add(split);
           }
        }
        
        return tokens;
    }
    
    private static String[] splitString(String s) {
        String[] splits;
        
        //replace double "/#<>;:.,=()~&[]{}-_"  with " "
        s = s.replaceAll("/|—|#|<|>|;|:|\\.|\\,|=|\\(|\\)|~|&|\\[|\\]|\\{|\\}|\\-|\\_", " ");
        
        //remove repeating spaces
        s = s.replaceAll("( )\\1+", " ");
        
        //trim
        s = s.trim();
        
        splits = s.split(" ");
        return splits;
    }
    
    
    private static Set<String> parseStopListFile(String filename) throws FileNotFoundException {
        File file = new File(filename);
        BufferedReader input =  new BufferedReader(new FileReader(file));
        String line = null;
       
        int index = 0;
        String record = "";
        Set<String> stoplist = new TreeSet<String>();
        try {
            while (( line = input.readLine()) != null){
                    stoplist.add(line);
            }
        } catch (IOException ex) {
        } finally {
            return stoplist;
        }
    }        
}
