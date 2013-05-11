package edu.cmu.lti.qalab.runner;
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
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
public class testNlp {

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StanfordCoreNLP stanfordAnnotator;
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		stanfordAnnotator = new StanfordCoreNLP(props);
		// TODO Auto-generated method stub
		Annotation document = new Annotation("This is the knife with which I killed Tom.");
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

			}


			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies = sentence
					.get(CollapsedCCProcessedDependenciesAnnotation.class);
			List<SemanticGraphEdge> depList = dependencies.edgeListSorted();
			;
			System.out.println();
			IndexedWord[] a = 	dependencies.getRoots().toArray(new IndexedWord[0]);
			System.out.println(a[0].value()+" ");
			// Dependency dependency = new Dependency(jCas);
			// System.out.println("Dependencies: "+dependencies);
			sentNo++;
			System.out.println("Sentence no. " + sentNo + " processed");
			System.out.println(dependencies.getParent(dependencies.getNodeByWordPattern("which")).originalText());
			
			IndexedWord second_parent = dependencies.getParent(dependencies.getNodeByWordPattern("which"));
			IndexedWord[] b = dependencies.getChildList(second_parent).toArray(new IndexedWord[0]);;//.toArray(new IndexedWord[0]);
			for(IndexedWord w:b){
				System.out.println(w.originalText());
			}
		}
	}
	



	

	
	

}
