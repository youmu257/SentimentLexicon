package ncku.ikm.sentiment_lexicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


public class ExtractCandidate
{
    public static void CandidateExtract()
    {
    	getCandidate(0.5);
    	System.out.println("CandidateExtract finish!!");
    }
    
    //Candidate Extraction
    private static void getCandidate(double probability)
    {
    	ArrayList<String> stopwords = ReadStopwords();
    	
    	HashSet<String> candidate = new HashSet<String>();
    	HashSet<String> garbage = new HashSet<String>();
        
        try{
	        String regex = "^[a-zA-Z][a-zA-Z]*(-[a-zA-Z]+)*$"; //兩次以上的字母
	        
	        BufferedReader br = IO.Reader(IO.data_preprocessing+"POS_corpus(conll).txt");
	        String lin = "";
	        while((lin = br.readLine()) != null)
	        {
                String[] spli = lin.split("\t");//word \t tagging \t probability
                if(spli.length < 3)//avoid empty lin
                	continue;
                if(spli[1].compareTo("N")==0 || spli[1].compareTo("A")==0 ||
                   spli[1].compareTo("V")==0 || spli[1].compareTo("R")==0)
                {
	                if(Float.parseFloat(spli[2]) >= probability)
	                {
			            String word = spli[0].toLowerCase();
			            String tagging = spli[1];
		
			            if (word.matches(regex) && word.length() > 2 && !stopwords.contains(word))
			                candidate.add(word + "\t" + tagging);
			            else
			                garbage.add(word + "\t" + tagging);
	                }
                }
	        }
        }catch(IOException e){
        	System.err.println("ExtractCandidate.getCandidate error");
        }
        try{
	        BufferedWriter bw1 = IO.Writer(IO.data_preprocessing + "Candidate_" + probability + ".txt");
	        bw1.write(MyJavaUtil.StringJoin("\n", candidate) + "\n");
	        BufferedWriter bw2 = IO.Writer(IO.data_preprocessing + "Garbage_" + probability + ".txt");
	        bw2.write(MyJavaUtil.StringJoin("\n", garbage) + "\n");
	        
	        bw1.close();bw2.close();
    	}catch(IOException e){
    		System.err.println("getCandidate error!!\n" + e);
    	}
    }
    
    //Read stop word list
    private static ArrayList<String> ReadStopwords()
    {
    	ArrayList<String> stopwords = null;
    	try{
	        BufferedReader br = IO.Reader("Tool\\wiki_stopwords.txt");
	        stopwords = new ArrayList<String>(Arrays.asList(br.readLine().split(",")));
	        br.close();
    	}catch(IOException e){
    		System.err.println("Read stopwords error!!\n" + e);
    	}
    	return stopwords;
    }
}
