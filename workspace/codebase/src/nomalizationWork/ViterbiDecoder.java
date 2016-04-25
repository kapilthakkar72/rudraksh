package nomalizationWork;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


import languageModel.ReadLmProb;
import languageModel.ReadSyllablesDicLm;

import syllablesTrans.LetterToSoundPhones;
import syllablesTrans.SyllablesCal;

import crf.Segmentation;
import crf.Segmentor;

public class ViterbiDecoder {
	public class CandidateInfo {
		String candidate;
		double score;// segmentation prob
		int ttsED;

		public CandidateInfo(String _candidate, double _score) {
			candidate = _candidate;
			score = _score;
		}

		public CandidateInfo(String _candidate, double _score, int _ttsED) {
			candidate = _candidate;
			score = _score;
			ttsED = _ttsED;
		}
	}

	public Segmentor segmentor;
	public SyllablesCal sc;
	public ArrayList<ArrayList<CandidateInfo>> candidates = new ArrayList<>();
	public String sentence;
	public String label;
	public ReadLmProb lmProb;
	public ArrayList<String> sentenceList = new ArrayList<>();
	public HashMap<String, String> traditionTrans = new HashMap<>(); // few fullforms
	public ReadSyllablesDicLm rs = new ReadSyllablesDicLm(); // word segmentation
	public HashMap<String, String> conventionTrans = new HashMap<>();// few fullforms
	HashSet<String> ivDic = new HashSet<>();  // normal dictionary
	public LetterToSoundPhones lts;

