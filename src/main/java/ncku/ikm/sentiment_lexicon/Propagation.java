package ncku.ikm.sentiment_lexicon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/* 這隻程式用來產生各種不同相似度的矩陣 */


public class Propagation
{
    private static double epison = 0.01;//propagate的收斂門檻
    private static double damping = 0.3;//
    private static double ratio = 1.2;
    private static int maxiteration = 300;//超過這數字直接停止
    private static int Fiteration = 0;//紀錄需要幾個iteration才會收斂
    private static ArrayList<String> Seedwords_P = new ArrayList<String>();//有P polarity的字
    private static ArrayList<String> Seedwords_N = new ArrayList<String>();//有N polarity的字
    private static ArrayList<String> FirstCandidate = new ArrayList<String>();//第一輪剛讀進來的，還沒照SEED去排
    private static LinkedHashMap<String, Integer> Candidatewords = new LinkedHashMap<String, Integer>();//正確的順序+ID
    private static HashMap<String, Integer> wordcount = new HashMap<String, Integer>(); 
    
    public static void propagation(String POS, int[] ratio)
    {
        //前處理
        ReadSeedwords();
        ReadCandidatewords(POS);//可以填X，代表沒有詞性
        CheckSeedwords();

        WordNet(POS);
        ConjunctionRule(POS);
        SOCPMI(POS);

        PrintTransition(BuildMatrix(ReadWordNet(), ReadConj(), ReadSOCPMI(), ratio[0], ratio[1], ratio[2]),IO.data_propagation);
        PrintClass();
        WordCount();
        PropagationLabel();  
    }
    
    private static void WordCount()
    {
	    try{
	    	//read Tweet and Attribute
	        BufferedReader br = IO.Reader(IO.data_preprocessing + "CleanCorpus.txt");
	        String lin = "";
	        while ((lin = br.readLine()) != null)
	        {
	            String[] spli = lin.split("\t");
	            try{
		            for(String word : spli[0].split(" "))
		            {
		            	if(wordcount.containsKey(word))
		            		wordcount.put(word, wordcount.get(word)+1);
		            	else
		            		wordcount.put(word, 1);
		            }
		            
	            }catch(ArrayIndexOutOfBoundsException e){
	            	continue;
	            }
	        }
	        br.close();
	    }catch(IOException e){
	    	System.err.println("wordCount error!!\n" + e);
	    }
    }
    
	//取出所有的seedwords，通用
    private static void ReadSeedwords()
    {
    	try{
	        BufferedReader br = IO.Reader(IO.data_path + "Seedwords.txt");
	       	Seedwords_P = new ArrayList<String>(Arrays.asList(br.readLine().split(",")));
	        Seedwords_N = new ArrayList<String>(Arrays.asList(br.readLine().split(",")));
	        System.out.println("P: " + Seedwords_P.size());
	        System.out.println("N: " + Seedwords_N.size());
	        br.close();
    	}catch(IOException e){
    		System.err.println("ReadSeedwords error!!\n" + e);
    	}
    }
	
    //取出要放在matrix的字，通用
    private static void ReadCandidatewords(String POS)
    {
    	try{
			BufferedReader br = IO.Reader(IO.data_preprocessing+"NLP_Stemmer.txt");
	        FirstCandidate = new ArrayList<String>();
	        String lin="";
	        while((lin = br.readLine()) != null)
	        {
	        	String[] spli = lin.split("\t");
	        	boolean is_candidate = Integer.parseInt(spli[1])==1?true:false;
	        	float prob = Float.parseFloat(spli[2]);
	        	String stem_word = spli[3];
	        	String pos = spli[4];
	        	
	        	boolean add = false;
	        	if(is_candidate && prob == 0.5 && !FirstCandidate.contains(stem_word))
	        	{
	        		if(POS.compareTo("X")==0)
	        			if(pos.compareTo("N")!=0)
	        				add = true;
	        		else
	        			if(POS.compareTo(pos)==0)
	        				add = true;
	        	}
	        	if(add)
	        		FirstCandidate.add(stem_word);
	        }
	        br.close();
	        System.out.println("1st Candidatewords: " + FirstCandidate.size());
    	}catch(IOException e){
    		System.err.println("ReadCandidatewords error!!\n" + e);
    	}
    }
    
