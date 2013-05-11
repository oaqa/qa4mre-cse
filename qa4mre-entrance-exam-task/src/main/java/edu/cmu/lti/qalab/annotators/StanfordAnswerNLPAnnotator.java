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

public class StanfordAnswerNLPAnnotator extends JCasAnnotator_ImplBase {

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
		ArrayList<QuestionAnswerSet> qaList = Utils
				.getQuestionAnswerSetFromTestDocCAS(jCas);// .getQuestionListFromTestDocCAS(jCas);

		System.out.println("Total Questions: " + qaList.size());
		int sentNo = 0;
		for (int i = 0; i < qaList.size(); i++) {

			ArrayList<Answer> answers = Utils.fromFSListToCollection(qaList
					.get(i).getAnswerList(),Answer.class);
			for (int i2 = 0; i2 < answers.size(); i2++) {
				String answerText = answers.get(i2).getText();
				Annotation document = new Annotation(answerText);
				try {
					// System.out.println("Entering stanford annotation");
					stanfordAnnotator.annotate(document);
					// System.out.println("Out of stanford annotation");
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				List<CoreMap> nlpedAnswer = document
						.get(SentencesAnnotation.class);

				for (CoreMap answerUnit : nlpedAnswer) {

					String qText = answerUnit.toString();
					Answer annAnswer = new Answer(jCas);
					ArrayList<Token> tokenList = new ArrayList<Token>();
					// Dependency should have Token rather than String
					for (CoreLabel token : answerUnit
							.get(TokensAnnotation.class)) { // order
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

					// this is the Stanford dependency graph of the current
					// sentence
					SemanticGraph dependencies = answerUnit
							.get(CollapsedCCProcessedDependenciesAnnotation.class);
					List<SemanticGraphEdge> depList = dependencies
							.edgeListSorted();
					FSList fsDependencyList = this.createDependencyList(jCas,
							depList);
					fsDependencyList.addToIndexes();
					// Dependency dependency = new Dependency(jCas);
					// System.out.println("Dependencies: "+dependencies);

					annAnswer.setId(String.valueOf(sentNo));
					annAnswer.setBegin(tokenList.get(0).getBegin());// begin of
																	// first
																	// token
					annAnswer.setEnd(tokenList.get(tokenList.size() - 1)
							.getEnd());// end
										// of
										// last
										// token
					annAnswer.setText(answerText);
					annAnswer.setTokenList(fsTokenList);
					annAnswer.setDependencies(fsDependencyList);
					annAnswer.addToIndexes();
					answers.set(i2, annAnswer);

					System.out.println("Question no. " + sentNo + " Answer no."
							+ i2 + " processed");
				}

			}
			sentNo++;

			FSList answerFSList = this.createAnswerFSList(jCas, answers);
			for (int i2 = 0; i2 < answers.size(); i2++) {
				answers.get(i2).addToIndexes();
				qaList.get(i).setAnswerList(answerFSList);
			}

		}
		// FSList fsQuestionList = Utils.createQuestionList(jCas, questionList);
		// fsQuestionList.addToIndexes();
		FSList fsQASet = Utils.createQuestionAnswerSet(jCas, qaList);
		testDoc.setQaList(fsQASet);
		testDoc.addToIndexes();
		for(Answer a:Utils.fromFSListToCollection(Utils.getQuestionAnswerSetFromTestDocCAS(
				jCas).get(0).getAnswerList(),Answer.class)){
			try{
				for(int i=0;;i++){
					System.out.println(a.getDependencies().getNthElement(i).toString());
				}
				
			}catch(Exception e){
				
			}
			
		}
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

	public FSList createAnswerFSList(JCas aJCas, Collection<Answer> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<Answer> i = aCollection.iterator();
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
