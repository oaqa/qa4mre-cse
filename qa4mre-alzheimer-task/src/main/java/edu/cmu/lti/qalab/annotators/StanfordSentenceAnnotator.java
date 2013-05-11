package edu.cmu.lti.qalab.annotators;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Brackets;
import edu.cmu.lti.qalab.utils.Utils;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordSentenceAnnotator extends JCasAnnotator_ImplBase{
	private StanfordCoreNLP stanfordAnnotator;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit");
		stanfordAnnotator = new StanfordCoreNLP(props);
	}

	

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {

		TestDocument testDoc = (TestDocument) Utils.getTestDocumentFromCAS(jCas);

		String id = testDoc.getId();
		String docText = testDoc.getText();
		//System.out.println("===============================================");
		//System.out.println("DocText: " + docText);
		String segments[] = docText.split("[\\n]");
		//FSList allFSSentList=new FSList(jCas);
		ArrayList<Sentence> sentList = new ArrayList<Sentence>();
		for (int i = 0; i < segments.length; i++) {
			String segmentText=segments[i];
			//System.out.println(i+"\t"+segmentText);
			//System.out.println("=========================================================");
			
			Annotation document = new Annotation(segmentText);
			
			try {
				//System.out.println("Entering stanford annotation");
				stanfordAnnotator.annotate(document);
				//System.out.println("Out of stanford annotation");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			// SourceDocument sourcecDocument=(SourceDocument)
			// jCas.getAnnotationIndex(SourceDocument.type);
			int sentNo = 0;
			// FSList sentenceList = srcDoc.getSentenceList();
			
			ArrayList<Brackets> brackatedExpression = new ArrayList<Brackets>();
			try{
				brackatedExpression=Utils.findBrackatedExpression(segmentText);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			String candSent="";
			//int start=0;
			//int end=0;
			for (CoreMap sentence : sentences) {

				String sentText = sentence.toString();
				
				candSent+=sentText+" ";
				List<CoreLabel>coreLabels=sentence.get(TokensAnnotation.class);
				int sentStartPos=coreLabels.get(0).beginPosition();
				int sentEndPos=coreLabels.get(coreLabels.size()-1).endPosition();
				//System.out.println("Sent End: "+sentEndPos);
				Sentence annSentence = new Sentence(jCas);
				
				boolean isInside=Utils.isInsideBracket(brackatedExpression, sentEndPos);	
				
				if(isInside){
				
					continue;
				}
				sentText=candSent.trim();
				//System.out.println(sentText);
				annSentence.setId(String.valueOf(sentNo));
				annSentence.setBegin(sentStartPos);// begin of
																	// first
																	// token
				annSentence
						.setEnd(sentEndPos);// end
																				// of
																				// last
																				// token
				annSentence.setText(sentText);
				
				annSentence.addToIndexes();
				sentList.add(annSentence);
				sentNo++;
				candSent="";
				//System.out.println("Sentence no. " + sentNo + " processed");
			}
			//System.out.println("=========================================================");
			

			// this.iterateFSList(fsSentList);
			

		}
		FSList fsSentList = Utils.createSentenceList(jCas, sentList);
		fsSentList.addToIndexes();
		testDoc.setId(id);
		testDoc.setSentenceList(fsSentList);
		testDoc.addToIndexes();
	
		
	}
}