    //檢查seedword是否也在candidate裡面，並排序正確順序，通用
    private static void CheckSeedwords()
    {
        for(int i=0;i< Seedwords_P.size();i++){
            if (!FirstCandidate.contains(Seedwords_P.get(i)))
            {
                System.out.println("No this word:" + Seedwords_P.get(i));
                Seedwords_P.remove(Seedwords_P.get(i));
                i--;
            }
        }
        for(int i=0;i< Seedwords_N.size();i++){
            if (!FirstCandidate.contains(Seedwords_N.get(i)))
            {
                System.out.println("No this word:" + Seedwords_N.get(i));
                Seedwords_N.remove(Seedwords_N.get(i));
                i--;
            }
        }
        System.out.println("P: " + Seedwords_P.size());
        System.out.println("N: " + Seedwords_N.size());

        //排正確的順序
        Candidatewords = new LinkedHashMap<String, Integer>();
        int ID = 0;
        for(String word : Seedwords_P)
            Candidatewords.put(word, ID++);
        for(String word : Seedwords_N)
            Candidatewords.put(word, ID++);
        for(String word : FirstCandidate)
        {
            if (!Candidatewords.containsKey(word))
                Candidatewords.put(word, ID++);
        }
        System.out.println("Final Candidatewords: " + Candidatewords.size());
        
        try{
        	BufferedWriter bw = IO.Writer(IO.data_propagation + "Candidatewords.txt");
        	bw.write(MyJavaUtil.StringJoin("\r\n", Candidatewords.keySet()) + "\n");
        	bw.close();
        }catch(IOException e){
        	System.err.println(e);
        }
    }
    
    //將transition matrix放到陣列裡，For WordNet
    private static double[][] BuildMatrix(double[][] M1, double[][] M2, double[][] M3, int a, int b, int c)
    {
        double[][] ALL = new double[Candidatewords.size()][Candidatewords.size()];
        for (int i = 0; i < Candidatewords.size(); i++)
        {
            for (int j = 0; j < Candidatewords.size(); j++)
                ALL[i][j] = a * M1[i][j] + b * M2[i][j] + c * M3[i][j];
            //ALL[i][j] = (M1[i][j] > 0 ? M1[i][j] : 1) * (M2[i][j] > 0 ? M2[i][j] : 1) * (M3[i][j] > 0 ? M3[i][j] : 1);
        }
        return ALL;
    }
    
    //把wordnet從印好的檔案讀出來
    private static double[][] ReadWordNet()
    {
        double[][] WordNet = new double[Candidatewords.size()][Candidatewords.size()];
        try{
	        BufferedReader br = IO.Reader(IO.result_wordnet + "Transition.csv");
	        int number = 0;
	        String lin = "";
	        while ((lin = br.readLine()) != null)
	        {
	            WordNet[number++] = MyJavaUtil.toDouble(lin.split(","));
	        }
	        br.close();
        }catch(IOException e){
        	System.err.println("ReadWordNet error!!");
        }
        return WordNet;
    }
    
    //把Conj從印好的檔案讀出來
    private static double[][] ReadConj()
    {
       double[][] Conj = new double[Candidatewords.size()][Candidatewords.size()];
       try{
    	   BufferedReader br = IO.Reader(IO.result_conjuction_rule + "Transition.csv");
            int number = 0;
            String lin = "";
            while ((lin = br.readLine()) != null)
            {
                Conj[number++] = MyJavaUtil.toDouble(lin.split(","));
            }
            br.close();
        }catch(IOException e){
        	System.err.println("ReadConj error!!");
        }
        return Conj;
    }
    
