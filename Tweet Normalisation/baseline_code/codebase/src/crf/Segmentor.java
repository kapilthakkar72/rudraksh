package crf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.chasen.crfpp.Tagger;

public class Segmentor {


	public final int num=10;
	public ArrayList<Segmentation> syllableSegment(String word){
		ArrayList<Segmentation> syllablesList=new ArrayList<>();
		
		Tagger tagger = new Tagger("-m ./crf/model -v 3 -n2");
		tagger.clear();
		
		
		String vowels="aeiou";
		for (int i=0;i<word.length();i++){
			if (vowels.contains((word.charAt(i)+""))){
				tagger.add(word.charAt(i)+" V");
			}
			else tagger.add(word.charAt(i)+" C");
		}
		
//		for (int i=0;i<word.length();i++){
//			if (vowels.contains((word.charAt(i)+""))){
//				if (i==word.length()-1)
//					tagger.add(word.charAt(i)+" V W");
//				else 
//					tagger.add(word.charAt(i)+" V I");
//			}
//			else {
//				if (i==word.length()-1)
//					tagger.add(word.charAt(i)+" C W");
//				else
//					tagger.add(word.charAt(i)+" C I");
//			}
//		}
		if (!tagger.parse())
			return syllablesList;
		for (int i=0;i<num;i++){
			if (! tagger.next()) break;
			char[] syllable=new char[word.length()*2];
			int pchar=0;
			for (int j=0;j<tagger.size();j++){
				syllable[pchar++]=word.charAt(j);
				if (tagger.y2(j).equals("E")){	
					syllable[pchar++]='&';
				}
			}
			if (syllable[pchar-1]=='&') pchar--;
			String syString=new String(syllable,0,pchar);
			syllablesList.add(new Segmentation(syString, tagger.prob()));
		}
		HashMap<String, Double> syllablesMap=new HashMap<>();
		for (Segmentation segment:syllablesList){
			double prob=segment.prob;
			if (syllablesMap.containsKey(segment.seg)) prob+=syllablesMap.get(segment.seg);
			syllablesMap.put(segment.seg, prob);
		}
		syllablesList.clear();
		for (String key:syllablesMap.keySet()){
			syllablesList.add(new Segmentation(key, syllablesMap.get(key)));
		}
		Collections.sort(syllablesList);
		return syllablesList;
	}
	
	public static void main(String args[]){
		String word="edcation";
		Segmentor seg=new Segmentor();
		ArrayList<Segmentation> sys=seg.syllableSegment(word);
		for (int i=0;i<sys.size();i++){
			System.out.println(sys.get(i).seg);
			System.out.println(sys.get(i).prob);
		}
	}
	static {
		  try {
		    System.loadLibrary("CRFPP");
		  } catch (UnsatisfiedLinkError e) {
		    System.err.println("Cannot load the example native code.\nMake sure your LD_LIBRARY_PATH contains \'.\'\n" + e);
		    System.exit(1);
		  }
		}
}
