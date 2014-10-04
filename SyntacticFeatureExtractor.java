import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Neeti
public class SyntacticFeatureExtractor {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		String descriptionFileName = "/Users/neetipokhriyal/Research/data/30Min_tier1_desc.txt";
		String featuresFileName = "/Users/neetipokhriyal/Research/data/30Min_tier1_feature.txt";
		File f = new File(descriptionFileName);
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		File f1 = new File(featuresFileName);
		BufferedWriter featureWriter = new BufferedWriter(new FileWriter(f1));
		
		HashMap<String, ArrayList<Blog>> map = new HashMap<String, ArrayList<Blog>>();
		int total = 0;

		try (BufferedReader br = new BufferedReader(new FileReader("/Users/neetipokhriyal/Research/data/tier1/tier1_icwsm09stories.txt")))
		{
			int linesRead = 0;
			long s,e;
			s = System.currentTimeMillis();
			String sCurrentLine=null;
			while ((sCurrentLine = br.readLine()) != null) {
				Blog blog = new Blog(); 
				blog.author_name = sCurrentLine.substring(0, sCurrentLine.indexOf("::")).trim();
				blog.desc = sCurrentLine.substring(sCurrentLine.indexOf("::")+2).trim();
				
				if(blog.author_name != null && blog.desc !=null){
					if(map.containsKey(blog.author_name)){
						map.get(blog.author_name).add(blog);
					}
					else{
						ArrayList<Blog> a1 = new ArrayList<Blog>();
						a1.add(blog);
						map.put(blog.author_name, a1);
						total ++;
					}
				}

				linesRead++;
				if(linesRead%1000 == 0)
				{
					e = System.currentTimeMillis();
					System.out.println("Read "+linesRead+" lines in"+(e-s)/1000);
					s = System.currentTimeMillis(); 
				}

			}
			br.close();  

		}

		int t=0;
		int b = 0;
		long s1,e1;
		int linesRead1 = 0;
		
