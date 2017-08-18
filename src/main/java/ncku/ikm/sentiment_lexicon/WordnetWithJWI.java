package ncku.ikm.sentiment_lexicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import edu.mit.jwi.*;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

public class WordnetWithJWI
{
	private static IDictionary dict;
	
	public static void Stemming(String wordnet)
	{
		ReadDic(wordnet);
		stemmer2file();
		System.out.println("Stemming finish!!");
	}
	
	//Read Dictionary
	private static void ReadDic(String dir)
	{
		String path = dir;
		URL url = null;
		try {
			url = new URL("file", null, path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (url == null)
			return;

		// construct the dictionary object and open it
		dict = new Dictionary(url);
		try {
			dict.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void stemmer2file()
	{
		// stemmer object
		WordnetStemmer WS = new WordnetStemmer(dict);
		WS.getDictionary();
		try{
			BufferedWriter bw = IO.Writer(IO.data_preprocessing+"NLP_Stemmer.txt");
			
			for (double i = 0.5; i <= 0.5; i = i + 0.10)
			{
				BufferedReader br = IO.Reader(IO.data_preprocessing + "Candidate_"+ i + ".txt");
				String lin="";
				IIndexWord idxWord;
				
				int max = 0;
				POS initPOS = null;
				
				while ((lin = br.readLine()) != null)
				{
					String[] spli = lin.split("\t");// word \t pos
					initPOS = POS_mapping(spli[1]);

					if (spli[0].startsWith("anti-")) {
						spli[0] = spli[0].replaceFirst("-", "");
					}
					List<String> tmp = WS.findStems(spli[0], initPOS);
					String stem = "";
					max = 0;
					for (String x : tmp) {
						try{
							idxWord = dict.getIndexWord(x, initPOS);
							
							if(x.compareTo(spli[0]) == 0 && !x.endsWith("s"))
							{	
								stem = x;
								break;
							}
							else if (idxWord != null && idxWord.getTagSenseCount() >= max) {
								stem = x;
								max = idxWord.getTagSenseCount();
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					if (stem.compareTo("") != 0) {//is candidate
						bw.write(spli[0]+"\t1\t"+i+"\t"+stem+"\t"+spli[1]+"\n");
					} else {
						bw.write(spli[0]+"\t0\t"+i+"\t"+spli[0]+"\t"+spli[1]+"\n");
					}
				}
				br.close();
			}
			bw.close();
		} catch (IOException e) {
			System.err.println("stemmer2file error!!\n" + e);
		}
	}
	
	private static POS POS_mapping(String s)
	{
		POS initPOS = null;
		if(s.compareTo("A") == 0){
			initPOS = POS.ADJECTIVE;
		}else if(s.compareTo("N") == 0){
			initPOS = POS.NOUN;
		}else if(s.compareTo("R") == 0){
			initPOS = POS.ADVERB;
		}else if(s.compareTo("V") == 0){
			initPOS = POS.VERB;
		}

		return initPOS;
	}

}
