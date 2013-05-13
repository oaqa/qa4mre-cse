package edu.cmu.lti.qalab.annotators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.Synonym;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class SynonymAnnotator extends JCasAnnotator_ImplBase {

	public static String FILE_NAME = "data/gene_ontology_ext.obo";
	public static String GIGA_WORD = "data/cmudict.0.7a.gigaword.freq";
	private static HashMap<String, LinkedList<String>> dict = new HashMap<String, LinkedList<String>>();
	private static HashMap<String, Integer> gigaMap = new HashMap<String, Integer>();
	public static final int gigaThreshold = 400; // we treat words that have
	// counts more than 5000 as
	// common words.

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		FILE_NAME=(String)context.getConfigParameterValue("FILE_NAME");
		GIGA_WORD=(String)context.getConfigParameterValue("GIGA_WORD");
				
		try{
			startup();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		TestDocument testDoc=Utils.getTestDocumentFromCAS(jCas);

		if(testDoc.getTopicId().equals("2") || testDoc.getTopicId().equals("3")){//2. Climate change, 3. music and society
			return;
		}
		ArrayList<Sentence> sentList = Utils
				.getSentenceListFromTestDocCAS(jCas);
		for (int i = 0; i < sentList.size(); i++) {
			Sentence sentence = sentList.get(i);

			ArrayList<NounPhrase> phraseList = Utils.fromFSListToCollection(
					sentence.getPhraseList(), NounPhrase.class);
			ArrayList<NER> nerList = Utils.fromFSListToCollection(
					sentence.getNerList(), NER.class);
			for (int j = 0; j < phraseList.size(); j++) {
				NounPhrase phrase = phraseList.get(j);
				LinkedList<String>synPhrases=getSynonym(phrase.getText());
				ArrayList<Synonym>synList=new ArrayList<Synonym>();
				for(int k=0;k<synPhrases.size();k++){
					String syn=synPhrases.get(k);
					Synonym synonym=new Synonym(jCas);
					synonym.setText(syn);
					synonym.setWeight(1.0);
					synonym.setSource("gigaword-pubmed");
					synonym.addToIndexes();
					synList.add(synonym);					
				}
				FSList fsSynList=Utils.fromCollectionToFSList(jCas, synList);
				phrase.setSynonyms(fsSynList);
				phraseList.set(j, phrase);
			}

			for (int j = 0; j < nerList.size(); j++) {
				NER ner = nerList.get(j);
				LinkedList<String>synNers=getSynonym(ner.getText());
				ArrayList<Synonym>synList=new ArrayList<Synonym>();
				for(int k=0;k<synNers.size();k++){
					String syn=synNers.get(k);
					Synonym synonym=new Synonym(jCas);
					synonym.setText(syn);
					synonym.setWeight(1.0);
					synonym.setSource("gigaword-pubmed");
					synonym.addToIndexes();
					synList.add(synonym);
				}
				FSList fsSynList=Utils.fromCollectionToFSList(jCas, synList);
				ner.setSynonyms(fsSynList);
				nerList.set(j, ner);
				
			}
			FSList fsPhraseList = Utils.fromCollectionToFSList(jCas, phraseList);			
			FSList fsNERList = Utils.createNERList(jCas, nerList);
			sentence.setPhraseList(fsPhraseList);
			sentence.setNerList(fsNERList);
			sentence.addToIndexes();

		}

	}

	

	/**
	 * Read the ontology file and build the dictionary
	 */
	public static void startup() {
		String thisLine;
		String currentName = "";
		boolean next = true;

		// DEBUG
		// System.out.println("entered here");

		// Loop across the arguments

		// Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(GIGA_WORD));
			while ((thisLine = br.readLine()) != null) {
				String str = thisLine.trim();//replaceAll("(\\r|\\n)", "");
				String[] wordNumber = str.split("[ ]{2,}");
				String word = wordNumber[0].toLowerCase();
				int cnt = Integer.parseInt(wordNumber[1]);
				gigaMap.put(word, cnt);
			}

			br = new BufferedReader(new FileReader(FILE_NAME));
			while ((thisLine = br.readLine()) != null) {
				String str = thisLine.trim();//replaceAll("(\\r|\\n)", "");
				if (str.equals("[Term]")) {
					next = true;
				}
				if (str.startsWith("name:")) {
					if (next) {
						currentName = str.replaceAll("name: ", "");
					}
				}
				if (str.startsWith("synonym:")) {
					if (str.contains("EXACT") || str.contains("NARROW")) {
						// synonym..
						Pattern pattern = Pattern
								.compile("synonym:.*\"(.*)\".*");
						Matcher matcher = pattern.matcher(str);
						// DEBUG
						// System.err.println(str);
						if (matcher.find()) {
							String syno = matcher.group(1);
							if (!currentName.equals("")) {
								// System.err.format("%s:%s.\n", currentName,
								// syno);

								add(currentName, syno);
								add(syno, currentName);
								recursiveAddSyno(currentName, syno);
							}
						}
					}
				}
			}
			br.close();
			br=null;

		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}

	private static void recursiveAddSyno(String currentName, String syno) {
		if (currentName.length() == 0 || syno.length() == 0) {
			return;
		}
		add(currentName, syno);
		add(syno, currentName);
		String[] currSplitted = currentName.split(" ");
		String[] synoSplitted = syno.split(" ");
		String currLast = currSplitted[currSplitted.length - 1];
		String synoLast = synoSplitted[synoSplitted.length - 1];
		// DEBUG
		// System.out.format("%s, %s, %d\n", currLast,
		// synoLast, gigaMap.get(currLast));
		if (currLast.equals(synoLast)) {
			// DEBUG
			// System.out.println("Entered here!");
			String anotherCurr = "";
			String anotherSyno = "";
			for (int i = 0; i < currSplitted.length - 1; i++) {
				if (i == currSplitted.length - 2) {
					anotherCurr += currSplitted[i];
				} else {
					anotherCurr += currSplitted[i] + " ";
				}

			}
			for (int i = 0; i < synoSplitted.length - 1; i++) {
				if (i == synoSplitted.length - 2) {
					anotherSyno += synoSplitted[i];
				} else {
					anotherSyno += synoSplitted[i] + " ";
				}
			}
			// DEBUG
			// System.out.println("!!!" + anotherSyno
			// + "," + anotherCurr);

			recursiveAddSyno(anotherCurr, anotherSyno);
			// add(anotherCurr, anotherSyno);
			// add(anotherSyno, anotherCurr);
		}
	}

	/**
	 * 
	 * @param target
	 * @param newSyno
	 */
	private static void add(String target, String newSyno) {
		if (!dict.containsKey(target)) {
			LinkedList<String> l = new LinkedList<String>();
			l.add(newSyno);
			dict.put(target, l);
		} else {
			LinkedList<String> l = dict.get(target);
			if (!l.contains(newSyno)) {
				l.add(newSyno);
			}
		}
	}

	/**
	 * returns a list of synonym of a given word.
	 * 
	 * @param word
	 * @return list of synonym
	 */
	public static LinkedList<String> getSynonym(String word) {
		LinkedList<String> res = new LinkedList<String>();
		if (dict.containsKey(word)) {
			res = dict.get(word);
		}
		return res;
	}

}
