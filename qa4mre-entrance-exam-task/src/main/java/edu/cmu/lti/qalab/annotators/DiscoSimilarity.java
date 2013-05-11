package edu.cmu.lti.qalab.annotators;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import de.linguatools.disco.DISCO;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class DiscoSimilarity {
	DISCO disco = null;
	StanfordCoreNLP pipeline = null;
	private Stemmer stemmer = null;
	private Set<String> words = null;

	DiscoSimilarity() {
		try {
			disco = new DISCO("data/similarityModel", false);
		} catch (IOException e) {
			System.out.println("LoadError");
			e.printStackTrace();
		}

		words = new HashSet<String>();
		try {
			FileInputStream fstream = new FileInputStream(
					"data/dictionary/dicWords");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				words.add(strLine);
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		stemmer = new Stemmer();
	}

	
	double getSimilarity(String sent1, String sent2) throws IOException{
		String [] t1 = sent1.split(" ");
		String [] t2 = sent2.split(" ");
		for (int i = 1; i < t1.length; i++) {
			t1[i] = stemmer.stem(t1[i]);
		}
		for (int i = 1; i < t2.length; i++) {
			t2[i] = stemmer.stem(t2[i]);
		}
		double score = 0.0;
		for (String w1:t1){
			for (String w2:t2){
				if (words.contains(w1) && words.contains(w2)) {
					score+=disco.secondOrderSimilarity(w1, w2);
				}
			}
		}
		return score;
	}

	public static void main(String args[]) throws IOException {
		DiscoSimilarity sim = new DiscoSimilarity();
		String str = " GOVERNOR SUNUNU AND I FELL ASLEEP AND SHE WENT UP AROUND THIRTY FIVE FLOORS BELOW THE EIGHT DEMOCRATS WHO RUN THIS IS JULIE MCCARTHY REPORTING";
		//double s = sim.getSelfMaxSimilarity(str.toLowerCase());
		//System.out.println(s);
	}
}