	public void generateDic() throws IOException {
		String infile = "./data/my.dict";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(infile), "utf-8"));
		String line = "";
		while ((line = br.readLine()) != null) {
			ivDic.add(line);
		}
		br.close();
	}

	public void getTraditionalTrans() throws IOException {
		String infile = "./data/traditionalTrans.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(infile), "utf-8"));
		String line = "";
		while ((line = br.readLine()) != null) {
			String words[] = line.split(",");
			traditionTrans.put(words[0], words[1]);
		}
		br.close();
		// The tradictional and convention should be merged together. But they just gain same results so I left it alone
		// But noticable, tradictional file are more likely to be the words that dictionary.com regard it as IV but were labeled as OOV
		infile = "./data/convention.txt";
		br = new BufferedReader(new InputStreamReader(new FileInputStream(
				infile), "utf-8"));
		line = "";
		while ((line = br.readLine()) != null) {
			String words[] = line.split(",");
			conventionTrans.put(words[0], words[1]);
		}
		br.close();
	}

	public ViterbiDecoder() throws IOException {
		// This is default constructor
		segmentor = new Segmentor();
		// Loads library, which can convert word to sound, sullable's sound and basic trie generator
		sc = new SyllablesCal();		
		lmProb = new ReadLmProb();
		// reads n-gram model from file
		lmProb.loadWord();
		// Loads segmentations of a word from dictionary file
		rs.load();
		// Some 14 manually annotated full forms
		getTraditionalTrans();
		// loaded normal dictionary
		generateDic();
		// letter to sound phones using some library
		lts = new LetterToSoundPhones();
	}

	public void setSentence(String _sentence, String _label) {
		sentence = _sentence;
		label = _label;
		candidates.clear();
		sentenceList.clear();
	}

	public double weightWord = 0.7; //relative importance of word and sound
	public double weightSound = 0.3;
	public double weightSeg = 0.0;
	public double weightLM = 1.0;

	public void setAttr(double _weightWord, double _weightSound,
			double _weightSeg, double _weightLM) {
		weightWord = _weightWord;
		weightSound = _weightSound;
		weightSeg = _weightSeg;
		weightLM = _weightLM;
	}

	public HashSet<String> formatExcep(String word) {
		HashSet<String> formList = new HashSet<>();
		if (word.equals("c"))
			formList.add("see");
		if (word.equals("u"))
			formList.add("you");
		if (word.equals("r"))
			formList.add("are");
		if (word.endsWith("x")&&(word.length()>=2)){
			String tword=new String(word.getBytes(), 0, word.length() - 1)+"ks";
			formList.add(tword);
		}
		// numbers:have only 1 number in a word
		String tmp = "";
		if ((tmp = word.replaceAll("[0-9]", "0")).contains("0")) {
			if (tmp.lastIndexOf('0') == tmp.indexOf('0')) {
				String tword = word.replace("2", "to").replace("4", "four")
						.replace("0", "o").replace("1", "one");
				formList.add(convert2PlusChar(tword, 1));
				formList.add(convert2PlusChar(tword, 2));
				if (word.length() >= 2) {
					tword=word.replace("4", "a");
					
					formList.add(convert2PlusChar(tword, 1));
					formList.add(convert2PlusChar(tword, 2));
				}
				return formList;
			}
		}
		formList.add(convert2PlusChar(word, 1));
		formList.add(convert2PlusChar(word, 2));
		return formList;
	}

	public ArrayList<CandidateInfo> reduce2Char(String word) {
		ArrayList<CandidateInfo> _tmpCan = new ArrayList<>();
		if (word.contains("q") && ivDic.contains(word.replace("q", "g"))) {
			_tmpCan.add(new CandidateInfo(word.replace("q", "g"), 1.0));
		}
		if (convert2PlusChar(word, 1).equals(word))
			return _tmpCan;
		// looooove => love, goooooood => good
		if (ivDic.contains(convert2PlusChar(word, 1)))
			_tmpCan.add(new CandidateInfo(convert2PlusChar(word, 1), 1.0));
		if (ivDic.contains(convert2PlusChar(word, 2)))
			_tmpCan.add(new CandidateInfo(convert2PlusChar(word, 2), 1.0));
		return _tmpCan;
	}

	private String convert2PlusChar(String str, int n) {
		if (str.equals(""))
			return str;
		String tmp = str.charAt(0) + "";
		int repetCount = 1;

		for (int i = 1; i < str.length(); i++) {
			if (str.charAt(i) == str.charAt(i - 1))
				repetCount++;
			else
				repetCount = 1;
			if (repetCount > n) {
				continue;
			}
			tmp += str.charAt(i) + "";
		}
		return tmp;
	}

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
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));
		return distance[str1.length()][str2.length()];
	}

	private int LCS(String x, String y) {

		int i, j;
		int lenx = x.length();
		int leny = y.length();
		int[][] table = new int[lenx + 1][leny + 1];

		for (i = 0; i <= lenx; i++)
			table[i][0] = 0;
		for (i = 0; i <= leny; i++)
			table[0][i] = 0;
		
		for (i = 1; i <= lenx; i++) {
			for (j = 1; j <= leny; j++) {
				if (x.charAt(i - 1) == y.charAt(j - 1))
					table[i][j] = 1 + table[i - 1][j - 1];
				else
					table[i][j] = Math.max(table[i][j - 1], table[i - 1][j]);
			}
		}
		return table[lenx][leny];
	}

	public ArrayList<CandidateInfo> extraCandidate(String word) {
		ArrayList<CandidateInfo> extra = new ArrayList<>();
		for (String w : rs.word2Syllables.keySet()) {
			if (ED(w, word) <= 3)
				extra.add(new CandidateInfo(w, 1 * LCS(w, word) - 1
						* ED(w.replace("'", ""), word)));
		}
		if (extra.size() == 0)
			extra.add(new CandidateInfo(word, 1.0));
		return extra;
	}

	public void candidateGeneration() throws NumberFormatException, IOException {
		String words[] = sentence.split(" ");
		String labels[] = label.split(" ");
		ArrayList<CandidateInfo> tmp = new ArrayList<>();
		tmp.add(new CandidateInfo("<s>", 1.0));
		sentenceList.add("<s>");
		candidates.add(tmp);
		for (int i = 0; i < words.length; i++) {

			if (labels[i].equals("IV") || labels[i].equals("NO")
					|| words[i].contains("haha")) {
				sentenceList.add(words[i]);
				ArrayList<CandidateInfo> oneCandi = new ArrayList<>();
				oneCandi.add(new CandidateInfo(words[i].replaceAll(
						"haha(h|a)*", "haha"), 1.0));
				candidates.add(oneCandi);
			} else if (traditionTrans.containsKey(words[i])) {
				sentenceList.add(words[i]);
				ArrayList<CandidateInfo> oneCandi = new ArrayList<>();
				oneCandi.add(new CandidateInfo(traditionTrans.get(words[i]),
						1.0));
				candidates.add(oneCandi);
			} else if (reduce2Char(words[i]).size() != 0) {
				candidates.add(reduce2Char(words[i]));
				sentenceList.add(words[i]);
			} else {
				// System.out.println("!!!" + words[i]);
				ArrayList<CandidateInfo> multiCandi = new ArrayList<>();
				// System.out.println("####"+multiCandi.size());
				// Replaces c-see, 2 - to, etc.
				HashSet<String> informalList = formatExcep(words[i]);
				for (String informal : informalList) {
					sentenceList.add(informal);
					// segments words, also gives prob of segmentation
					ArrayList<Segmentation> segmentType = segmentor
							.syllableSegment(informal);
					sc.syllableWeights(segmentType);
					ArrayList<ArrayList<String>> alterWords = sc
							.candidateWords();
					// System.out.println(segmentType.size()+"^^^"+alterWords.size());
					for (int j = 0; j < segmentType.size(); j++) {
						ArrayList<String> oneWord = alterWords.get(j);
						double sum_exp = 0.0;
						ArrayList<CandidateInfo> oneWordCache = new ArrayList<>();
						for (int k = 0; k < oneWord.size(); k++) {
							String parts[] = oneWord.get(k).split("@");
							String _word = parts[0].replace("&", "");
							int soundLCS = Integer.parseInt(parts[1]);
							int soundED = Integer.parseInt(parts[2]);
							double logProb = weightWord
									* (LCS(_word, informal) - ED(
											_word.replace("'", ""), informal))
									+ weightSound * (soundLCS - soundED);
							sum_exp += Math.pow(10, logProb);
							oneWordCache.add(new CandidateInfo(parts[0],
									logProb));
						}
						sum_exp = Math.log10(sum_exp);
						for (int k = 0; k < oneWord.size(); k++) {
							double emissionProb = weightSeg
									* Math.log10(segmentType.get(j).prob)
									+ oneWordCache.get(k).score;
							// System.out.println(oneWordCache.get(k).candidate+" "+oneWordCache.get(k).score+" "+sum_exp);
							multiCandi.add(new CandidateInfo(oneWordCache
									.get(k).candidate, emissionProb));
						}
					}
				}
				if (multiCandi.size() == 0)
					multiCandi = extraCandidate(words[i]);// if no alters
				candidates.add(multiCandi);
			}
		}
		tmp = new ArrayList<>();
		tmp.add(new CandidateInfo("</s>", 1.0));
		candidates.add(tmp);
		sentenceList.add("</s>");
	}
	// TODO:: We changed maxN to 1000 from 10000 for running purpose
	public final int maxN = 10000;
	public final int Verca = 3069580; // 1.0/Verca: smooth for bi-gram model
	public final int Ve = 1126030; // 1.0/Ve: smooth for uni-gram model

	public String viterbiWork() throws NumberFormatException, IOException {
		candidateGeneration();
		double V[][] = new double[maxN][maxN];
		for (int i = 0; i < maxN; i++)
			for (int j = 0; j < maxN; j++)
				V[i][j] = -400;
		V[0][0] = 1.0;
		int path[][] = new int[maxN][maxN];
		int timeNumber = candidates.size();
		// System.out.println("****"+timeNumber);
		for (int i = 1; i < timeNumber; i++) {
			ArrayList<CandidateInfo> currentState = candidates.get(i);
			ArrayList<CandidateInfo> preState = candidates.get(i - 1);
			String originWord = sentenceList.get(i);
			for (int j = 0; j < currentState.size(); j++)
				for (int k = 0; k < preState.size(); k++) {
					//pre-processing
					String currentWord = currentState.get(j).candidate.replace(
							"&", "");
					String preWord = preState.get(k).candidate.replace("&", "");
					if (preWord.startsWith("@"))
						preWord = "<user>";
					if (preWord.startsWith("#"))
						preWord = "<topic>";
					if (preWord.replaceAll("[^A-Za-z0-9\\+-]", "").length() == 0)
						preWord = "<punct>";
					if (currentWord.startsWith("@"))
						currentWord = "<user>";
					if (currentWord.startsWith("#"))
						currentWord = "<topic>";
					if (currentWord.replaceAll("[^A-Za-z0-9\\+-]", "").length() == 0)
						currentWord = "<punct>";
					
					double stateTransProb;
					if (lmProb.lmProb.containsKey(preWord + " " + currentWord)) {
						stateTransProb = lmProb.lmProb.get(preWord + " "
								+ currentWord);
					} else
						stateTransProb = Math.log10(1.0 / Verca);
					double emissionProb1 = currentState.get(j).score;
					double emissionProb2;
					if (lmProb.lmProb.containsKey(currentWord)) {
						emissionProb2 = lmProb.lmProb.get(currentWord);
					} else
						emissionProb2 = Math.log10(1.0 / Ve);
					double tmp = V[i - 1][k] + weightLM * stateTransProb
							+ emissionProb1 + emissionProb2;
							
					// first order HMM
					// double tmp = V[i - 1][k] + weightLM * stateTransProb
					// + emissionProb1;
					if (tmp > V[i][j]) {
						V[i][j] = tmp;
						path[i][j] = k;
					}
				}
		}

		double maxFinalProb = 0.0;
		// System.out.println(timeNumber);
		int Q[] = new int[timeNumber];
		Q[timeNumber - 1] = 0;
		int lastStateNumber = candidates.get(timeNumber - 1).size();
		for (int i = 0; i < lastStateNumber; i++) {
			if (V[timeNumber][i] > maxFinalProb) {
				maxFinalProb = V[timeNumber][i];
				Q[timeNumber - 1] = i;
			}
		}
		for (int i = timeNumber - 2; i >= 0; i--) {
			Q[i] = path[i + 1][Q[i + 1]];
		}


		String answer = "";
		for (int i = 0; i < timeNumber; i++) {
			ArrayList<CandidateInfo> currentState = candidates.get(i);
			String word = currentState.get(Q[i]).candidate;
			if (word.equals("<s>") || word.equals("</s>"))
				continue;
			if (conventionTrans.containsKey(word.replace("&", "")))
				word = conventionTrans.get(word.replace("&", ""));
			answer += " " + word.replace("&", "");
		}
		// System.out.println(answer+"\n");
		return answer.trim();
	}

	public static void main(String args[]) throws IOException,
			NumberFormatException {
		ViterbiDecoder vd = new ViterbiDecoder();
		String sentence = "sux fux";
		String label = "OOV OOV";
		vd.setSentence(sentence, label);
		System.out.println("!!");
		System.out.println(vd.viterbiWork());
	}

}
