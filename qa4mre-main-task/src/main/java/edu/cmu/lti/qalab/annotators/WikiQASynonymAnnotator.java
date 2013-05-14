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
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Synonym;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class WikiQASynonymAnnotator extends JCasAnnotator_ImplBase {

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

		if(testDoc.getTopicId().equals("1") || testDoc.getTopicId().equals("4")){//1. AIDS, 4. Alzheimers
			return;
		}
		
		ArrayList<Question> questionList = Utils
				.getQuestionListFromTestDocCAS(jCas);
		ArrayList<ArrayList<Answer>> answerList = Utils
				.getAnswerListFromTestDocCAS(jCas);

		for (int i = 0; i < questionList.size(); i++) {
			Question question = questionList.get(i);
			ArrayList<NounPhrase> qPhraseList = Utils.fromFSListToCollection(
					question.getNounList(), NounPhrase.class);
			ArrayList<NER> qNerList = Utils.fromFSListToCollection(
					question.getNerList(), NER.class);

			for (int j = 0; j < qPhraseList.size(); j++) {

				NounPhrase phrase = qPhraseList.get(j);
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
				qPhraseList.set(j, phrase);

			}
			for (int j = 0; j < qNerList.size(); j++) {
				NER ner = qNerList.get(j);
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
				qNerList.set(j, ner);

			}
			FSList fsPhraseList = Utils.fromCollectionToFSList(jCas,
					qPhraseList);
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

				ArrayList<NounPhrase> aPhraseList = Utils
						.fromFSListToCollection(answer.getNounPhraseList(),
								NounPhrase.class);
				ArrayList<NER> aNerList = Utils.fromFSListToCollection(
						answer.getNerList(), NER.class);

				for (int j = 0; j < aPhraseList.size(); j++) {

					NounPhrase phrase = aPhraseList.get(j);
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
					FSList fsSynList = Utils.fromCollectionToFSList(jCas,
							synList);
					phrase.setSynonyms(fsSynList);
					aPhraseList.set(j, phrase);

				}
				for (int j = 0; j < aNerList.size(); j++) {
					NER ner = aNerList.get(j);
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
					FSList fsSynList = Utils.fromCollectionToFSList(jCas,
							synList);
					ner.setSynonyms(fsSynList);
					aNerList.set(j, ner);

				}
				FSList fsPhraseList = Utils.fromCollectionToFSList(jCas,
						aPhraseList);
				FSList fsNERList = Utils.fromCollectionToFSList(jCas, aNerList);
				answer.setNounPhraseList(fsPhraseList);
				answer.setNerList(fsNERList);
				answer.addToIndexes();
				choiceList.set(c, answer);
			}
			answerList.set(i, choiceList);
		}
		ArrayList<QuestionAnswerSet> qaSetList = new ArrayList<QuestionAnswerSet>();

		for (int i = 0; i < questionList.size(); i++) {
			QuestionAnswerSet qaSet = new QuestionAnswerSet(jCas);
			qaSet.setQuestion(questionList.get(i));
			FSList fsQASet = Utils.fromCollectionToFSList(jCas,
					answerList.get(i));
			qaSet.setAnswerList(fsQASet);
			qaSetList.add(qaSet);
		}
		FSList fsQAList = Utils.fromCollectionToFSList(jCas, qaSetList);
		testDoc.setQaList(fsQAList);
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
				
				word=word.replace("~", "").replace("(","").replace(")", "").replace("?","").replace("*", "").trim();
				synonymList.add(word);
				if (i >= MAX_SYNONYMS)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return synonymList;
	}
}
