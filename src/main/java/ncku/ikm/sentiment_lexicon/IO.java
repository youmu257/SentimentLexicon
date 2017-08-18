package ncku.ikm.sentiment_lexicon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class IO {
	public static String path = ""; // main dir path
	public static String data_path = ""; // second layer dir path
	public static String data_preprocessing = "Preprocessing\\";
	public static String data_similarity_matrix = "SimilarityMatrix\\";
	public static String result_wordnet = "WordNet\\";
	public static String result_socpmi = "SOC_PMI\\";
	public static String result_conjuction_rule = "ConjuctionRule\\";
	public static String data_propagation = "Propagation\\";
	
	
	public static void PathInit(String PATH)
	{
		path = PATH;
		data_path = path + "Input\\";
		data_preprocessing = path+data_preprocessing;
		data_similarity_matrix = path+data_similarity_matrix;
		data_propagation = path+data_propagation;
		result_wordnet = data_propagation+result_wordnet;
		result_socpmi = data_propagation+result_socpmi;
		result_conjuction_rule = data_propagation+result_conjuction_rule;
		
		
		mkdir(data_path);
		mkdir(data_preprocessing);
		mkdir(data_similarity_matrix);
		mkdir(result_wordnet);
		mkdir(result_socpmi);
		mkdir(result_conjuction_rule);
		mkdir(data_propagation);
	}
	
	/** 
	 * Create folder if not exits
	 * @param path : folder path
	 */
	public static void mkdir(String path)
	{
		if(!new File(path).exists())
			new File(path).mkdirs();
	}
	
	public static BufferedReader Reader(String path) throws IOException
	{
		return new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
	}
	
	public static BufferedWriter Writer(String path) throws IOException
	{
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8"));
	}
}
