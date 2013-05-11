package edu.cmu.lti.qalab.annotators;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.SourceDocument;
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

public class StanfordNLPAnnotator extends JCasAnnotator_ImplBase {

	private StanfordCoreNLP stanfordAnnotator;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		stanfordAnnotator = new StanfordCoreNLP(props);
	}

	public Object getAnnotationObject(JCas jCas, int type) {

		FSIterator fsIt = jCas.getAnnotationIndex(type).iterator();
		Object obj = null;
		if (fsIt.hasNext()) {
			obj = fsIt.next();
		}
		return obj;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		SourceDocument srcDoc = (SourceDocument) this.getAnnotationObject(jCas,
				SourceDocument.type);

		String id = srcDoc.getId();
		String docText = srcDoc.getText();

		Annotation document = new Annotation(docText);
		try {
			stanfordAnnotator.annotate(document);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		// SourceDocument sourcecDocument=(SourceDocument)
		// jCas.getAnnotationIndex(SourceDocument.type);
		int sentNo = 0;
		// FSList sentenceList = srcDoc.getSentenceList();
		ArrayList<Sentence> sentList = new ArrayList<Sentence>();
		for (CoreMap sentence : sentences) {

			String sentText = sentence.toString();
			Sentence annSentence = new Sentence(jCas);
			ArrayList<Token> tokenList = new ArrayList<Token>();

			// Dependency should have Token rather than String
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) { // order
																			// needs
																			// to
																			// be
																			// considered
				int begin=token.beginPosition();
				
				int end=token.endPosition();
				System.out.println(begin+"\t"+end);
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

			FSList fsTokenList = Utils.createTokenList(jCas, tokenList);
			fsTokenList.addToIndexes();

			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies = sentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class);
			List<SemanticGraphEdge> depList = dependencies.edgeListSorted();
			FSList fsDependencyList = Utils.createDependencyList(jCas, depList);
			fsDependencyList.addToIndexes();
			// Dependency dependency = new Dependency(jCas);
			// System.out.println("Dependencies: "+dependencies);

			annSentence.setId(String.valueOf(sentNo));
			annSentence.setBegin(tokenList.get(0).getBegin());//begin of first token
			annSentence.setEnd(tokenList.get(tokenList.size()-1).getEnd());//end of last token
			annSentence.setText(sentText);
			annSentence.setTokenList(fsTokenList);
			annSentence.setDependencyList(fsDependencyList);
			annSentence.addToIndexes();
			sentList.add(annSentence);
			sentNo++;
			System.out.println("Sentence no. " + sentNo + " processed");
		}

		FSList fsSentList = Utils.createSentenceList(jCas, sentList);
		
		//this.iterateFSList(fsSentList);
		fsSentList.addToIndexes();

		srcDoc.setId(id);
		srcDoc.setSentenceList(fsSentList);
		srcDoc.addToIndexes();
	}

	
}
