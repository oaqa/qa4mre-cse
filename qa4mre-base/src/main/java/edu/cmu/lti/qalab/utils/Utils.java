package edu.cmu.lti.qalab.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.fit.util.JCasUtil;

import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.Dependency;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.SourceDocument;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Token;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;

public class Utils {
	/**
	 * Creates FeatureStructure List from sentenceList
	 * 
	 * @param <T>
	 * 
	 * @param aJCas
	 * @param aCollection
	 * @return FSList
	 */

	public static FSList createSentenceList(JCas aJCas,
			Collection<Sentence> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<Sentence> i = aCollection.iterator();
		while (i.hasNext()) {
			head.setHead(i.next());
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	/**
	 * Creates FeatureStructure List from questionList
	 * 
	 * @param <T>
	 * 
	 * @param aJCas
	 * @param aCollection
	 * @return FSList
	 */

	public static FSList createQuestionList(JCas aJCas,
			Collection<Question> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<Question> i = aCollection.iterator();
		while (i.hasNext()) {
			head.setHead(i.next());
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	public static <T extends TOP> ArrayList<T> fromFSListToCollection(
			FSList list, Class<T> classType) {

		Collection<T> myCollection = JCasUtil.select(list, classType);
		/*
		 * for(T element:myCollection){ System.out.println(.getText()); }
		 */

		return new ArrayList<T>(myCollection);
	}

	public static <T extends Annotation> FSList fromCollectionToFSList(
			JCas aJCas, Collection<T> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<T> i = aCollection.iterator();
		while (i.hasNext()) {
			head.setHead(i.next());
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	/**
	 * Creates FeatureStructure List from questionList
	 * 
	 * @param <T>
	 * 
	 * @param aJCas
	 * @param aCollection
	 * @return FSList
	 */

	public static FSList createQuestionAnswerSet(JCas aJCas,
			Collection<QuestionAnswerSet> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<QuestionAnswerSet> i = aCollection.iterator();
		while (i.hasNext()) {
			head.setHead(i.next());
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	/**
	 * @param aJCas
	 * @param aCollection
	 * @return
	 */
	public static FSList createTokenList(JCas aJCas,
			Collection<Token> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<Token> i = aCollection.iterator();
		while (i.hasNext()) {
			head.setHead(i.next());
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	/**
	 * @param aJCas
	 * @param aCollection
	 * @return
	 */
	public static FSList createNounPhraseList(JCas aJCas,
			Collection<NounPhrase> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<NounPhrase> i = aCollection.iterator();
		while (i.hasNext()) {
			head.setHead(i.next());
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	/**
	 * @param aJCas
	 * @param aCollection
	 * @return
	 */
	public static FSList createNERList(JCas aJCas, Collection<NER> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<NER> i = aCollection.iterator();
		while (i.hasNext()) {
			head.setHead(i.next());
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	public static FSList createDependencyList(JCas aJCas,
			Collection<SemanticGraphEdge> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<SemanticGraphEdge> i = aCollection.iterator();
		while (i.hasNext()) {
			SemanticGraphEdge edge = i.next();
			Dependency dep = new Dependency(aJCas);

			Token governorToken = new Token(aJCas);
			governorToken.setText(edge.getGovernor().originalText());
			governorToken.setPos(edge.getGovernor().tag());
			governorToken.setNer(edge.getGovernor().ner());
			governorToken.addToIndexes();
			dep.setGovernor(governorToken);

			Token dependentToken = new Token(aJCas);
			dependentToken.setText(edge.getDependent().originalText());
			dependentToken.setPos(edge.getDependent().tag());
			dependentToken.setNer(edge.getDependent().ner());
			dependentToken.addToIndexes();
			dep.setDependent(dependentToken);

			dep.setRelation(edge.getRelation().toString());
			dep.addToIndexes();

			head.setHead(dep);
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	public static SourceDocument getSourceDocumentFromCAS(JCas jCas) {
		FSIterator it = jCas.getAnnotationIndex(SourceDocument.type).iterator();
		SourceDocument srcDoc = null;
		if (it.hasNext()) {
			srcDoc = (SourceDocument) it.next();
		}
		return srcDoc;
	}

	public static ArrayList<Sentence> getSentenceListFromTestDocCAS(JCas jCas) {

		TestDocument testDoc = Utils.getTestDocumentFromCAS(jCas);
		FSList sentList = testDoc.getSentenceList();
		ArrayList<Sentence> sentenceList = new ArrayList<Sentence>();
		int i = 0;
		while (true) {

			Sentence sentence = null;
			try {
				sentence = (Sentence) sentList.getNthElement(i);
			} catch (Exception e) {
				break;
			}
			sentenceList.add(sentence);
			i++;
		}

		return sentenceList;
	}

	public static ArrayList<QuestionAnswerSet> getQuestionAnswerSetFromTestDocCAS(
			JCas jCas) {

		TestDocument testDoc = Utils.getTestDocumentFromCAS(jCas);
		FSList fsQAList = testDoc.getQaList();
		ArrayList<QuestionAnswerSet> qaSetList = new ArrayList<QuestionAnswerSet>();
		int i = 0;
		while (true) {

			QuestionAnswerSet qaSet = null;
			try {
				qaSet = (QuestionAnswerSet) fsQAList.getNthElement(i);

			} catch (Exception e) {
				break;
			}
			qaSetList.add(qaSet);
			i++;
		}

		return qaSetList;
	}

	public static ArrayList<Question> getQuestionListFromTestDocCAS(JCas jCas) {

		TestDocument testDoc = Utils.getTestDocumentFromCAS(jCas);
		FSList fsQAList = testDoc.getQaList();
		ArrayList<Question> questionList = new ArrayList<Question>();
		int i = 0;
		while (true) {

			Question question = null;
			try {
				QuestionAnswerSet qaSet = (QuestionAnswerSet) fsQAList
						.getNthElement(i);
				question = qaSet.getQuestion();
			} catch (Exception e) {
				break;
			}
			questionList.add(question);
			i++;
		}

		return questionList;
	}

	public static ArrayList<ArrayList<Answer>> getAnswerListFromTestDocCAS(
			JCas jCas) {

		TestDocument testDoc = Utils.getTestDocumentFromCAS(jCas);
		FSList fsQAList = testDoc.getQaList();

		ArrayList<ArrayList<Answer>> answerList = new ArrayList<ArrayList<Answer>>();
		int i = 0;
		while (true) {

			ArrayList<Answer> answerChoiceList = null;
			try {
				QuestionAnswerSet qaSet = (QuestionAnswerSet) fsQAList
						.getNthElement(i);

				answerChoiceList = Utils.fromFSListToCollection(
						qaSet.getAnswerList(), Answer.class);

			} catch (Exception e) {
				break;
			}
			answerList.add(answerChoiceList);
			i++;
		}

		return answerList;
	}

	public static ArrayList<Token> getTokenListFromSentenceList(
			Sentence sentence) {

		FSList fsTokenList = sentence.getTokenList();
		ArrayList<Token> tokenList = new ArrayList<Token>();
		int i = 0;
		while (true) {

			Token token = null;
			try {
				token = (Token) fsTokenList.getNthElement(i);
			} catch (Exception e) {
				break;
			}
			tokenList.add(token);
			i++;
		}

		return tokenList;
	}

	public static ArrayList<NounPhrase> getNounPhraseListFromQuestionList(
			Question question) {

		FSList fsNounList = question.getNounList();
		ArrayList<NounPhrase> nounPhraseList = new ArrayList<NounPhrase>();
		int i = 0;
		while (true) {

			NounPhrase nounPhrase = null;
			try {
				nounPhrase = (NounPhrase) fsNounList.getNthElement(i);
			} catch (Exception e) {
				break;
			}
			nounPhraseList.add(nounPhrase);
			i++;
		}

		return nounPhraseList;
	}

	public static ArrayList<Token> getTokenListFromQuestion(Question question) {

		FSList fsTokenList = question.getTokenList();
		ArrayList<Token> tokenList = new ArrayList<Token>();
		int i = 0;
		while (true) {

			Token token = null;
			try {
				token = (Token) fsTokenList.getNthElement(i);
			} catch (Exception e) {
				break;
			}
			tokenList.add(token);
			i++;
		}

		return tokenList;
	}

	public static ArrayList<Token> getTokenListFromAnswer(Answer answer) {

		FSList fsTokenList = answer.getTokenList();
		ArrayList<Token> tokenList = new ArrayList<Token>();
		int i = 0;
		while (true) {

			Token token = null;
			try {
				token = (Token) fsTokenList.getNthElement(i);
			} catch (Exception e) {
				break;
			}
			tokenList.add(token);
			i++;
		}

		return tokenList;
	}

	public static ArrayList<Sentence> getSentenceListFromSourceDocCAS(JCas jCas) {

		SourceDocument srcDoc = Utils.getSourceDocumentFromCAS(jCas);
		FSList sentList = srcDoc.getSentenceList();
		ArrayList<Sentence> sentenceList = new ArrayList<Sentence>();
		int i = 0;
		while (true) {

			Sentence sentence = null;
			try {
				sentence = (Sentence) sentList.getNthElement(i);
			} catch (Exception e) {
				break;
			}
			sentenceList.add(sentence);
			i++;
		}

		return sentenceList;
	}

	public static TestDocument getTestDocumentFromCAS(JCas jCas) {
		FSIterator it = jCas.getAnnotationIndex(TestDocument.type).iterator();
		TestDocument srcDoc = null;
		if (it.hasNext()) {
			srcDoc = (TestDocument) it.next();
		}
		return srcDoc;
	}

	public static CandidateSentence getCandidateSentenceFromCAS(JCas jCas) {
		FSIterator it = jCas.getAnnotationIndex(CandidateSentence.type)
				.iterator();
		CandidateSentence candSent = null;
		if (it.hasNext()) {
			candSent = (CandidateSentence) it.next();
		}
		return candSent;
	}

	public static boolean isInsideBracket(ArrayList<Brackets> bracketedList,
			int pos) {

		boolean isInside = false;
		for (int i = 0; i < bracketedList.size(); i++) {
			int start = bracketedList.get(i).getStart();
			int end = bracketedList.get(i).getEnd();

			if (pos >= start && pos <= end) {
				isInside = true;
				break;
			}

			if (start > pos) {
				break;
			}
		}
		return isInside;
	}

	public static ArrayList<Brackets> findBrackatedExpression(String docText)
			throws Exception {

		Pattern pattern = Pattern.compile("[(].*?[)]");
		Matcher matcher = pattern.matcher(docText);
		ArrayList<Brackets> bracketList = new ArrayList<Brackets>();

		while (matcher.find()) {
			String matched = matcher.group();
			int start = matcher.start();
			int end = matcher.end();
			Brackets brackets = new Brackets(matched, start, end);
			// System.out.println("******Bracket\t"+matched+"\t"+start+"\t"+end);
			bracketList.add(brackets);
		}

		return bracketList;

	}

}
