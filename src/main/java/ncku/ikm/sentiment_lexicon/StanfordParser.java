package ncku.ikm.sentiment_lexicon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.StringUtils;



public class StanfordParser
{
	public static void Stanford()
	{
		CleanUP();
		StanFord();
		Depedency2file();
		System.out.println("Stanford Parser finish!!");
	}
	
	private static void CleanUP()	//for StanFord parser
	{
		try {
			Pattern pattern_1 = Pattern.compile("@[a-zA-Z0-9_]*");
			Pattern pattern_2 = Pattern.compile("[#|$][a-zA-Z0-9_]*");
			
			BufferedReader br = IO.Reader(IO.data_path+"Corpus.txt");
			BufferedWriter bw = IO.Writer(IO.data_preprocessing+"StanFord_CleanCorpus.txt");
			String lin = "";
			while((lin = br.readLine()) != null) {
				try{
				String[] spli = lin.split("\t");
				String ID = spli[0];
				String texts = spli[1];
				bw.write(ID+"\t"+StringFilter(texts, pattern_1, pattern_2)+"\n");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			br.close();
			bw.close();
			System.out.println("CleanUP Finish!!!");
		} catch (IOException e) {
			System.err.println("CleanUP error!!");
			e.printStackTrace();
		}
	}
	
	private static String StringFilter(String inputStr, Pattern pattern_1, Pattern pattern_2)
	{
		//remove URL
		inputStr = inputStr.replaceAll("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "").trim();
		//remove RT
		inputStr = inputStr.replaceAll("RT @[a-zA-Z0-9_]*-|RT @[a-zA-Z0-9_]*:|RT @[a-zA-Z0-9_]* -|RT @[a-zA-Z0-9_]* :|RT @[a-zA-Z0-9_]*","").trim();
		//remove .
		inputStr = inputStr.replaceAll("[a-zA-Z0-9_]*\\.{2,}+", "").trim();
		//remove ()[]
		inputStr = inputStr.replaceAll("[(]\\s*[)]", "").trim();
		//remove \n
		inputStr = inputStr.replaceAll("\\\\n", "").trim();
		
		Boolean flag = true;
		Matcher m1;
		while(flag) {
			m1 = pattern_1.matcher(inputStr);// �ention
			if(m1.lookingAt()) {
				inputStr = m1.replaceFirst("").trim();
			}
			else {
				flag = false;
			}
		}
		
		List<String> raw = new ArrayList<String>(Arrays.asList(inputStr.split("\\s")));
		Collections.reverse(raw);
		inputStr = StringUtils.join(raw, " ");

		Boolean flag2 = true;
		Matcher m2;
		while(flag2) {
			m2 = pattern_2.matcher(inputStr);// �ashTag
			if(m2.lookingAt()) {
				inputStr = m2.replaceFirst("").trim();
			}
			else {
				flag2 = false;
			}
		}
		raw = new ArrayList<String>(Arrays.asList(inputStr.split("\\s")));
		Collections.reverse(raw);
		inputStr = StringUtils.join(raw, " ");

		//convert $XXX to STOCKTICKER
		inputStr = inputStr.replaceAll("[$][a-zA-Z0-9_]*", "STOCKTICKER");
		//remove @#
		inputStr = inputStr.replace("@", "").replace("#", "");
		return inputStr;
	}
	
	private static void StanFord()
	{
		LexicalizedParser lp = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		try {
			BufferedReader br = IO.Reader(IO.data_preprocessing+"StanFord_CleanCorpus.txt");
			BufferedWriter bw = IO.Writer(IO.data_preprocessing+"StanFord.txt");
			String lin = "";
			while((lin = br.readLine()) != null)
			{
				try{
					String[] spli = lin.split("\t");//ID \t texts
					String[] result = Parser(lp, spli[1]).split("\t");
					
					bw.write(spli[0]+"\n"+lin+"\n"+result[0].replace("\n", "\t")+"\n"+result[1]+"\n"+result[2]+"\n");
				}catch(IOException e){
					System.err.println("write StanFord error!!\n");
					e.printStackTrace();
					continue;
				}catch(ArrayIndexOutOfBoundsException e){
					System.err.println("write StanFord error!!\n");
					e.printStackTrace();
					continue;
				}
			}
			br.close();
			bw.close();
		}catch(IOException e){
			System.err.println("StanFord error!!\n" + e);
		}
	}

	private static String Parser(LexicalizedParser lp, String inputStr)
	{
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		List<CoreLabel> rawWords2 = tokenizerFactory.getTokenizer(
				new StringReader(inputStr)).tokenize();
		Tree parse = lp.apply(rawWords2);

		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
		TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");

		String output = StringUtils.join(tdl,"\n"); 
		
		return (output + "\t" + tp.markHeadNodes(parse).toString() + "\t" + parse.taggedYield().toString());
	}

	private static void Depedency2file()
	{
		try {
			BufferedReader br = IO.Reader(IO.data_preprocessing+"StanFord.txt");
			BufferedWriter bw = IO.Writer(IO.data_preprocessing+"StanFord_Structure.txt");
			String lin = "";
			while((lin = br.readLine()) != null)
			{
				String id = lin;
				String corpus = br.readLine();//don't comment this line
				String Dependency = br.readLine();
				String[] Structure = br.readLine().split("[(]");
				String POSTagging = br.readLine();//don't comment this line
				String[] Tagging = new String[140];
				
				Tagging[0] = "ROOT";
				int i = 1;
				for (String x : Structure) {
					if (x.matches(".*?[\\s]{1}.+?")) {
						Tagging[i++] = x.split("[=H\\s]{1}")[0];
					}
				}
				if(!Dependency.equals(""))
				{
					String[] dependency_split = Dependency.split("\t");//\n
	
					for (String stru : dependency_split) {
						try{
							String[] spli2 = stru.split("[(]");
							String[] word  = spli2[1].substring(0, spli2[1].length() - 1).split(",\\s{1}");
							String[] word1 = word[0].split("-(?=\\d+$)|-(?=\\d+'$)|-(?=\\d+''$)");
							String[] word2 = word[1].split("-(?=\\d+$)|-(?=\\d+'$)|-(?=\\d+''$)");
		
							bw.write(id+"\t"+spli2[0]+"\t"+word1[0].trim()+"\t"+word2[0].trim()+"\t"+
									Tagging[Integer.valueOf(word1[1].replaceAll("'+", ""))]+"\t"+
									Tagging[Integer.valueOf(word2[1].replaceAll("'+", ""))]+"\n");
						}catch(IOException e){
							System.err.println("Depedency2file write error!!\n" + e);
							continue;
						}
					}
				}
			}
			br.close();
			bw.close();
		} catch(IOException e) {
			System.err.println("Depedency2file error!!\n" + e);
		}
	}

}
