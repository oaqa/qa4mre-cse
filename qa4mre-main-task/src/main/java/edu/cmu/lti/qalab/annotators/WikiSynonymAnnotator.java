package edu.cmu.lti.qalab.annotators;

import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import de.linguatools.disco.DISCO;
import de.linguatools.disco.ReturnDataCol;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.Synonym;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class WikiSynonymAnnotator extends JCasAnnotator_ImplBase {

	DISCO disco = null;
	int MAX_SYNONYMS = 10;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		String discoDir = (String) context
				.getConfigParameterValue("DISCO_INDEX_DIR");
		MAX_SYNONYMS = (Integer) context
				.getConfigParameterValue("MAX_SYNONYMS");
		try {
			disco = new DISCO(discoDir, false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		TestDocument testDoc = Utils.getTestDocumentFromCAS(jCas);

		if (testDoc.getTopicId().equals("1")
				|| testDoc.getTopicId().equals("4")) {//1.AIDS 4. Alzheimers
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
				LinkedList<String> synPhrases = getSynonym(phrase.getText());
				ArrayList<Synonym> synList = new ArrayList<Synonym>();
				for (int k = 0; k < synPhrases.size(); k++) {
					String syn = synPhrases.get(k);
					Synonym synonym = new Synonym(jCas);
					synonym.setText(syn);
					synonym.setWeight(1.0);
					synonym.setSource("gigaword-wiki");
					synonym.addToIndexes();
					synList.add(synonym);
				}
				FSList fsSynList = Utils.fromCollectionToFSList(jCas, synList);
				phrase.setSynonyms(fsSynList);
				phraseList.set(j, phrase);
			}

			for (int j = 0; j < nerList.size(); j++) {
				NER ner = nerList.get(j);
				LinkedList<String> synNers = getSynonym(ner.getText());
				ArrayList<Synonym> synList = new ArrayList<Synonym>();
				for (int k = 0; k < synNers.size(); k++) {
					String syn = synNers.get(k);
					Synonym synonym = new Synonym(jCas);
					synonym.setText(syn);
					synonym.setWeight(1.0);
					synonym.setSource("gigaword-wiki");
					synonym.addToIndexes();
					synList.add(synonym);
				}
				FSList fsSynList = Utils.fromCollectionToFSList(jCas, synList);
				ner.setSynonyms(fsSynList);
				nerList.set(j, ner);

			}
			FSList fsPhraseList = Utils
					.fromCollectionToFSList(jCas, phraseList);
			FSList fsNERList = Utils.createNERList(jCas, nerList);
			sentence.setPhraseList(fsPhraseList);
			sentence.setNerList(fsNERList);
			sentence.addToIndexes();

		}

	}

	public LinkedList<String> getSynonym(String word) {

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
