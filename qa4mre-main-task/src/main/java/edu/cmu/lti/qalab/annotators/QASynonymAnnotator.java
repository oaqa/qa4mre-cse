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

import de.linguatools.disco.DISCO;
import de.linguatools.disco.ReturnDataCol;

import abner.Tagger;
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Synonym;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class QASynonymAnnotator extends JCasAnnotator_ImplBase {

	public static String FILE_NAME = "data/gene_ontology_ext.obo";
	public static String GIGA_WORD = "data/cmudict.0.7a.gigaword.freq";
	private static HashMap<String, LinkedList<String>> dict = new HashMap<String, LinkedList<String>>();
	private static HashMap<String, Integer> gigaMap = new HashMap<String, Integer>();
	public static final int gigaThreshold = 400; // we treat words that have
	// counts more than 5000 as
	// common words.

	String DISCO_INDEX_DIR="";
	int MAX_SYNONYMS=3;
	DISCO disco=null;
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		super.initialize(context);
		FILE_NAME=(String)context.getConfigParameterValue("FILE_NAME");
		GIGA_WORD=(String)context.getConfigParameterValue("GIGA_WORD");
		DISCO_INDEX_DIR = (String) context
				.getConfigParameterValue("DISCO_INDEX_DIR");
		MAX_SYNONYMS = (Integer) context
				.getConfigParameterValue("MAX_SYNONYMS");

		try{
			startup();
			disco = new DISCO(DISCO_INDEX_DIR, false);

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		
		TestDocument testDoc=Utils.getTestDocumentFromCAS(jCas);
		if(testDoc.getTopicId().equals("2") || testDoc.getTopicId().equals("3")){//2. Climate change, 3. Music and society
			return;
		}
		
		ArrayList<Question> questionList = Utils
				.getQuestionListFromTestDocCAS(jCas);
		ArrayList<ArrayList<Answer>> answerList = Utils
				.getAnswerListFromTestDocCAS(jCas);
		
		for (int i = 0; i < questionList.size(); i++) {
			Question question = questionList.get(i);
			ArrayList<NounPhrase>qPhraseList=Utils.fromFSListToCollection(question.getNounList(),NounPhrase.class);
			ArrayList<NER> qNerList = Utils.fromFSListToCollection(question.getNerList(), NER.class);
			
			for(int j=0;j<qPhraseList.size();j++){
				
				NounPhrase phrase = qPhraseList.get(j);
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
				qPhraseList.set(j, phrase);

			}
			for(int j=0;j<qNerList.size();j++){
				NER ner = qNerList.get(j);
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
				qNerList.set(j, ner);
			
			}
			FSList fsPhraseList = Utils.fromCollectionToFSList(jCas, qPhraseList);
			FSList fsNERList = Utils.fromCollectionToFSList(jCas, qNerList);
			question.setNounList(fsPhraseList);
			question.setNerList(fsNERList);
			question.addToIndexes();
			questionList.set(i, question);
		}

		for (int i = 0; i < answerList.size(); i++) {
			ArrayList<Answer> choiceList = answerList.get(i);

			for (int c = 0; c < choiceList.size(); c++) {
				Answer answer = choiceList.get(c);
				
				ArrayList<NounPhrase>aPhraseList=Utils.fromFSListToCollection(answer.getNounPhraseList(),NounPhrase.class);
				ArrayList<NER> aNerList = Utils.fromFSListToCollection(answer.getNerList(), NER.class);
				
				for(int j=0;j<aPhraseList.size();j++){
					
					NounPhrase phrase = aPhraseList.get(j);
					LinkedList<String>synPhrases=getSynonym(phrase.getText());
					LinkedList<String>synPhrase1=getSynonymPubmed(phrase.getText());
					if(synPhrase1.size()>0){
						synPhrase1.addAll(synPhrase1);
					}
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
					aPhraseList.set(j, phrase);

				}
				for(int j=0;j<aNerList.size();j++){
					NER ner = aNerList.get(j);
					LinkedList<String>synNers=getSynonym(ner.getText());
					LinkedList<String>synNers1=getSynonymPubmed(ner.getText());
					if(synNers1.size()>0){
						synNers.addAll(synNers1);
					}
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
					aNerList.set(j, ner);
				
				}
				FSList fsPhraseList = Utils.fromCollectionToFSList(jCas, aPhraseList);
				FSList fsNERList = Utils.fromCollectionToFSList(jCas, aNerList);
				answer.setNounPhraseList(fsPhraseList);
				answer.setNerList(fsNERList);
				answer.addToIndexes();
				choiceList.set(c, answer);
			}
			answerList.set(i, choiceList);
		}
		ArrayList<QuestionAnswerSet>qaSetList=new ArrayList<QuestionAnswerSet>();

		for(int i=0;i<questionList.size();i++){
			QuestionAnswerSet qaSet=new QuestionAnswerSet(jCas);
			qaSet.setQuestion(questionList.get(i));
			FSList fsQASet=Utils.fromCollectionToFSList(jCas,answerList.get(i));
			qaSet.setAnswerList(fsQASet);
			qaSetList.add(qaSet);
		}
		FSList fsQAList=Utils.fromCollectionToFSList(jCas, qaSetList);
		testDoc.setQaList(fsQAList);
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
				String str = thisLine.trim();//.replaceAll("(\\r|\\n)", "");
				String[] wordNumber = str.split("[ ]{2,}");
				String word = wordNumber[0].toLowerCase();
				int cnt = Integer.parseInt(wordNumber[1]);
				gigaMap.put(word, cnt);
			}
			br.close();
			br=null;
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
	public LinkedList<String> getSynonymPubmed(String word) {

		LinkedList<String> synonymList = new LinkedList<String>();

		try {
			int freq = disco.frequency(word);
			// and print it to stdout
			System.out.println("Frequency of " + word + " is " + freq);

			// end if the word wasn't found in the index
			if (freq == 0) {
				return synonymList;
			}

			// retrieve the collocations for the input word
			ReturnDataCol[] collocationResult = disco.collocations(word);
			// and print the first 20 to stdout
			System.out.println("Collocations:");
			for (int i = 1; i < collocationResult.length; i++) {
				System.out.println("\t" + collocationResult[i].word + "\t"
						+ collocationResult[i].value);
				if (i >= MAX_SYNONYMS)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return synonymList;
	}

}
