package edu.cmu.lti.qalab.annotators;

import java.util.HashSet;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;

import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.SourceDocument;
import edu.cmu.lti.qalab.utils.Utils;
import edu.stanford.nlp.util.StringUtils;

public class NoiseFilter extends JCasAnnotator_ImplBase {

	double QUALITY_THRESHOLD=0.10;
	int MIN_WORDS=5;
	int MIN_LENGTH=25;
	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		//System.out.println("******Entered into process of NoiseFilter");
		SourceDocument srcDoc=Utils.getSourceDocumentFromCAS(jCas);
		//String id = srcDoc.getId();
		String docText = srcDoc.getText();

		try {
			//String lines[] = docText.split("[\\n]");
			FSList sentList=srcDoc.getSentenceList();
			String filteredText = "";
			int i=0;
			while (true) {
			//	System.out.println("Processing sentence "+i);
				i++;
				Sentence sentence = null;
				try {
					sentence = (Sentence) sentList.getNthElement(i);
				} catch (Exception e) {
					break;
				}
				
				String sentText=sentence.getText().trim();
				if(sentText.equals("")){
					continue;
				}
				
				double qualityScore = this.getSentQuality(sentText);
				
				if(qualityScore<QUALITY_THRESHOLD){
					//sentence.removeFromIndexes();
					continue;
				}
				//System.out.println("****Quality Score: "+qualityScore);
				sentence.setQualityScore(qualityScore);
				sentence.addToIndexes();
				filteredText+=sentText+" ";
				
			}
						
			System.out.println("Difference between size of (SourceDocument - FilteredDocument): "+(docText.length()-filteredText.length()));
		
			SourceDocument annotation = new SourceDocument(jCas);
			//annotation.setId(id);
			annotation.setFilteredText(filteredText);
			annotation.addToIndexes();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public double getSentQuality(String sent) throws Exception {
		String words[] = sent.split("[\\W]");

		if (words.length <MIN_WORDS || sent.length() < MIN_LENGTH) {
			return 0.0;
		}

		HashSet<String> lowQualityWord = new HashSet<String>();
		lowQualityWord.add("Abstract");
		lowQualityWord.add("References");
		lowQualityWord.add("Medline");
		lowQualityWord.add("pp.");
		lowQualityWord.add("See also");

		int numericWords = 0;
		int abbrWords = 0;
		int lowQualityWords = 0;
		int totalWords = 0;
		for (int i = 0; i < words.length; i++) {
			if (StringUtils.isNumeric(words[i])) {
				numericWords++;
			}
			if (StringUtils.isAcronym(words[i])) {
				abbrWords++;
			}
			if (lowQualityWord.contains(words[i])) {
				lowQualityWords++;
			}

			totalWords++;
		}

		double noiseScore = (numericWords + abbrWords + lowQualityWords)
				/ (double) totalWords;

		double score = 1.0 - noiseScore;
		return score;

	}
	
}
