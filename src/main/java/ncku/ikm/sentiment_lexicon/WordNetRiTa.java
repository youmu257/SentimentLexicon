package ncku.ikm.sentiment_lexicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import rita.wordnet.RiWordnet;

public class WordNetRiTa
{
	public static void WordNet()
	{
		RelationToDB();
		System.out.println("WordNet finish!!");
	}
	
	private static void RelationToDB() 
	{
		RiWordnet wordnet = new RiWordnet();
		try{
			BufferedReader br = IO.Reader(IO.data_preprocessing+"NLP_Stemmer.txt");
			BufferedWriter bw = IO.Writer(IO.data_similarity_matrix+"WordNet_Relation.txt");
			
			String lin = "";
			int j=0;
			while((lin = br.readLine()) != null)
			{
				String[] spli = lin.split("\t");
				String stem_word = spli[3];
				String POS = spli[4];

				String[] Syno = wordnet.getAllSynonyms(stem_word, POS);
				String[] Similar = wordnet.getAllSimilar(stem_word, POS);
				String[] Synsets = wordnet.getAllSynsets(stem_word, POS);
				String[] tmp = ArrayUtils.addAll(Syno, Similar);
	
				String[] Same = ArrayUtils.addAll(tmp, Synsets);
				String[] Different = wordnet.getAllAntonyms(stem_word, POS);
				String[] Hypernyms = wordnet.getAllHypernyms(stem_word, POS);
	
				String same = StringUtils.join(Same, " ");
				String different = StringUtils.join(Different, " ");
				String Hyper = StringUtils.join(Hypernyms, " ");

				if (same == null)
					same = "";
				if (different == null)
					different = "";
				if (Hyper == null)
					Hyper = "";

				bw.write(stem_word+"\t"+POS+"\t"+same+"\t"+different+"\t"+Hyper+"\n");

				j++;
				if(j%1000==0)
					bw.flush();
			}
			br.close();
			bw.close();
		}catch(IOException e){
			System.err.println("RelationToDB error!!\n" + e);
		}
	}
	
}
