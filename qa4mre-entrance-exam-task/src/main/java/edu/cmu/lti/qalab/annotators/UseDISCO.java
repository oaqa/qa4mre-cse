package edu.cmu.lti.qalab.annotators;

import de.linguatools.disco.DISCO;
import de.linguatools.disco.DISCO.ReturnDataCommonContext;
import de.linguatools.disco.ReturnDataBN;
import de.linguatools.disco.ReturnDataCol;
import java.io.IOException;
import java.util.ArrayList;

/*****************************************************************
 * sample code to show the use of the DISCO Java API version 1.4 *
 * 
 * (C) 2013 Peter Kolb peter.kolb@linguatools.org
 */
public class UseDISCO {

	/************************************************************************
	 * Call with: java UseDISCO <DISCO directory> <word> Make sure that
	 * disco-1.4.jar is in the classpath. Set the JVM heap size to be larger
	 * than the word space that will be loaded into RAM, otherwise an
	 * OutOfMemoryError will be thrown!
	 */
	public static void main(String[] args) throws IOException {

		if (args.length == 0) {
			args = new String[2];
			args[0] = new String("data/similarityModel/");
			args[1] = new String("energy");
		}

		// first command line argument is path to the DISCO word space directory
		String discoDir = args[0];
		// second argument is the input word
		String word = args[1];

		/****************************************
		 * create instance of class DISCO. * Do NOT load the word space into
		 * RAM. *
		 ****************************************/
		DISCO disco = new DISCO(discoDir, false);

		// retrieve the frequency of the input word
		int freq = disco.frequency(word);
		// and print it to stdout
		System.out.println("Frequency of " + word + " is " + freq);

		// end if the word wasn't found in the index
		if (freq == 0){
			return;
		}

		// retrieve the collocations for the input word
		ReturnDataCol[] collocationResult = disco.collocations(word);
		// and print the first 20 to stdout
		System.out.println("Collocations:");
		for (int i = 1; i < collocationResult.length; i++) {
			System.out.println("\t" + collocationResult[i].word + "\t"
					+ collocationResult[i].value);
			if (i >= 20)
				break;
		}
		System.out.println("common context:");
		System.out.println("~~~~"
				+ disco.secondOrderSimilarity("banana", "pear"));
		ArrayList<ReturnDataCommonContext> tmp = disco.commonContext("apple",
				"banana");
		for (int i = 1; i < tmp.size(); i++) {
			System.out.println("\t" + tmp.get(i).word + "\t"
					+ tmp.get(i).valueW1);
			if (i >= 20)
				break;
		}

		// retrieve the most similar words for the input word
		ReturnDataBN simResult = disco.similarWords(word);
		// and print the first 20 of them to stdout
		System.out.println("Most similar words:");
		for (int i = 1; i < simResult.words.length; i++) {
			System.out.println("\t" + simResult.words[i] + "\t"
					+ simResult.values[i]);
			if (i >= 20)
				break;
		}

		// compute second order similarity between the input word and its most
		// similar words
		System.out.println("Computing second order similarity between " + word
				+ " and all of its similar words...");
		long startTime = System.currentTimeMillis();
		for (int i = 1; i < simResult.words.length; i++) {
			// float s2 = disco.secondOrderSimilarity(word, simResult.words[i]);
		}
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("OK. Computation took " + elapsedTime + " ms.");

		/**********************************************
		 * Create another DISCO instance, * this time loading the word space
		 * into RAM. *
		 **********************************************/
		System.out.println("Trying to load word space into RAM...\n"
				+ "(in case of OutOfMemoryError: increase JVM "
				+ "heap space to size of word space directory!)");
		startTime = System.currentTimeMillis();
		DISCO discoRAM = new DISCO(discoDir, true);
		endTime = System.currentTimeMillis();
		long elapsedTimeLoad = endTime - startTime;
		System.out.println("OK (loading to RAM took " + elapsedTimeLoad
				+ " ms)");

		// compute second order similarity between the input word and its most
		// similar words in RAM
		System.out.println("Computing second order similarity between " + word
				+ " and all of its similar words in RAM...");
		startTime = System.currentTimeMillis();
		for (int i = 1; i < simResult.words.length; i++) {
			// float s2 = discoRAM.secondOrderSimilarity(word,
			// simResult.words[i]);
		}
		endTime = System.currentTimeMillis();
		long elapsedTimeRAM = endTime - startTime;
		System.out.println("OK. Computation took " + elapsedTimeRAM
				+ " ms in RAM.");
		if (elapsedTimeRAM >= elapsedTime) {
			System.out.println("Maybe your system had to swap to disk?");
		}

	}
}