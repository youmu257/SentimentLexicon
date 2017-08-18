package ncku.ikm.sentiment_lexicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

class SOCPMI_Thread extends Thread {
	String word1;
	BufferedWriter bw;
	public SOCPMI_Thread(String word1,BufferedWriter bw) {
		this.word1 = word1;
		this.bw = bw;
	}
 
	public void run() {
		try{
			StringBuilder soc_pmi = new StringBuilder();
        	//long start = System.currentTimeMillis();
        	Map<String,Double> score = new LinkedHashMap<String,Double>();

        	if(SOCPMI.Token.containsKey(word1))
        	{
        		for(String word2 : SOCPMI.CandidateWords)
        		{
        			if(SOCPMI.Token.containsKey(word2))
        			{
        				double value = SOCPMI.SOC_PMI(word1, word2);
	        			if(value > 0)
	        				score.put(word1+"\t"+word2+"\t", value);
        			}
        		}
        	}
        	
        	Map<String,Double> rank = new LinkedHashMap<String,Double>();
        	rank.putAll(SOCPMI.sortByComparator(score));
        	
        	for(Map.Entry<String, Double> result : rank.entrySet())
        		soc_pmi.append(result.getKey()).append(result.getValue()).append("\n");
        	
        	bw.write(soc_pmi.toString());
        	bw.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

public class SOCPMI
{
    public static LinkedHashMap<String, Integer> Token = new LinkedHashMap<String, Integer>();//儲存每個字的次數
    public static ArrayList<String> CandidateWords = new ArrayList<String>();//要算相似度的字
    public static LinkedHashMap<String, LinkedHashMap<String, Integer>> CandidateNeighbors = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();//候選字的鄰居，及共同出現幾次
    private static int TokenCount = 0;
    private static double delta = 2.0;//corpus越大，delta要越大，12個例句用0.7，我先暫時用2
    private static int gama = 3;
    private static int window_size = 5;
    private static int thread_num = 5;
    
    public static void SOC_PMI()
    {
    	WordFrequency();//先讀取總token數量，及各單字的數量
        Candidate();    //單純取出所有的candidate words
        Neighbors();    //取出所有候選字(candidate words)的鄰居字(neighbors)
        Method();       //串接"前處理(上面三步驟)"到呼叫SOC-PMI
        System.out.println("SOC PMI finish!!");
    }
    
    public static void Method_thread()
    { 
    	long start  = System.currentTimeMillis();
		try{
			System.out.println("Method Start!!");
	        BufferedWriter bw = IO.Writer(IO.data_similarity_matrix+"Word_SOCPMI_Similarity.txt");
	        
	        System.out.println("CandidateWords size = " + CandidateWords.size());
	        
	        int count = 0;
	        for(String word1:CandidateWords)
	        {
	        	count++;
	        	SOCPMI_Thread thread = new SOCPMI_Thread(word1,bw);
	        	if(count % thread_num == 0)
	        	{
	        		thread.run();
	        	}
	        	else
	        		thread.start();
	        }
	        Thread.sleep(3000);
	        System.out.println("SOC PMI Method done!");
	        bw.close();
		}catch(Exception e){
			System.err.println("Method error!");
		}
		System.out.println((System.currentTimeMillis()-start)/1000);
    }
    
    // write soc-pmi similarity to file
    private static void Method()
    {
    	long start = System.currentTimeMillis();
    	try{
	        BufferedWriter bw = IO.Writer(IO.data_similarity_matrix+"Word_SOCPMI_Similarity.txt");
	        
	        for (String word1 : CandidateWords)
	        {
	        	if(Token.containsKey(word1))
                for (String word2 : CandidateWords)
                {
                	if(Token.containsKey(word2)){
	                	double value = SOC_PMI(word1, word2);
	                    if (value > 0)
	                    {
	                    	bw.write(word1+"\t"+word2+"\t"+(float)value+"\n");
	                    }
                }
                }
                bw.flush();
	        }
	        bw.close();
    	}catch(IOException e){
    		System.err.println("SOC PMI Method error!!\n" + e);
    	}
    	System.out.println((System.currentTimeMillis()-start)/1000);
    }

    //sentence segment and count word frequency
    private static void WordFrequency()
    {
    	try{
    		BufferedReader br = IO.Reader(IO.data_preprocessing + "CleanCorpus.txt");
	        String lin = "";
	        while((lin = br.readLine()) != null)
	        {
	        	try{
		            ArrayList<String> tmp = new ArrayList<String>(Arrays.asList(lin.split("\t")[0].split(" ")));//檔案必須已經stem完並去除stopwords
		            for (String word : tmp)
		            {
		                TokenCount++;
		                if (Token.containsKey(word))
		                    Token.put(word, Token.get(word)+1);
		                else
		                    Token.put(word, 1);
		            }
	        	}catch(ArrayIndexOutOfBoundsException e){
//	        		e.printStackTrace();
	        	}
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println("WordFrequency error!!\n" + e);
    	}
    }

    //get all candidate words，and seeting probability,candidate, and POStagging
    private static void Candidate()
    {
    	try{
	        //distinct stem是我們要算相似度的候選字
    		BufferedReader br = IO.Reader(IO.data_preprocessing+"NLP_Stemmer.txt");
    		String lin = "";
	        while((lin = br.readLine()) != null)
	        {
	        	String[] spli = lin.split("\t");
	        	boolean is_candidate = Integer.parseInt(spli[1])==1?true:false;
	        	float prob = Float.parseFloat(spli[2]);
	        	String stem = spli[3];
	        	String POS = spli[4];
	        	if(is_candidate && prob == 0.5 && POS.compareTo("A")==0 && !CandidateWords.contains(stem))
	        		CandidateWords.add(stem);//add candidate
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println("SOCPMI read candidate error!!\n" + e);
    	}
    }

    //select all candidate words' neighbor words
    private static void Neighbors()
    {
    	try{
	        //get all corpus
			BufferedReader br = IO.Reader(IO.data_preprocessing+"CleanCorpus.txt");
			String lin = "";
	        while((lin = br.readLine()) != null)
	        {
	        	try{
		            String[] words = lin.split("\t")[0].split(" ");//切割tweet
		           
		            List<String> intersect = MyJavaUtil.Intersect(CandidateWords, Arrays.asList(words));//取出有交集的candidate
		            if (intersect.size() > 0)
		            {
		            	
		            	for(String Hitword : intersect)
	                    {
		            		String[] tmp;
		            		String[] tmp2;
	                        int index = Arrays.asList(words).indexOf(Hitword);//取得屬於candidatewords的位置
	                        int start = (index - 5) > 0 ? (index - 5) : 0;
	                        int leng = words.length - start;
	
	                        if (leng >= 11)//array夠長，可以直接11個長度
	                        {
	                            tmp = new String[11];
	                            tmp = Arrays.copyOfRange(words, start, start + window_size * 2 + 1);
	                            tmp2 = new HashSet<String>(Arrays.asList(tmp)).toArray(new String[0]);
	                        }else//不夠11的，則是剩餘的長度
	                        {
	                            tmp = new String[leng];
	                            tmp = Arrays.copyOfRange(words, start, start + leng);
	                            tmp2 = new HashSet<String>(Arrays.asList(tmp)).toArray(new String[0]);
	                        }
	
	                        for(String x : tmp2)
	                        {
	                            if (CandidateNeighbors.containsKey(Hitword))
	                            {
	                                if (CandidateNeighbors.get(Hitword).containsKey(x))
	                                	CandidateNeighbors.get(Hitword).put(x, CandidateNeighbors.get(Hitword).get(x)+1);
	                                else
	                                    CandidateNeighbors.get(Hitword).put(x, 1);
	                            }else
	                            {
	                                LinkedHashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
	                                temp.put(x, 1);
	                                CandidateNeighbors.put(Hitword, temp);
	                            }
	                        }
	                        CandidateNeighbors.get(Hitword).remove(Hitword);//移除自己本身
	                    }
		            }
	        	}catch(ArrayIndexOutOfBoundsException e){
	        		
	        	}
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println("select neighbors error!!\n" + e);
    	}
    }

    //SOC-PMI main algorithm
    public static double SOC_PMI(String word1, String word2)
    {
    	double sum1 = 0, sum2 = 0;
        double similarity = 0;
        Map<String, Integer> word1_nei = new LinkedHashMap<String,Integer>();
        Map<String, Integer> word2_nei = new LinkedHashMap<String,Integer>();
        Map<String, Double>  Word1_Pmi = new LinkedHashMap<String, Double>();
        Map<String, Double>  Word2_Pmi = new LinkedHashMap<String, Double>();
        int beta1 =0;
        int beta2 =0;

        if(CandidateNeighbors.containsKey(word1))
        {
            word1_nei = CandidateNeighbors.get(word1);//取出word1 的鄰居
            beta1 = beta(word1);
        }
        if(CandidateNeighbors.containsKey(word2))
        {
            word2_nei = CandidateNeighbors.get(word2);//取出word2 的鄰居
            beta2 = beta(word2);
        }
        // 計算兩字與各自鄰居的PMI
        for(Map.Entry<String,Integer> neighbors : word1_nei.entrySet())
        	Word1_Pmi.put(neighbors.getKey(), PMI(Token.get(word1), Token.get(neighbors.getKey()), neighbors.getValue())); //鄰居與word1的PMI
        for(Map.Entry<String,Integer> neighbors : word2_nei.entrySet())
        	Word2_Pmi.put(neighbors.getKey() , PMI(Token.get(word2), Token.get(neighbors.getKey()), neighbors.getValue()));//鄰居與word2的PMI
        // ------------
        if (beta1 != 0 )
        {
        	if(word1_nei.size() < beta1)
        		beta1 = word1_nei.size();
        	
        	String[] word = sortByComparator(Word1_Pmi).keySet().toArray(new String[0]);
        	for(int i=0; i < word.length && i < beta1 ;i++)//只計算top beta1
        	{
        		if(Word2_Pmi.containsKey(word[i]) && Word2_Pmi.get(word[i]) > 0)
        			sum1 += Math.pow(Word2_Pmi.get(word[i]) , gama);
        	}
        	//計算 top beta1 後跟最後一個word 的 pmi 值相同的字
        	for(int i=beta1;i<word.length;i++){
        		if(Math.abs(Word1_Pmi.get(word[beta1-1])- Word1_Pmi.get(word[i])) > 0.1)
        			break;
        		if(Word2_Pmi.containsKey(word[i]) && Word2_Pmi.get(word[i]) > 0)
        			sum1 += Math.pow(Word2_Pmi.get(word[i]) , gama);
        	}
            sum1 /= beta1;
        }

        if (beta2 != 0 && word2_nei.size() > 0)
        {
        	if(word2_nei.size() < beta2)
        		beta2 = word2_nei.size();
        	
        	String[] word = sortByComparator(Word2_Pmi).keySet().toArray(new String[0]);
        	
        	for(int i=0; i < word.length && i < beta2 ;i++)//只計算top beta1
        	{
        		if(Word1_Pmi.containsKey(word[i]) && Word1_Pmi.get(word[i]) > 0)
        			sum2 += Math.pow(Word1_Pmi.get(word[i]) , gama);
        	}
        	//計算 top beta2 後跟最後一個word 的 pmi 值相同的字
        	for(int i=beta2;i<word.length;i++){
        		if(Math.abs(Word2_Pmi.get(word[beta2-1])-Word2_Pmi.get(word[i])) > 0.1)
        			break;
        		if(Word1_Pmi.containsKey(word[i]) && Word1_Pmi.get(word[i]) > 0)
        			sum2 += Math.pow(Word1_Pmi.get(word[i]) , gama);
        	}
            sum2 /= beta2;
        }
        similarity = sum1 + sum2;
        return similarity;
    }

    //calculate beta
    private static int beta(String word)
    {
    	double beta = Math.pow( MyJavaUtil.log2(Token.get(word) + 1), 2) * 
    			                (MyJavaUtil.log2(Token.size()) / delta);
        return (int)beta;
    }
    
    //這個sort 使用完後建議在新建一個LinkedHashMap ,用putAll 的方式把排序結果放進去,不然有時再用get 會出問題
    //sorted descending by value 
    public static Map<String, Double> sortByComparator(final Map<String, Double> pmi) {
    	List<Entry<String,Double>> arr = new LinkedList<Entry<String,Double>>( pmi.entrySet() );
        
        Collections.sort( arr , new Comparator<Entry<String, Double>>() {
            public int compare(Entry<String,Double> o1 , Entry<String,Double> o2 )
            {
                return o2.getValue().compareTo( o1.getValue() );//大到小排序
            }
        });
        
        LinkedHashMap<String,Double> sortedByComparator = new LinkedHashMap<String,Double>();
        for(Entry<String,Double> e : arr)
        {
        	sortedByComparator.put(e.getKey() , e.getValue() );
        }
	    return sortedByComparator;
	}
    
    //calculate PMI
    private static double PMI(int a, int b, int a_b)//a&b都是字在corpus的次數，a_b是一起出現的次數，TokenCount是整個corpus的字數(可重複)
    {
    	double down = (double)a * b; //分母
        double up = (double)a_b * TokenCount; //分子
        return MyJavaUtil.log2(up / down);
    }
}
