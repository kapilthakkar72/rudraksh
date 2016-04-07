package languageModel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class ReadSyllablesDicLm {
	public HashMap<String, String>word2Syllables=new HashMap<>();
	
	//because of some issues of dictionary.com 
	//trying to naively handle different form of a word like: apple->apples
	public String reverseRecover(String a,String b,String c){
		int index=0;
		String str=c;
		if ((index=a.indexOf(b))!=-1){
			str=new String(a.getBytes(),0,index)+";"+c+";"+new String(a.getBytes(),index+b.length(),a.length()-b.length()-index);
		}
		if (str.startsWith(";")) str=new String(str.getBytes(),1,str.length()-1);
		if (str.endsWith(";")) str=new String(str.getBytes(),0,str.length()-1);
		str=str.replaceAll(";s$", "s");
		str=str.replaceAll(";'s$", "'s");
		str=str.replaceAll(";d$", "d");
		str=str.replaceAll(";r$", "r");
		return str;
	}
	
	public void load() throws IOException{
		String infile = "./data/all_syllables";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(infile), "utf-8"));
		
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			parts[2]=parts[2].replace("·", ";");
			if (parts[0].equals(parts[1])==false) parts[2]=reverseRecover(parts[0],parts[1],parts[2]);
			word2Syllables.put(parts[0], parts[2]);
		}
		br.close();
	}
	public static void main(String args[]) throws IOException{
		ReadSyllablesDicLm rsd=new ReadSyllablesDicLm();
		rsd.load();
	}
}
