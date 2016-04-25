package syllablesTrans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

import languageModel.ReadSyllablesDicLm;

public class CMULexSeg {
	ReadSyllablesDicLm rsd = new ReadSyllablesDicLm();
	LetterToSoundPhones ltsp;
	HashMap<String, String> word2Sound = new HashMap<>();
	HashMap<String, String> word2SoundSeg = new HashMap<>();

	public HashSet<String> sySet = new HashSet<>();

	public CMULexSeg() throws IOException {
		getSound();
		rsd.load();
		ltsp = new LetterToSoundPhones();
	}

	public void getSound() throws IOException {
		String infile = "./data/CMULex.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(infile), "utf-8"));
		String line = "";
		while ((line = br.readLine()) != null) {
			// System.out.println(line);
			String parts[] = line.split("  ");
			String word = parts[0].replaceAll("\\([0-9]\\)", "").toLowerCase();
			String sound = parts[1].replaceAll("[0-9]", "");
			word2Sound.put(word, sound);
		}
		br.close();
	}

	// Mapping formal word's syllables to their phonetic symbols using CMULex
	public void cutSyllables() {
		for (String _word : rsd.word2Syllables.keySet()) {
			if (word2Sound.containsKey(_word) == false)
				continue;
			String word = rsd.word2Syllables.get(_word);
			String[] sys = word.split(";");
			for (int i = 0; i < sys.length; i++)
				sySet.add(sys[i]);
			int pword = 0;
			int psound = 0;
			
			String sound[] = word2Sound.get(_word).split(" ");
			String soundSeg = sound[psound++];
			int tmp = psound;
			while (pword < word.length()) {
				if (word.charAt(pword) != ';') {
					pword++;
					continue;
				}
				char ch = word.charAt(pword - 1);
				char ch2 = word.charAt(pword + 1);
				// tmp=psound;
				boolean flag = false;
				while (psound < sound.length) {
					String sch = sound[psound].toLowerCase();
					//if (word.equals("peo;ple")) System.out.println(sch+"  "+ch+" "+psound);
					if (("aeou".indexOf(ch) == -1)
							&& ((sch.endsWith("" + ch))
									|| ((ch == 'c') && (sch.equals("k")))
									|| ((ch == 'x') && (sch.equals("z")))
									|| ((ch == 's') && (sch.equals("z")))
									|| ((ch == 'g') && (sch.startsWith("j")))
									|| ((ch == 'c') && (sch.equals("s")))
									|| ((ch == 'x') && (sch.equals("z")))
									|| ((ch == 'x') && (sch.equals("s")))
									|| ((ch == 's') && (sch.equals("z")))
									|| ((ch == 'i') && (sch.equals("iy")))
									|| ((ch == 'i') && (sch.equals("ay"))) || ((ch == 'i') && (sch
									.equals("ih"))))) {
						soundSeg += " " + sound[psound] + ";";
						psound++;
						tmp = psound;
						flag = true;
						break;
					} else if (("aeou".indexOf(ch2) == -1)
							&& ((sch.startsWith("" + ch2))
									|| ((ch2 == 'c') && (sch.equals("k")))
									|| ((ch2 == 'x') && (sch.equals("z")))
									|| ((ch2 == 'p') && (sch.equals("f")))
									|| ((ch2 == 't') && (sch.equals("d")))
									|| ((ch2 == 'q') && (sch.equals("k")))
									|| ((ch2 == 'x') && (sch.equals('s')))
									|| ((ch2 == 'c') && (sch.equals("s")))
									|| ((ch2 == 't') && (sch.startsWith("s")))
									|| ((ch2 == 'g') && (sch.startsWith("j")))
									|| ((ch2 == 's') && (sch.equals("z"))) || ((ch == 'i') && (sch
									.equals("ih"))))) {
						soundSeg += ";" + sound[psound++];
						flag = true;
						break;
					}
					soundSeg += " " + sound[psound++];
				}
				if (flag)
					pword++;
				else
					break;
			}
			while (psound < sound.length)
				soundSeg += " " + sound[psound++];
			int countWord = word.split(";").length;
			int countSound = soundSeg.split(";").length;
			if (countSound != countWord) {
				System.out.println("!!!" + word + "  "
						+ soundSeg + " " + " "
						+ countWord + " " + countSound);
				continue;
			}
			word2SoundSeg.put(word, soundSeg);
		}
	}

	public void soundofSys() throws IOException {
		String outfile = "./data/sound4Syllables.txt";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outfile), "utf-8"));

		HashMap<String, HashSet<String>> sound4Syllables = new HashMap<>();
		for (String word : word2SoundSeg.keySet()) {
			String sys[] = word.split(";");
			String sounds[] = word2SoundSeg.get(word).split(";");
			for (int i = 0; i < sys.length; i++) {
				HashSet<String> sounds1Sys = new HashSet<>();
				if (sound4Syllables.containsKey(sys[i]))
					sounds1Sys = sound4Syllables.get(sys[i]);
				sounds1Sys.add(sounds[i].trim());
				sound4Syllables.put(sys[i], sounds1Sys);
			}
		}

		for (String sy : sySet) {
			String sound = ltsp.getLetterToSoundPhones(sy).trim();
			HashSet<String> sounds1Sys = new HashSet<>();
			if (sound4Syllables.containsKey(sy)) sounds1Sys=sound4Syllables.get(sy);
			sounds1Sys.add(sound);
			sound4Syllables.put(sy, sounds1Sys);
		}

		for (String sy : sound4Syllables.keySet()) {
			HashSet<String> sounds = sound4Syllables.get(sy);
			for (String sound : sounds) {
				if (sound.equals("")) continue;
				bw.write(sy + "," + sound + "\n");
			}
		}
		bw.close();
	}

	public static void main(String args[]) throws IOException {
		String outfile = "./data/syllablesSeg.txt";

		CMULexSeg lseg = new CMULexSeg();
		lseg.cutSyllables();
		lseg.soundofSys();
	}
}
