package ncku.ikm.sentiment_lexicon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyJavaUtil {
	
	//交集
    public static ArrayList<String> Intersect(List<String> origin,List<String> selected)
    {
    	ArrayList<String> intersect = new ArrayList<String>();
    	for(String s : origin)
    		if(selected.contains(s))
    			intersect.add(s);
    	return intersect;
    }
    
    public static double[] toDouble(String[] s)
    {
    	double[] tmp = new double[s.length];
    	for(int i=0;i<s.length;i++)
    		tmp[i] = Double.parseDouble(s[i]);
    	return tmp;
    }
    
    //以 join 串聯 set 成字串
    public static String StringJoin(String join, Set<String> arr)
    {
    	StringBuilder s = new StringBuilder();
    	for(String key : arr)
    	{
    		s.append(key).append(join);
    	}
    	return s.toString().substring(0, s.length()-1);
    }
    
    //以 join 串聯陣列成字串
    public static String StringJoin(String join, double[] arr)
    {
    	StringBuilder s = new StringBuilder();
    	for(double num : arr)
    		s.append(num + "").append(join);
    	return s.toString().substring(0, s.length()-1);
    }
    
    //Math.log2
    public static double log2(double n)
    {
    	return Math.log(n) / Math.log(2.0);
    }
}
