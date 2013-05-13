package edu.cmu.lti.qalab.annotators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.Dependency;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.utils.Utils;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

public class StanfordQuestionNLPAnnotator extends JCasAnnotator_ImplBase {

	private StanfordCoreNLP stanfordAnnotator;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");// ,
																			// ssplit
		stanfordAnnotator = new StanfordCoreNLP(props);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		TestDocument testDoc = (TestDocument) Utils
				.getTestDocumentFromCAS(jCas);

		String id = testDoc.getId();
		ArrayList<Question> questionList = Utils
				.getQuestionListFromTestDocCAS(jCas);
		ArrayList<ArrayList<Answer>> answerList = Utils
				.getAnswerListFromTestDocCAS(jCas);

		for (int i = 0; i < answerList.size(); i++) {

			ArrayList<Answer> choiceList = answerList.get(i);
			for (int j = 0; j < choiceList.size(); j++) {
				Answer answer = choiceList.get(j);
				Annotation document = new Annotation(answer.getText());
				try {
					// System.out.println("Entering stanford annotation");
					stanfordAnnotator.annotate(document);
					// System.out.println("Out of stanford annotation");
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				List<CoreMap> answers = document.get(SentencesAnnotation.class);

				for (CoreMap ans : answers) {

					String ansText = ans.toString();
					//Answer annAnswer = new Answer(jCas);
					ArrayList<Token> tokenList = new ArrayList<Token>();

					// Dependency should have Token rather than String
					for (CoreLabel token : ans.get(TokensAnnotation.class)) { // order
																				// needs
																				// to
																				// be
																				// considered
						int begin = token.beginPosition();

						int end = token.endPosition();
						// System.out.println(begin + "\t" + end);
						String orgText = token.originalText();
						// this is the POS tag of the token
						String pos = token.get(PartOfSpeechAnnotation.class);
						// this is the NER label of the token
						String ne = token.get(NamedEntityTagAnnotation.class);
						Token annToken = new Token(jCas);
						annToken.setBegin(begin);
						annToken.setEnd(end);
						annToken.setText(orgText);
						annToken.setPos(pos);
						annToken.setNer(ne);
						annToken.addToIndexes();

						tokenList.add(annToken);
					}

					FSList fsTokenList = this.createTokenList(jCas, tokenList);
					fsTokenList.addToIndexes();

					answer.setId(String.valueOf(j));
					answer.setBegin(tokenList.get(0).getBegin());// begin of
																	// first
																	// token
					answer.setEnd(tokenList.get(tokenList.size() - 1)
							.getEnd());// end
										// of
										// last
										// token
					answer.setText(ansText);
					answer.setTokenList(fsTokenList);

					answer.addToIndexes();
					choiceList.set(j, answer);

					System.out.println("Answer no. " + j + " processed");
				}
				
			}
			answerList.set(i, choiceList);
			
		}

		System.out.println("Total Questions: " + questionList.size());
		int sentNo = 0;
		for (int i = 0; i < questionList.size(); i++) {

			String questionText = questionList.get(i).getText();
			Annotation document = new Annotation(questionText);

			try {
				// System.out.println("Entering stanford annotation");
				stanfordAnnotator.annotate(document);
				// System.out.println("Out of stanford annotation");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Although it is defined as list...this list will contain only one
			// question
			List<CoreMap> questions = document.get(SentencesAnnotation.class);

			for (CoreMap question : questions) {

				String qText = question.toString();
				Question annQuestion = new Question(jCas);
				ArrayList<Token> tokenList = new ArrayList<Token>();

				// Dependency should have Token rather than String
				for (CoreLabel token : question.get(TokensAnnotation.class)) { // order
																				// needs
																				// to
																				// be
																				// considered
					int begin = token.beginPosition();

					int end = token.endPosition();
					// System.out.println(begin + "\t" + end);
					String orgText = token.originalText();
					// this is the POS tag of the token
					String pos = token.get(PartOfSpeechAnnotation.class);
					// this is the NER label of the token
					String ne = token.get(NamedEntityTagAnnotation.class);
					Token annToken = new Token(jCas);
					annToken.setBegin(begin);
					annToken.setEnd(end);
					annToken.setText(orgText);
					annToken.setPos(pos);
					annToken.setNer(ne);
					annToken.addToIndexes();

					tokenList.add(annToken);
				}

				FSList fsTokenList = this.createTokenList(jCas, tokenList);
				fsTokenList.addToIndexes();

				// this is the Stanford dependency graph of the current sentence
				SemanticGraph dependencies = question
						.get(CollapsedCCProcessedDependenciesAnnotation.class);
				List<SemanticGraphEdge> depList = dependencies.edgeListSorted();
				FSList fsDependencyList = this.createDependencyList(jCas,
						depList);
				fsDependencyList.addToIndexes();
				// Dependency dependency = new Dependency(jCas);
				// System.out.println("Dependencies: "+dependencies);

				annQuestion.setId(String.valueOf(sentNo));
				annQuestion.setBegin(tokenList.get(0).getBegin());// begin of
																	// first
																	// token
				annQuestion
						.setEnd(tokenList.get(tokenList.size() - 1).getEnd());// end
																				// of
																				// last
																				// token
				annQuestion.setText(questionText);
				annQuestion.setTokenList(fsTokenList);
				annQuestion.setDependencies(fsDependencyList);
				annQuestion.addToIndexes();
				questionList.set(i, annQuestion);
				sentNo++;
				System.out.println("Question no. " + sentNo + " processed");
			}
		}
		// FSList fsQuestionList = Utils.createQuestionList(jCas, questionList);
		// fsQuestionList.addToIndexes();
		ArrayList<QuestionAnswerSet> qaSet = Utils
				.getQuestionAnswerSetFromTestDocCAS(jCas);
		for (int i = 0; i < qaSet.size(); i++) {
			questionList.get(i).addToIndexes();
			qaSet.get(i).setQuestion(questionList.get(i));
			FSList fsAnswerList=Utils.fromCollectionToFSList(jCas, answerList.get(i));
			qaSet.get(i).setAnswerList(fsAnswerList);
			
		}

		FSList fsQASet = Utils.createQuestionAnswerSet(jCas, qaSet);

		testDoc.setId(id);
		testDoc.setQaList(fsQASet);
		testDoc.addToIndexes();

	}

	/**
	 * Creates FeatureStructure List from sentenceList
	 * 
	 * @param <T>
	 * 
	 * @param aJCas
	 * @param aCollection
	 * @return FSList
	 */

	public FSList createSentenceList(JCas aJCas,
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
	 * @param aJCas
	 * @param aCollection
	 * @return
	 */
	public FSList createTokenList(JCas aJCas, Collection<Token> aCollection) {
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

	public FSList createDependencyList(JCas aJCas,
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

}