    //把SOCPMI從印好的檔案讀出來
    private static double[][] ReadSOCPMI()
    {
    	double[][] SOCPMI = new double[Candidatewords.size()][Candidatewords.size()];
    	try{
	        BufferedReader br = IO.Reader(IO.result_socpmi + "Transition.csv");
	        int number = 0;
	        String lin = "";
	        while ((lin = br.readLine()) != null)
	        {
	            SOCPMI[number++] = MyJavaUtil.toDouble(lin.split(","));
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println("ReadSOCPMI error!!");
    	}
        return SOCPMI;
    }
    
    private static void WordNet(String POS)
    {
        //建置
        double[][] temp = BuildMatrix_WordNet(POS);

        PrintTransition(temp, IO.result_wordnet);
    }

    private static void ConjunctionRule(String POS)
    {
        //建置
        double[][] temp = BuildMatrix_ConjunctionRule(POS);

        PrintTransition(temp, IO.result_conjuction_rule);
    }

    private static void SOCPMI(String POS)
    {
        //建置
        double[][] temp = BuildMatrix_SOCPMI(POS);

        PrintTransition(temp, IO.result_socpmi);
    }
    
    //印出Transition matrix
    private static void PrintTransition(double[][] input, String dir)
    {
    	try{
	        BufferedWriter bw = IO.Writer(dir + "Transition.csv");
	        for (int i = 0; i < Candidatewords.size(); i++)
	        {
	        	bw.write(MyJavaUtil.StringJoin(",", input[i]) + "\n");
	        }
	        bw.close();
    	}catch(IOException e){
    		System.err.println(e);
    	}
    }
    
    //印出class matrix
    private static void PrintClass()
    {
    	try{
	        BufferedWriter bw = IO.Writer(IO.data_propagation + "Class.txt");
	        for (int i = 0; i < Candidatewords.size(); i++)
	        {
	            if (i < Seedwords_P.size())
	            	bw.write("1,0\n");
	            else if (i >= Seedwords_P.size() && i < (Seedwords_P.size() + Seedwords_N.size()))
	            	bw.write("0,1\n");
	            else
	            	bw.write("0,0\n");
	        }
	        bw.close();
    	}catch(IOException e){
    		System.err.println(e);
    	}
    }

    private static void PropagationLabel()
    {
    	int pos = 0, neg = 0, instance = 0, Classnumber = 0;
    	try{
	        //從Class.txt讀取總筆數、正筆數、負筆數
	        BufferedReader br = IO.Reader(IO.data_propagation + "Class.txt");
	        for(String lin = br.readLine();lin != null;lin = br.readLine())
	        {
	            if (lin.split(",")[0].compareTo("1") == 0)
	                pos++;
	            else if (lin.split(",")[1].compareTo("1") == 0)
	                neg++;
	            instance++;
	            Classnumber = lin.split(",").length;
	        }
	        br.close();
	        //end
    	}catch(IOException e){
    		System.err.println("Propagation read class error!!");
    	}
    	
    	String[] Candidatewords = new String[instance];
    	try{
	        //從Candidatewords.txt讀取candidate的list
	        BufferedReader br = IO.Reader(IO.data_propagation + "Candidatewords.txt");
	        int number = 0;
	        for(String lin = br.readLine();lin != null;lin = br.readLine())
	        {
	            Candidatewords[number++] = lin;
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println("Propagation read candidate error!!");
    	}
    	
        //讀取資料陣列
        MatrixDecompositionProgram M = new MatrixDecompositionProgram();
        double[][] Transition = M.MatrixRead(IO.data_propagation + "Transition.csv", instance, instance);//相似度矩陣
        double[][] Class = M.MatrixRead(IO.data_propagation + "Class.txt", instance, Classnumber);//class矩陣
        int rows = Transition.length;   //兩個矩陣的共同rows數量
        int C_columns = Class[0].length;    //Class的columns數量，正常為2
        int Labellength = pos + neg;

        double[][] ActiveTransition = new double[instance][instance];
        ActiveTransition = M.MatrixDuplicate(Transition);//複製一個起來放

        ActiveTransition = M.Columm_Normalized(ActiveTransition);

        double[][] Yl = new double[Labellength][C_columns];//初始化
        for (int i = 0; i < Labellength; i++)
            for (int j = 0; j < C_columns; j++)
            {
                Yl[i][j] = Class[i][j];
            }

        double[][] LastClass = new double[rows][C_columns];//儲存上一輪class的結果
        int iteration = 0;
        while (true)
        {
            iteration++;
            //-----------------------------------------------step 1，Y=dTY + (1-d)Y
            Class = M.MatrixProduct(ActiveTransition, Class);
            Class = M.MatrixPlus(M.MatrixProduct(Class, damping), M.MatrixProduct(LastClass, 1 - damping));

            //-----------------------------------------------step 3，clamp原始值
            for (int i = 0; i < Labellength; i++)
                for (int j = 0; j < C_columns; j++)
                {
                    Class[i][j] = Yl[i][j];
                }
            //-----------------------------------------------檢查是否收斂
            if (M.MatrixAreEqual(LastClass, Class, epison) || iteration >= maxiteration)
            {
                System.out.println("The Matrix is a convergence");
                Fiteration = iteration;
                try{
	                BufferedWriter bw = IO.Writer(IO.data_propagation + "FinalMatrix.csv");
	                String tmp_word = "";
	                for (int i = 0; i < instance; i++)
	                {
	                	tmp_word = Candidatewords[i].toString();
	                	if (Class[i][0] > ratio*Class[i][1])
	                    	bw.write(tmp_word +","+wordcount.get(tmp_word)+ ",positive," + MyJavaUtil.StringJoin(",", Class[i]) + "\n");
	                    else if (Class[i][0]*ratio < Class[i][1])
	                    	bw.write(tmp_word +","+wordcount.get(tmp_word)+ ",negative," + MyJavaUtil.StringJoin(",", Class[i]) + "\n");
	                    else
	                    	bw.write(tmp_word +","+wordcount.get(tmp_word)+ ",neutral,"  + MyJavaUtil.StringJoin(",", Class[i]) + "\n");
	                }
	                bw.close();
                }catch(IOException e){
                	System.err.println("write to finalMatrix error!!");
                }
                break;
            }
            else
            {
                for (int i = 0; i < rows; i++)
                	System.arraycopy(Class[i],0 , LastClass[i],0 , C_columns);
            }
        }
    }
    
    //將transition matrix 放到陣列裡，For WordNet
    private static double[][] BuildMatrix_WordNet(String POS)
    {
    	double[][] WordNet = new double[Candidatewords.size()][Candidatewords.size()];
    	try{
	        BufferedReader br = IO.Reader(IO.data_similarity_matrix+"WordNet_Relation.txt");
	        String lin = "";
	        while((lin = br.readLine()) != null)
	        {
	        	String[] spli = lin.split("\t");
	            String Stem = spli[0];
	            String pos = spli[1];
	            
	            if(POS.compareTo("X")==0 && pos.compareTo("N") ==0)
	            	continue;
	            else if(POS.compareTo("X")!=0 && pos.compareTo(POS)!=0)
	            	continue;
	            
	            String Synonyms="", Antonyms="", Hypernyms="";
	            try{
	            	Synonyms= spli[2];
	            }catch(Exception e){}
	            try{
	            	Antonyms = spli[3];
	            }catch(Exception e){}
	            try{
	            	Hypernyms = spli[4];
	            }catch(Exception e){}
	            
	            ArrayList<String> syno = new ArrayList<String>(Arrays.asList(Synonyms.split(" ")));
	            ArrayList<String> anto = new ArrayList<String>(Arrays.asList(Antonyms.split(" ")));
	            ArrayList<String> hyper = new ArrayList<String>(Arrays.asList(Hypernyms.split(" ")));
	
	            ArrayList<String> hit = MyJavaUtil.Intersect(Arrays.asList(Candidatewords.keySet().toArray(new String[0])), syno);
	            if (hit.size() > 0)
	                for(String x : hit)
	                	if(Candidatewords.containsKey(Stem))
	                		WordNet[Candidatewords.get(Stem)][Candidatewords.get(x)]++;
	
	            ArrayList<String> hit2 = MyJavaUtil.Intersect(Arrays.asList(Candidatewords.keySet().toArray(new String[0])), anto);
	            if (hit2.size() > 0)
	                for(String x : hit2)
	                	if(Candidatewords.containsKey(Stem))
	                		WordNet[Candidatewords.get(Stem)][Candidatewords.get(x)]--;
	
	            ArrayList<String> hit3 = MyJavaUtil.Intersect(Arrays.asList(Candidatewords.keySet().toArray(new String[0])), hyper);
	            if (hit3.size() > 0)
	                for(String x : hit3)
	                	if(Candidatewords.containsKey(Stem))
	                		WordNet[Candidatewords.get(Stem)][Candidatewords.get(x)]++;
	        }
	        br.close();
    	}catch(IOException e){
    		System.err.println(e);
    	}
    	
        //---正規化每個row到0~1
        //for (int i = 0; i < Candidatewords.Count(); i++)
        //    WordNet[i] = Min_max_normalization(WordNet[i]);
        for (int i = 0; i < Candidatewords.size(); i++)
            for (int j = 0; j < Candidatewords.size(); j++)
                if (i == j)
                    WordNet[i][j] = 0;  
        System.out.println("Building WordNet matrix is done!");

        return WordNet;
    }

    //將transition matrix 放到陣列裡，For ConjunctionRule
    private static double[][] BuildMatrix_ConjunctionRule(String POS)
    {
    	double[][] Conj = new double[Candidatewords.size()][Candidatewords.size()];
    	try{
	        BufferedReader br = IO.Reader(IO.data_similarity_matrix+"Word_Conjunction_Negation.txt");
	        String lin = "";
			int count = 0;
	        while((lin = br.readLine()) != null)
	        {
	        	String[] spli = lin.split("\t");
	            String relation = spli[0];
	            String word1 = spli[1];
	            String word2 = spli[2];
	            String pos1 = spli[3];
	            String pos2 = spli[4];
	            
	            if(POS.compareTo("X") == 0 && (pos1.compareTo("N")==0 || pos2.compareTo("N")==0))
	            	continue;
	            else if(POS.compareTo("X") != 0 && (pos1.compareTo(POS)!=0 || pos2.compareTo(POS)!=0))
	            	continue;
	            
	            if (relation.compareTo("and") == 0)
	            {
	            	if(Candidatewords.containsKey(word1) && Candidatewords.containsKey(word2))
	            	{
		                Conj[Candidatewords.get(word1)][Candidatewords.get(word2)]++;
		                Conj[Candidatewords.get(word2)][Candidatewords.get(word1)]++;
	            	}
	            }else if (relation.compareTo("but") == 0)
	            {
	            	if(Candidatewords.containsKey(word1) && Candidatewords.containsKey(word2))
	            	{
		                Conj[Candidatewords.get(word1)][Candidatewords.get(word2)]--;
		                Conj[Candidatewords.get(word2)][Candidatewords.get(word1)]--;
	            	}
	            }
	            count++;
	        }
	        System.out.println("count = "+count);
	        
	        for (int i = 0; i < Candidatewords.size(); i++)
	        {
	            for (int j = 0; j < Candidatewords.size(); j++)
	            {
	                if (i == j)
	                    Conj[i][j] = 0;
	                else if (Conj[i][j] > 0)
	                    Conj[i][j] = Math.log10(Conj[i][j] + 1);
	                else if (Conj[i][j] < 0)
	                    Conj[i][j] = 0 - Math.log10(Math.abs(Conj[i][j]) + 1);
	            }
	        }
	
	        //---正規化每個row到0~1
            //for (int i = 0; i < Candidatewords.Count(); i++)
            //    Conj[i] = Min_max_normalization(Conj[i]);
	        br.close();
	        System.out.println("Building ConjunctionRule matrix is done!");
    	}catch(IOException e){
    		System.err.println(e);
    	}
        return Conj;
    }

    //將transition matrix 放到陣列裡，For SOC-PMI
    private static double[][] BuildMatrix_SOCPMI(String POS)
    {
    	System.out.println("Starting bulid SOC-PMI matrix...");
    	double[][] SOCPMI = new double[Candidatewords.size()][Candidatewords.size()];
    	try{
			BufferedReader br = IO.Reader(IO.data_similarity_matrix+"Word_SOCPMI_Similarity.txt");
			String lin = "";
	        while((lin = br.readLine()) != null)
	        {
	        	String[] spli = lin.split("\t");
	            String Word1 = spli[0];
	            String Word2 = spli[1];
	            double similarity = Double.parseDouble(spli[2]);
	            //------相似度處理
	
	            if (Candidatewords.containsKey(Word1) && Candidatewords.containsKey(Word2))
	                SOCPMI[Candidatewords.get(Word1)][Candidatewords.get(Word2)] = similarity;
	        }
	
	        //---正規化每個column到0~1
	        MatrixDecompositionProgram M = new MatrixDecompositionProgram();
	        SOCPMI = M.Columm_Normalized(SOCPMI);
	
	        for (int i = 0; i < Candidatewords.size(); i++)
	            for (int j = 0; j < Candidatewords.size(); j++)
	                if (i == j)
	                    SOCPMI[i][j] = 0;
    	}catch(IOException e){
    		System.err.println(e);
    	}
        return SOCPMI;
    }
    
}
