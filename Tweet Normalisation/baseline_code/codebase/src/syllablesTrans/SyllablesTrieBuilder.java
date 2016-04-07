package syllablesTrans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import languageModel.ReadSyllablesDicLm;

public class SyllablesTrieBuilder {
	
	
	public void trieGenerate() throws IOException{
		HashMap<String, Integer> syllablesTrie=new HashMap<>();
		ReadSyllablesDicLm rs=new ReadSyllablesDicLm();
		rs.load();
		for (String word:rs.word2Syllables.keySet()){
			String sys[]=rs.word2Syllables.get(word).toLowerCase().split(";");
			String trieElement="";
			for (int i=0;i<sys.length;i++){
				trieElement+=sys[i];
				//syllablesTrie.put(sys[i], 0);
				if (syllablesTrie.containsKey(trieElement)==false) syllablesTrie.put(trieElement, 0);
				if (i==sys.length-1) {
					syllablesTrie.put(trieElement, 1);
					//syllablesTrie.put(sys[i], 1);
				}
			}
		}
		
		String trieFile="./data/syllablesTrie.txt";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(trieFile), "utf-8"));
		for (String key:syllablesTrie.keySet()){
			bw.write(key+","+syllablesTrie.get(key)+"\n");
		}
		
		bw.close();
	}
	
	public HashMap<String, Integer> syllablesTrie() throws NumberFormatException, IOException{
		String trieFile="./data/syllablesTrie.txt";
		HashMap<String, Integer> tmp=new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(trieFile), "utf-8"));
		
		String line = "";
		while ((line = br.readLine()) != null) {
			String parts[]=line.split(",");
			tmp.put(parts[0], Integer.parseInt(parts[1]));
		}
		br.close();
		tmp.put("", 1);
		return tmp;
	}
	
	public static void main(String args[]) throws IOException{
		SyllablesTrieBuilder stb=new SyllablesTrieBuilder();
		stb.trieGenerate();
	}
}
