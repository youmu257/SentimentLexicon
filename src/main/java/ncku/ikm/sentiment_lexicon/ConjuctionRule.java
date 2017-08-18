package ncku.ikm.sentiment_lexicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ConjuctionRule
{
    private static LinkedHashMap<String, LinkedHashMap<String, String>> StemMap = new LinkedHashMap<String, LinkedHashMap<String, String>>();//POS,word,stemword
    private static LinkedHashMap<String, ArrayList<String>> NegationWord = new LinkedHashMap<String, ArrayList<String>>();//tweet ID,negation word
    
    public static void Conjuction_Rule()
    {
    	Original_Word("and");
    	System.out.println("Conjuction Rule finish!!");
    }
    
    private static void Original_Word(String relation)
    {
        String oppRelation = relation.compareTo("and") == 0?"but":"and";

        //init
        getCandidate();
        getNegativeWord();
	        
	    try{
	        BufferedReader br = IO.Reader(IO.data_preprocessing+"StanFord_Structure.txt");
	        BufferedWriter bw = IO.Writer(IO.data_similarity_matrix+"Word_Conjunction_Negation.txt");
	        String lin = "";
			int j=0;
			while((lin = br.readLine()) != null)
	        {
				String[] spli = lin.split("\t");
	            String ID    = spli[0];
	            String Relation = spli[1];
	            if(Relation.compareTo("conj_"+relation)!=0)
	            	continue;
	            String word1 = spli[2];
	            String word2 = spli[3];
	            String POS1  = spli[4];
	            String POS2  = spli[5];
	            
	            POS1 = pos_mapping(POS1);
	            POS2 = pos_mapping(POS2);
	            if ( POS1.compareTo("other") == 0 || POS2.compareTo("other") == 0)
	                continue;
	            
	            if (StemMap.get(POS1).containsKey(word1))
	                if (StemMap.get(POS2).containsKey(word2))//這兩個if先確認有沒有在candidate list裡面
	                {
	                	StringBuilder sb = new StringBuilder();
	                    if (NegationWord.containsKey(ID))
	                    {
	                        if (NegationWord.get(ID).contains(word1) && NegationWord.get(ID).contains(word2))//同時的negation，relation不變
	                        	sb.append(relation).append("\t");
	                        else if (NegationWord.get(ID).contains(word1) || NegationWord.get(ID).contains(word2))//單一的negation，relation要相反
	                        {	
	                        	sb.append(oppRelation).append("\t");
	                        }
	                        else
	                        	sb.append(relation).append("\t");
	                    }else //都沒有negation
	                    	sb.append(relation).append("\t");
	                    
	                    sb.append(StemMap.get(POS1).get(word1)).append("\t");
	                    sb.append(StemMap.get(POS2).get(word2)).append("\t");
	                    sb.append(POS1).append("\t");
	                    sb.append(POS2).append("\n");
	                    
	                    bw.write(sb.toString());
	                    j++;
	                    if(j%1000==0)
	                    	bw.flush();
	                }
	        }
			br.close();
			bw.close();
        }catch(IOException e){
        	System.err.println("Conjunction Original_Word error!!\n" + e);
        }
    }
    
    private static void getCandidate()
    {
    	try{
	        LinkedHashMap<String, String> temp = new LinkedHashMap<String, String>();
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
	            }
	            else
	            {
	                temp = new LinkedHashMap<String, String>();
	                temp.put(Word, Stem);
	                StemMap.put(POS, temp);
	            }
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println("Conjunction getCandidate error!!\n" + e);	
    	}
    }
    
    private static void getNegativeWord()
    {
    	try{
    		BufferedReader br = IO.Reader(IO.data_preprocessing+"StanFord_Structure.txt");
    		String lin = "";
	        while((lin = br.readLine()) != null)
	        {
	        	String[] spli = lin.split("\t");
	        	String ID = spli[0];
	        	String Relation = spli[1];
	        	if(Relation.compareTo("neg")!=0)
	        		continue;
	        	String word1 = spli[2];
	            if (!NegationWord.containsKey(ID))
	            {
	                ArrayList<String> tmp = new ArrayList<String>();
	                tmp.add(word1);
	                NegationWord.put(ID, tmp);
	            }
	            else
	                NegationWord.get(ID).add(word1);
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println("getNegativeWord error!!\n" + e);
    	}
    }

    private static String pos_mapping(String POS)
    {
    	//正規化詞性
    	String pos = "other";
    	if (POS.compareTo("JJ") == 0 || POS.compareTo("JJR") ==0 || POS.compareTo("JJS") ==0)
    		pos = "A";
        else if (POS.compareTo("NN") ==0 || POS.compareTo("NNP") ==0 || POS.compareTo("NNPS") ==0 || POS.compareTo("NNS") ==0)
        	pos = "N";
        else if (POS.compareTo("RB") ==0 || POS.compareTo("RBR") ==0 || POS.compareTo("RBS") ==0 )
        	pos = "R";
        else if (POS.compareTo("VB") ==0 || POS.compareTo("VBD") ==0 || POS.compareTo("VBG") ==0 ||
        		 POS.compareTo("VBN") ==0 || POS.compareTo("VBP") ==0 || POS.compareTo("VBZ") ==0)
        	pos = "V";
    	return pos;
    }
}
