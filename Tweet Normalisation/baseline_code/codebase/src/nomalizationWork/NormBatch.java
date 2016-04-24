package nomalizationWork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class NormBatch {
	public static void main(String args[]) throws IOException{
		ViterbiDecoder vd=new ViterbiDecoder();
		String infile="./data/test.txt";
		//String infile=args[0];
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(infile), "utf-8"));
		String outfile="./result_full.txt";
		//String outfile=args[1];
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outfile), "utf-8"));
		
		if (args.length>2){
			vd.setAttr(Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
		}
		String line="";
		while ((line=br.readLine())!=null){
			String sentence=line;
			String label=br.readLine();
			String answer=br.readLine();
			br.readLine();
			vd.setSentence(sentence, label);
			String result=vd.viterbiWork();
			bw.write(sentence+"\n");
			bw.write(result+"\n");
			bw.write(answer+"\n\n");
		}
		
		br.close();
		bw.close();
	
	}
}
