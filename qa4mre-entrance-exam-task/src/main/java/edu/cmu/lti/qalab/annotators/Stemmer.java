package edu.cmu.lti.qalab.annotators;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import de.linguatools.disco.DISCO;

public class Stemmer {
	private Set<String> words = null;
	private HashMap<String, String> verbs = null;
	DISCO disco = null;
	
	public Stemmer() {
		words = new HashSet<String>();
		verbs = new HashMap<String, String>();
		try {
			FileInputStream fstream = new FileInputStream(
					"data/dictionary/words");
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
		try {
			FileInputStream fstream = new FileInputStream(
					"data/dictionary/verbs.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String [] pair = strLine.split("\t");
				verbs.put(pair[0], pair[1]);
			}
			in.close();
		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
		}
		try {
			disco = new DISCO("data/similarityModel", false);
		} catch (IOException e) {
			System.out.println("LoadError");
			e.printStackTrace();
		}
	}

	public String stemWord(String input) throws IOException {
		if (verbs.containsKey(input)){
			return verbs.get(input);
		}
		TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_30,
				new StringReader(input));
		tokenStream = new PorterStemFilter(tokenStream);
		StringBuilder sb = new StringBuilder();
		CharTermAttribute termAttr = tokenStream
				.getAttribute(CharTermAttribute.class);
		while (tokenStream.incrementToken()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(termAttr);
		}
		return sb.toString();
	}

	public String stem(String word) throws IOException {
		String newWord = stemWord(word);
		if (words.contains(newWord)) {
			if (disco.firstOrderSimilarity(newWord, word)>=0.1){
				return newWord;
			}
			else{
				if (words.contains(word)){
					return word;
				}
				else{
					return newWord;
				}
			}
			//return newWord;
		} else {
			return word;
		}
	}

	public static void main(String args[]) {
		Stemmer stemmer = new Stemmer();
		try {
			System.out.println(stemmer.stem("\"and"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(stemmer.words.contains("laugh?"));
	}
}
