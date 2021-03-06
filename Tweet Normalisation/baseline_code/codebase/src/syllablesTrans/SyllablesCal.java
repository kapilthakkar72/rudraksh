package syllablesTrans;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.codec.language.DoubleMetaphone;

import crf.Segmentation;
import crf.Segmentor;

import languageModel.ReadLmProb;

public class SyllablesCal {
	public ReadLmProb lmprob;
	public DoubleMetaphone dm = new DoubleMetaphone();
	public HashMap<String, ArrayList<String>> tts4Syllables = new HashMap<>();
	public HashMap<String, ArrayList<String>> ttsofSyllables = new HashMap<>();
	public HashMap<String, Integer> syllablesTrie = new HashMap<>();
	public LetterToSoundPhones lts;
	public ArrayList<ArrayList<ArrayList<String>>> alterSyllables = new ArrayList<>();
	public ArrayList<String> syList=new ArrayList<>();
	public String _word;
	public ArrayList<Segmentation> _segments = new ArrayList<>();

	private int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public int ED(String str1, String str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == (str2.charAt(j - 1))) ? 0
										: 1));
		return distance[str1.length()][str2.length()];
	}

	private int LCS(String x, String y) {

		int i, j;
		int lenx = x.length();
		int leny = y.length();
		int[][] table = new int[lenx + 1][leny + 1];

		// Initialize table that will store LCS's of all prefix strings.
		// This initialization is for all empty string cases.
		for (i = 0; i <= lenx; i++)
			table[i][0] = 0;
		for (i = 0; i <= leny; i++)
			table[0][i] = 0;

		// Fill in each LCS value in order from top row to bottom row,
		// moving left to right.
		for (i = 1; i <= lenx; i++) {
			for (j = 1; j <= leny; j++) {

				// If last characters of prefixes match, add one to former
				// value.
				if (x.charAt(i - 1) == y.charAt(j - 1))
					table[i][j] = 1 + table[i - 1][j - 1];

				// Otherwise, take the maximum of the two adjacent cases.
				else
					table[i][j] = Math.max(table[i][j - 1], table[i - 1][j]);
			}
		}
		return table[lenx][leny];
	}

	public void soundFromDic() throws IOException {
		String infile = "./data/sound4Syllables.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(infile), "utf-8"));
		String line = "";
		while ((line = br.readLine()) != null) {
			String syllables = line.split(",")[0];
			String sound = line.split(",")[1];
			ArrayList<String> sys1sound = new ArrayList<>();
			if (tts4Syllables.containsKey(sound))
				sys1sound = tts4Syllables.get(sound);
			sys1sound.add(syllables);
			tts4Syllables.put(sound, sys1sound);

			ArrayList<String> sounds1sy = new ArrayList<>();
			if (ttsofSyllables.containsKey(syllables))
				sounds1sy = ttsofSyllables.get(syllables);
			sounds1sy.add(sound);
			ttsofSyllables.put(syllables, sounds1sy);
		}
		br.close();
	}

	public SyllablesCal() throws IOException {
		lts = new LetterToSoundPhones();
		lmprob = new ReadLmProb();
		lmprob.loadSyllablesUni();
		soundFromDic();
		SyllablesTrieBuilder stb = new SyllablesTrieBuilder();
		syllablesTrie = stb.syllablesTrie();
	}

	public String extractCV(String sound) {
		String consonantStr = "";
		String vowelStr = "";
		String sounds[] = sound.split(" ");
		for (int i=0;i<sounds.length;i++) {
			String tmp = sounds[i].trim();
			if (tmp.contains("R")) {
				vowelStr += tmp;
				consonantStr += "R";
				continue;
			}
			if (tmp.equals("W")&&(i>=1)
					&& ("AEIOU".contains(sounds[i-1].charAt(0)+"") == false)) {
				//System.out.println("##"+sounds[i-1]);
				vowelStr += tmp;
				continue;
			}
			if (tmp.equals("AA") || tmp.equals("AE") || tmp.equals("AH")
					|| tmp.equals("AO") || tmp.equals("AW") || tmp.equals("AY")
					|| tmp.equals("EH") || tmp.equals("ER") || tmp.equals("EY")
					|| tmp.equals("IY") || tmp.equals("IH") || tmp.equals("OW")
					|| tmp.equals("OY") || tmp.equals("UH") || tmp.equals("UW")) {
				vowelStr += tmp;
			} else {
				consonantStr += tmp;
			}
		}
		if (vowelStr.equals(""))
			vowelStr = " ";
		return consonantStr + "##" + vowelStr;
	}

	//consonant rules
	private boolean checkC(String str1, String str2) {
		if (str1.equals(str2))
			return true;
		if ((str1.endsWith("S") && str2.endsWith("Z"))
				|| (str1.endsWith("Z") && str2.endsWith("S"))
				|| (str1.endsWith("Z") && str2.endsWith("Z"))
				|| (str1.endsWith("S") && str2.endsWith("S"))) {
			// System.out.println("!!!");
			str1 = new String(str1.getBytes(), 0, str1.length() - 1);
			str2 = new String(str2.getBytes(), 0, str2.length() - 1);
			if (str1.equals(str2))
				return true;
		}
		// System.out.println(str1+" "+str2);
		if ((str1.endsWith("NG") && str2.endsWith("N"))
				|| (str1.endsWith("N") && str2.endsWith("NG"))) {
			String tmp1 = new String(str1.getBytes(), 0, str1.length() - 1);
			String tmp2 = new String(str2.getBytes(), 0, str2.length() - 1);
			if (str1.endsWith("NG"))
				tmp1 = new String(str1.getBytes(), 0, str1.length() - 2);
			if (str2.endsWith("NG"))
				tmp2 = new String(str2.getBytes(), 0, str2.length() - 2);
			return tmp1.equals(tmp2);
		}
		if ((str1.endsWith("NG") && str2.endsWith("G"))
				|| (str1.endsWith("G") && str2.endsWith("NG"))) {
			String tmp1 = new String(str1.getBytes(), 0, str1.length() - 1);
			String tmp2 = new String(str2.getBytes(), 0, str2.length() - 1);
			if (str1.endsWith("NG"))
				tmp1 = new String(str1.getBytes(), 0, str1.length() - 2);
			if (str2.endsWith("NG"))
				tmp2 = new String(str2.getBytes(), 0, str2.length() - 2);
			return tmp1.equals(tmp2);
		}
		if ((str1.endsWith("T") && str2.endsWith("D"))
				|| (str1.endsWith("D") && str2.endsWith("T"))) {
			String tmp1 = new String(str1.getBytes(), 0, str1.length() - 1);
			String tmp2 = new String(str2.getBytes(), 0, str2.length() - 1);
			if (tmp1.equals(tmp2))
				return true;
		}
		if ((str1.endsWith("M") && str2.endsWith("N"))
				|| (str1.endsWith("N") && str2.endsWith("M"))) {
			String tmp1 = new String(str1.getBytes(), 0, str1.length() - 1);
			String tmp2 = new String(str2.getBytes(), 0, str2.length() - 1);
			if (tmp1.equals(tmp2))
				return true;
		}
		if ((str2.length() >= 2)
				&& (str2.endsWith("T") || str2.endsWith("R")
						|| str2.endsWith("D") || str2.endsWith("N"))) {
			String tmp = new String(str2.getBytes(), 0, str2.length() - 1);
			if (tmp.equals(str1))
				return true;
		}
		if ((str2.startsWith("DH") || str2.startsWith("TH"))
				&& str1.startsWith("D")) {
			String tmp2 = new String(str2.getBytes(), 2, str2.length() - 2);
			String tmp1 = new String(str1.getBytes(), 1, str1.length() - 1);
			if (tmp1.equals(tmp2))
				return true;
		}
		if (str2.startsWith("W")) {
			String tmp = new String(str2.getBytes(), 1, str2.length() - 1);
			if (tmp.equals(str1))
				return true;
		}
		return false;
	}

	private int checkSim(String _str1, String _str2) {
		String str1[] = extractCV(_str1).split("##");
		String str2[] = extractCV(_str2).split("##");
		if (checkC(str1[0], str2[0]) == false)
			return 10;
		// System.out.println("!!"+str1[1]+","+str2[1]+","+_str1+","+_str2);
		return ED(str1[1], str2[1]);
	}

	public int EdThresh = 4;

	// return a 3D data list.
	// There are several types of segment.
	// Every type of segment contains many syllables.
	// Every syllables contains many alternatives.
	public void syllableWeights(ArrayList<Segmentation> segments) {
		alterSyllables.clear();
		// for restriction of first letter
		_word = segments.get(0).seg.replace("&", "");
		_segments = segments;
		for (Segmentation seg : segments) {
			// System.out.println(seg.seg);
			ArrayList<ArrayList<String>> candidateOfSegments = new ArrayList<>();
			boolean flag = true;
			String syllables[] = seg.seg.split("&");
			for (int i = 0; i < syllables.length; i++) {
				// flag = true;
				String ttsCode = lts.getLetterToSoundPhones(syllables[i]);
				// H case
				if ((syllables[i].length() >= 2)
						&& syllables[i].startsWith("h")
						&& ("aeiouy".indexOf(syllables[i].charAt(1)) == -1)
						&& (ttsCode.startsWith("H") == false))
					ttsCode = "HH " + ttsCode;
				ArrayList<String> likeSyllables = new ArrayList<>();
				for (String _tts : tts4Syllables.keySet()) {
					if (checkSim(ttsCode, _tts) <= EdThresh) {
						likeSyllables.addAll(tts4Syllables.get(_tts));
					}
				}
				candidateOfSegments.add(likeSyllables);
				if (likeSyllables.size() == 0)
					flag = false;
			}
			if (!flag)
				candidateOfSegments.clear();
			alterSyllables.add(candidateOfSegments);
		}
	}

	public boolean check(String prefix, String suffix) {
		String word = (prefix + suffix).replace("&", "");
		if (syllablesTrie.containsKey(word))
			return true;
		return false;
	}

	public ArrayList<String> syllablesFilterMerge(ArrayList<String> oneWord,
			ArrayList<String> alters) {
		// System.out.println("******");
		ArrayList<String> tmp = new ArrayList<>();
		for (String prefix : oneWord) {
			for (String suffix : alters) {
				// System.out.println(prefix + "   " + suffix);
				if (check(prefix, suffix) == true)
					tmp.add(prefix + "&" + suffix);
			}
		}
		return tmp;
	}

	public boolean checkConsonant(String legalWord) {
		if (syllablesTrie.containsKey(legalWord) == false)
			return false;
		if (syllablesTrie.get(legalWord) == 0)
			return false;
		// if ((LCS(legalWord, _word) * 2 < _word.length())&&(_word.length()>3))
		// return false;
		if (legalWord.length() == 1)
			return false;
		if ("aeiouy".indexOf(legalWord.charAt(0)) == -1) {
			if (legalWord.charAt(0) != _word.charAt(0)) {
				if ("aeiou".indexOf(legalWord.charAt(0)) != -1)
					return true;
				if ((legalWord.startsWith("f") || legalWord.startsWith("ph"))
						&& (_word.startsWith("f") || _word.startsWith("ph")))
					return true;
				if ((legalWord.startsWith("th")) && (_word.startsWith("d")))
					return true;
				if ((legalWord.startsWith("k") || legalWord.startsWith("c"))
						&& (_word.startsWith("k") || _word.startsWith("c")))
					return true;
				return false;
			}
		}
		return true;
	}

	// Reducing: turning a 3D list to a 2D list
	// every word has several seg types
	// every seg has several candidates
	public ArrayList<ArrayList<String>> candidateWords() {
		ArrayList<ArrayList<String>> candidateWords = new ArrayList<>();
		for (int i = 0; i < alterSyllables.size(); i++) {
			ArrayList<ArrayList<String>> oneSegment = alterSyllables.get(i);
			ArrayList<String> oneWord = new ArrayList<>();
			oneWord.add("");
			for (int j = 0; j < oneSegment.size(); j++) {
				ArrayList<String> alters = oneSegment.get(j);
				oneWord = syllablesFilterMerge(oneWord, alters);

			}
			if (oneSegment.size() == 0)
				oneWord.clear();

			// Reducing: eliminate according to consonant
			ArrayList<String> oneWordCache = (ArrayList<String>) oneWord
					.clone();
			oneWord.clear();
			HashSet<String> wordSet = new HashSet<>();
			for (String legalWord : oneWordCache) {
				String pureWord = legalWord.replace("&", "");
				if (checkConsonant(pureWord) == false)
					continue;
				if (wordSet.contains(legalWord))
					continue;
				oneWord.add(legalWord);
				wordSet.add(legalWord);
			}
			String OOVSeg[] = _segments.get(i).seg.split("&");
			oneWordCache = (ArrayList<String>) oneWord.clone();
			oneWord.clear();
			for (String legalWord : oneWordCache) {
				// System.out.println(legalWord);
				String IVSeg[] = legalWord.split("&");
				int LCSCount = 0;
				int EDCount = 0;
				for (int j = 1; j < IVSeg.length; j++) {
					// System.out.println(IVSeg[j]);
					ArrayList<String> IVttsCode = ttsofSyllables.get(IVSeg[j]);
					String OOVttsCode = lts
							.getLetterToSoundPhones(OOVSeg[j - 1]);
					if ((OOVSeg[j - 1].length() >= 2)
							&& OOVSeg[j - 1].startsWith("h")
							&& ("aeiouy".indexOf(OOVSeg[j - 1].charAt(1)) == -1)
							&& (OOVttsCode.startsWith("H") == false))
						OOVttsCode = "HH " + OOVttsCode;
					OOVttsCode = OOVttsCode.replace(" ", "");
					int edTmp = 6;
					int lcsTmp = 0;
					for (int k = 0; k < IVttsCode.size(); k++) {
						int tmp = 0;
						if ((tmp = ED(IVttsCode.get(k).replace(" ", ""),
								OOVttsCode)) < edTmp)
							edTmp = tmp;
						if ((tmp = LCS(IVttsCode.get(k).replace(" ", ""),
								OOVttsCode)) > lcsTmp)
							lcsTmp = tmp;
					}
					if (OOVSeg[j - 1].startsWith("u")) {
						OOVttsCode = lts.getLetterToSoundPhones(
								"yo" + OOVSeg[j - 1]).replace(" ", "");
						for (int k = 0; k < IVttsCode.size(); k++) {
							int tmp = 0;
							if ((tmp = ED(IVttsCode.get(k).replace(" ", ""),
									OOVttsCode)) < edTmp)
								edTmp = tmp;
							if ((tmp = LCS(IVttsCode.get(k).replace(" ", ""),
									OOVttsCode)) > lcsTmp)
								lcsTmp = tmp;
						}
					}
					LCSCount += lcsTmp;
					EDCount += edTmp;
				}
				oneWord.add(legalWord + "@" + LCSCount + "@" + EDCount);
			}
			candidateWords.add(oneWord);
		}
		return candidateWords;
	}

	public static void main(String args[]) throws IOException {
		SyllablesCal sc = new SyllablesCal();
		Segmentor segmentor = new Segmentor();
		String word = "smh";
		ArrayList<Segmentation> segmentType = segmentor.syllableSegment(word);
		sc.syllableWeights(segmentType);

		// ArrayList<ArrayList<ArrayList<String>>> candidates =
		// sc.alterSyllables;
		// System.out.println(candidates.size());
		// System.out.println(segmentType.size());
		// for (int i = 0; i < candidates.size(); i++) {
		// String segmentStrings[] = segmentType.get(i).seg.split("&");
		// System.out.println(segmentType.get(i).seg + ":"
		// + segmentType.get(i).prob);
		// System.out.println("=====================");
		// ArrayList<ArrayList<String>> oneSegment = candidates.get(i);
		// for (int j = 0; j < oneSegment.size(); j++) {
		// System.out.println("------------------");
		// System.out.println(segmentStrings[j]);
		// ArrayList<String> alter = oneSegment.get(j);
		// for (String seg : alter) {
		// System.out.println(seg);
		// }
		// }
		// }
		// }

		ArrayList<ArrayList<String>> candidateWords = sc.candidateWords();
		System.out.println(candidateWords.size());
		System.out.println(segmentType.size());
		int num = 0;
		for (int i = 0; i < candidateWords.size(); i++) {
			System.out.println("============");
			System.out.println(segmentType.get(i).seg);
			ArrayList<String> oneWord = candidateWords.get(i);
			System.out.println(oneWord.size());
			for (String aword : oneWord)
				System.out.println(aword);
			num++;
		}
		System.out.println(num);
		System.out.println(sc.extractCV("M"));
		System.out.println(sc.extractCV("M AA R"));
		System.out.println(sc.checkSim("M", "M AA R"));
		System.out.println(sc.ED("foto", "food"));
		System.out.println(sc.ED("foto", "photo"));
	}
}
