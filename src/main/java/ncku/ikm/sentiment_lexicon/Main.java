package ncku.ikm.sentiment_lexicon;

public class Main {
	private static String path = "Data\\";
	private static String wordnet_path = "Tool\\wordnet_2.1\\dict";
	
	public static void main(String[] args) {
		int n1=5 ,n2=1, n3=1;//the ratio of soc-pmi and conjunction and wordnett
		long start = System.currentTimeMillis();
		IO.PathInit(path);
		Preprocessing();
		
		SimilarityMatrixGeneration();
		int[] ratio = {n1,n2,n3};
		Propagation.propagation("X", ratio);
		System.out.println("total spend "+(System.currentTimeMillis()-start)/1000+" s");
    }
	
	public static void Preprocessing()
	{
		POSTagging.POSTagging1();
		ExtractCandidate.CandidateExtract();
		WordnetWithJWI.Stemming(wordnet_path);
		POSTagging.POSTagging2();
		StanfordParser.Stanford();
	}
	
	public static void SimilarityMatrixGeneration()
	{
		ConjuctionRule.Conjuction_Rule();
		SOCPMI.SOC_PMI();
		WordNetRiTa.WordNet();
	}
}