		for(String s: map.keySet())
		{  
			s1 = System.currentTimeMillis();
			
			if (map.get(s).size()>30){  // NUMBER OF BLOGS PER AUTHOR
				ArrayList<Blog> blogs = map.get(s);
				for(Blog blog: blogs)
				{
					if(blog.author_name != null)
					{
						Vector<Double> features = getFeatures(blog);
						//add more features here
						//print all features
						for(Double d: features)
						{
							b += 1;
							featureWriter.write(d+",");
						}
						//print the class name
						featureWriter.write(t+"\n");
						writer.write(blog.desc+"\n");
						
						linesRead1++;
						if(linesRead1%1000 == 0)
						{
							e1 = System.currentTimeMillis();
							System.out.println("Features Extracted "+linesRead1+" lines in"+(e1-s1)/1000);
							s1 = System.currentTimeMillis(); 
						}
						
					}
				}
				t++;
				//	System.out.println("Author name:"+s+"\n No. of posts:"+map.get(s).size()+"\n");
			}
		}
		//System.out.println("Unique Author names after trimming for size>5:"+t);
		//System.out.println("\n datset:"+b);
		writer.close();
		featureWriter.close();
	}

	static int avg = 0;

	private static Vector<Double> getFeatures(Blog blog)
	{
		Vector<Double> features = new Vector<Double>();
		String description;
		//create a hashmap
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		//description = blog.desc.replaceAll("\\,", "");
		description =  blog.desc.replaceAll("[~$%#\\(\\):,;_\\/]+", "");
		description =  description.replaceAll("@+", "");
		description = description.replaceAll("\\^+", "");
		String[] tokens = description.split(" ");

		for(String s: tokens)
		{
			if(map.containsKey(s.toLowerCase()))
			{
				Integer c = map.get(s.toLowerCase()) + 1;
				map.put(s.toLowerCase(), c);
			}
			else
			{
				map.put(s.toLowerCase(), new Integer(1));
			}
		}

		double f11 = yuleKRichness(map);
		features.add(f11);

		double f2 = map.size(); //no of words
		features.add(f2);

		double f1 = characterlength(blog.desc);
		features.add(f1);

		double f3 = alluppercasefreq(blog.desc);
		features.add(f3/f1);

		double f4 = alllowercasefreq(blog.desc);
		features.add(f4/f1);

		double f5 = camelcasefreq(blog.desc);
		features.add(f5/f1);

		double f6 = firstuppercasefreq(blog.desc);
		features.add(f6/f1);

		String[] puncts = {"\\,","\\.","\\?","\\!","\\;","\\:","\\(","\\)","\"","\\-","\\'"};
		for(int j=0;j<11;j++){
			double f7 = punctfreq(blog.desc,puncts[j]);
			features.add(f7/f1);
			avg++;
		}
		String[] specchars = {"\\`","\\~","\\@","\\#","\\$","\\%","\\^","\\&","\\*","\\_","\\+","\\=","\\[","\\]","\\{","//}","\\|","\\/","\\<","\\>"};
		for(int j=0;j<20;j++){
			double f8 = punctfreq(blog.desc,specchars[j]);
			features.add(f8/f1);
			avg++;
		}

		double[] f9 = stopwordsfreq(map);
		//System.out.println(f9.length);
		for (int j=0; j<f9.length;j++){
			features.add(f9[j]/f1);
			avg++;
		}
		for(int j=0;j<10;j++){
			double f10 = punctfreq(blog.desc,""+j+"");
			features.add(f10/f1);
			avg++;
		}

		for(int k=1;k<=20;k++){
			double f12 = lengthk(map,k);
			features.add(f12/f1);
		}

		double[] f13= letterfreq(blog.desc);
		for (int j=0; j<f13.length;j++){
			features.add(f13[j]/f1);
		}
		//System.out.println(features.size());
		return features;
	}

	private static double lengthk(HashMap<String, Integer> map, int k) {
		int c = 0;
		for(String s: map.keySet())
		{
			if(s.length() == k)
				c += map.get(s);
		}
		return c;
	}

	private static double yuleKRichness(HashMap<String, Integer> map) {
		Stemmer st;
		HashMap<String, Integer> stemMap = new HashMap<String, Integer>();
		for(String s: map.keySet())
		{
			st = new Stemmer();
			for (char c: s.toCharArray()) {
				st.add(c);
			}

			st.stem();
			String stem = st.toString(); 
			if(stemMap.containsKey(stem))
			{
				Integer i = stemMap.get(stem);
				stemMap.put(stem, i + map.get(s));
			}
			else
			{
				stemMap.put(stem, map.get(s));
			}
		}
		int m1 = stemMap.size();
		int m2 = 0;
		for(Integer i: stemMap.values())
		{
			m2 += Math.pow(i,2);
		}
		return Math.pow(m1, 2)/(m2 * m1);
//		should be - return Math.pow(m1, 2)/(m2 - m1);
	}

	private static String stripHTMLtags(String content) {
		String s = content.replaceAll("<(.|\n)*?>", "");
		s = s.replaceAll("&.+;", "");
		s = s.replace("\n", "").replace("\r", "");
		s = s.replaceAll("Send to a friend", "");
		s = s.replaceAll("Read and post comments","");
		s = s.replaceAll("\\|", "");
		return s;
	}

	public static double characterlength(String description)
	{
		return description.length();
	}

	public static double alluppercasefreq(String description)
	{
		String[] tokens = description.split(" ");
		int c = 0;
		for(String s: tokens)
		{
			if(isAllUpper(s))
				c += 1;
		}
		return c;
	}

	public static double alllowercasefreq(String description)
	{
		String[] tokens = description.split(" ");
		int c = 0;
		for(String s: tokens)
		{
			if(isAllLower(s))
				c += 1;
		}
		return c;
	}

	public static double camelcasefreq(String description)
	{
		String[] tokens = description.split(" ");
		int c = 0;
		for(String s: tokens)
		{
			if(camelcase(s))
				c += 1;
		}
		return c;
	}

	public static double firstuppercasefreq(String description)
	{
		String[] tokens = description.split(" ");
		int c = 0;
		for(String s: tokens)
		{
			if(firstUpper(s))
				c += 1;
		}
		return c;
	}

	public static double punctfreq(String description, String punct)
	{ 
		Pattern p;
		p = Pattern.compile(punct);
		Matcher m = p.matcher(description);
		int count = 0;
		while (m.find()) {
			count++;
		}
		return count;
	}

	public static double[] stopwordsfreq(HashMap<String, Integer> map)
	{
		String stopStr = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
		String[] stopWords = stopStr.split(",");
		double[] result  = new double[stopWords.length];
		for(int i = 0; i < stopWords.length; i++)
		{
			String sW = stopWords[i];
			if(map.containsKey(sW))
				result[i] = map.get(sW);
			else
				result[i] = 0;
		}
		return result;
	}

	private static boolean isAllUpper(String s) {
		for(char c : s.toCharArray()) {
			if(Character.isLetter(c) && Character.isLowerCase(c)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isAllLower(String s) {
		for(char c : s.toCharArray()) {
			if(Character.isLetter(c) && Character.isUpperCase(c)) {
				return false;
			}
		}
		return true;
	}

	public static double[] letterfreq(String ht)
	{
		String st = ht.toLowerCase();    
		double[] alphabetArray = new double[26];

		for ( int i = 0; i < st.length(); i++ ) {
			char ch=  st.charAt(i);
			int value = (int) ch;
			if (value >= 97 && value <= 122){
				alphabetArray[ch-'a']++;
			}
		}
		return alphabetArray; 
	}

	private static boolean camelcase(String s) {

		if(s.length()<2)
			return false;
		char[] c = s.toCharArray();
		for(int i = 0; i<c.length-1; i++) {
			if(Character.isLetter(c[i]) && Character.isLowerCase(c[i]) && Character.isLetter(c[i+1]) && Character.isUpperCase(c[i+1])) {
				return true;
			}
		}
		return false;
	}

	private static boolean firstUpper(String s) {	
		if (s.length() == 0)
			return false;
		char[] c = s.toCharArray();
		for(int i = 0; i<c.length-1; i++) {
			if(Character.isLetter(c[i]) && Character.isUpperCase(c[i]) && Character.isLetter(c[i+1]) && Character.isLowerCase(c[i+1])) {
				return true;
			}
		}
		return false;
	}
}

