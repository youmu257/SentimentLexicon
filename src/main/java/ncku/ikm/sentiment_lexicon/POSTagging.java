package ncku.ikm.sentiment_lexicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

class TwitterNLP_POS{
    int ID;
    String Word,Tagging;
    float p;//Probability
}
class TwitterNLP_Sentence{
	int ID;
    String Keyword, ScreenName, Tweet, Tagging;
}

public class POSTagging
{
    private static String nlp_path = "Tool\\ark-tweet-nlp-0.3.2\\";

    public static void POSTagging1()
    {
    	ReadCorpus();
    	CallCMD();
    	ReadPOSCorpus_conll();
    	System.out.println("POSTagging1 finish!!");
    }
    public static void POSTagging2()
    {
    	CleanUp_Lemmatization();
    	ReadCleanCorpus();
        System.out.println("POSTagging2 finish!!");
    }
    
    private static void ReadCorpus()
    {
    	try{
    		BufferedReader br = IO.Reader(IO.data_path+"Corpus.txt");
    		BufferedWriter bw = IO.Writer(IO.data_path+"Courpus(no_id).txt");
    		String lin = "";
    		while((lin = br.readLine()) != null)
    		{
    			try{
    				bw.write(lin.split("\t")[1]+"\n");
    			}catch(ArrayIndexOutOfBoundsException e){
    				continue;
    			}
    		}
    		br.close();
    		bw.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
    // call art-tweet-nlp
    private static void CallCMD()
    {
    	try{
    		BufferedWriter bw = IO.Writer(nlp_path + "postagging.bat");
    		bw.write("cd Tool\\ark-tweet-nlp-0.3.2\n");
    		bw.write("java -Xmx1000m -jar ark-tweet-nlp-0.3.2.jar --output-format conll ..\\..\\"+IO.data_path+"Courpus(no_id).txt > ..\\..\\"+IO.data_preprocessing+"\\POS_corpus(conll).txt\n");
    		bw.write("java -Xmx1000m -jar ark-tweet-nlp-0.3.2.jar ..\\..\\"+IO.data_path+"Courpus(no_id).txt > ..\\..\\"+IO.data_preprocessing+"POS_corpus.txt\n");
    		bw.close();
    		
	    	Runtime run = Runtime.getRuntime();
	    	Process p = run.exec("cmd.exe /c " + nlp_path + "postagging.bat");
	        p.waitFor();
    	}catch(Exception e){
    		System.err.println("CallCMD error!!\n" + e);
    	}
    }

    //read POStweet(conll)
    private static void ReadPOSCorpus_conll()
    {
    	ArrayList<TwitterNLP_POS> DataTable = new ArrayList<TwitterNLP_POS>();
        
        int i = 1;
    	try{
	        BufferedReader br = IO.Reader(IO.data_preprocessing + "POS_corpus(conll).txt");
	        String lin = "";
	        while ((lin = br.readLine()) != null)
	        {
	            if (lin != null)
	            {
	                String[] spli = lin.split("\t");
	                if(spli.length < 3)
	                	continue;
	                TwitterNLP_POS tmp = new TwitterNLP_POS();
	                tmp.ID = i;
	                tmp.Word = spli[0];
	                tmp.Tagging = spli[1];
	                tmp.p = Float.parseFloat(spli[2]);
	                DataTable.add(tmp);
	            }
	            else
	                i++;
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println("Reader POStweet(conll) error!!\n");
    		e.printStackTrace();
    	}
    }

    // Lemmatization tweet(2013/5/23)
    private static void CleanUp_Lemmatization()
    {
    	LinkedHashMap<String, LinkedHashMap<String, String>> StemMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();//POS,word,stemword
	    LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
    	try{
	        //讀取 stemming 完的字，當對照表給sentence使用
	        BufferedReader br = IO.Reader(IO.data_preprocessing+"NLP_Stemmer.txt");
	        String lin = "";
	        while((lin = br.readLine()) != null)
	        {
	        	String[] spli = lin.split("\t");
	        	
		        String Word = spli[0];
		        String Stem = spli[3];
		        String POS = spli[4];
		        
	            if (StemMap.containsKey(POS))
	            {
	            	StemMap.get(POS).put(Word, Stem);
	            }else
	            {
	                temp = new LinkedHashMap<String, String>();
	                temp.put(Word, Stem);
	                StemMap.put(POS, temp);
	            }
	        }
    	}catch(IOException e){
    		System.err.println("CleanUp_Lemmatization Read Candidate error!!\n");
    		e.printStackTrace();
    	}
	        
        ArrayList<String> stopwords = new ArrayList<String>();
        try{
	        BufferedReader br2 = IO.Reader("Tool\\wiki_stopwords.txt");
	        stopwords = new ArrayList<String>(Arrays.asList(br2.readLine().split(",")));
	        br2.close();
	
	        BufferedReader br = IO.Reader(IO.data_preprocessing + "POS_corpus.txt");//parse完的
	        BufferedWriter bw = IO.Writer(IO.data_preprocessing + "CleanCorpus.txt");//清乾淨的
	
	        String lin = "";
	        while ((lin = br.readLine()) != null)
	        {
	            String[] spli = lin.split("\t");
	            String[] Sentence = spli[0].toLowerCase().split(" ");//Parser切完的token
	            String[] Tagging = spli[1].split(" ");//POS
	            int length = Sentence.length;
	            ArrayList<Integer> RemovePosition = new ArrayList<Integer>();
	
	            for (int i = 0; i < length; i++)
	            {
	                if (stopwords.contains(Sentence[i])){//stopwords
	                    RemovePosition.add(i);
	                }else{
	                	if(Tagging[i].compareTo("A") == 0){
	                		if (StemMap.get("A").containsKey(Sentence[i]))
                                Sentence[i] = StemMap.get("A").get(Sentence[i]);//將"原字"代換成"stemming完的字"
	                	}else if(Tagging[i].compareTo("N") == 0){
	                		if (StemMap.get("N").containsKey(Sentence[i]))
	                            Sentence[i] = StemMap.get("N").get(Sentence[i]);//將"原字"代換成"stemming完的字"
	                	}else if(Tagging[i].compareTo("R") == 0){
	                		if (StemMap.get("R").containsKey(Sentence[i]))
                                Sentence[i] = StemMap.get("R").get(Sentence[i]);//將"原字"代換成"stemming完的字"
	                	}else if(Tagging[i].compareTo("V") == 0){
	                		if (StemMap.get("V").containsKey(Sentence[i]))
                                Sentence[i] = StemMap.get("V").get(Sentence[i]);//將"原字"代換成"stemming完的字"
	                	}else{
//	                		---以下是要清除的字
//	                		E : emotion
//	                		U : URL
//	                		~ : RT
//	                		, : punctuation
//	                		$ : Numeral
	                		RemovePosition.add(i);
	                	}
	                }//else
	            }//for
	
	            StringBuilder SB = new StringBuilder();
	            StringBuilder SB2 = new StringBuilder();
	            for (int i = 0; i < length; i++)
	            {
	                if (!RemovePosition.contains(i))
	                {
	                    SB.append(Sentence[i] + " ");
	                    SB2.append(Tagging[i] + " ");
	                }
	            }
	
	            bw.write(SB.toString().trim() + "\t" + SB2.toString().trim() + "\n");
	        }
	        
	        br.close();  bw.close();
    	}catch(IOException e){
    		System.err.println("CleanUp_Lemmatization error!!\n");
    		e.printStackTrace();
    	}
    }
    
    private static void ReadCleanCorpus()
    {
	    ArrayList<TwitterNLP_Sentence> dataTable = new ArrayList<TwitterNLP_Sentence>();
	    try{
	    	//read Tweet and Attribute
	        BufferedReader br = IO.Reader(IO.data_preprocessing + "CleanCorpus.txt");
	        String lin = "";
	        while ((lin = br.readLine()) != null)
	        {
	            String[] spli = lin.split("\t");
	            try{
		            String Tweet = spli[0];
		            String Tagging = spli[1];
		            
		            TwitterNLP_Sentence tmp = new TwitterNLP_Sentence();
		            tmp.Tweet = Tweet;
		            tmp.Tagging = Tagging;
		            dataTable.add(tmp);
	            }catch(ArrayIndexOutOfBoundsException e){
	            	continue;
	            }
	        }
	        br.close();
	    }catch(IOException e){
	    	System.err.println("ReadCleanCorpus error!!\n" + e);
	    }
    }
}

