package syllablesTrans;

import java.io.*;
import java.net.URL;
import com.sun.speech.freetts.lexicon.LetterToSound;
import com.sun.speech.freetts.lexicon.LetterToSoundImpl;

public class LetterToSoundPhones {
	LetterToSound lts = null;

	public LetterToSoundPhones() {
		try {
			File file = new File("./lib/cmulex_lts.bin");
			lts = new LetterToSoundImpl(file.toURI().toURL(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getLetterToSoundPhones(String word) {
		String[] ltspM_phone_array;
		String phoneCode = "";
		ltspM_phone_array = lts.getPhones(word, null);

		//System.out.print("Word: " + word.toUpperCase() + " Phones: ");
		for (int i = 0; i < ltspM_phone_array.length; i++) {
			phoneCode += ltspM_phone_array[i].toString().replace("ax", "ah")
					.replaceAll("[1-9]", "").toUpperCase()+" ";
		}
		return phoneCode.trim();
	}

	public static void main(String[] args) {
		LetterToSoundPhones ltspM = new LetterToSoundPhones();

		System.out.println(ltspM.getLetterToSoundPhones("people"));
		System.out.println(ltspM.getLetterToSoundPhones("barber doing"));
		System.out.println(ltspM.getLetterToSoundPhones("between"));
		System.out.println(ltspM.getLetterToSoundPhones("the"));
	}
}