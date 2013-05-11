package edu.cmu.lti.qalab.annotators;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Brackets;
import edu.cmu.lti.qalab.utils.Utils;



public class TextSegmenter extends JCasAnnotator_ImplBase {

	
	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		try {
			TestDocument testDoc = Utils.getTestDocumentFromCAS(jCas);
			// String id = srcDoc.getId();
			String docId = testDoc.getId();
			System.out.println("################### DocId: " + docId+ "###########################");
			
			String docText = testDoc.getText();

			Pattern pattern = Pattern.compile("[0-9a-z][A-Z][a-z]+{3}");
			Matcher matcher = pattern.matcher(docText);

			ArrayList<Brackets> brackatedExpression = Utils.findBrackatedExpression(docText);

			ArrayList<Integer> posList = new ArrayList<Integer>();
			while (matcher.find()) {
				//String matched = matcher.group();
				int start = matcher.start();
				int end = matcher.end();
				//System.out.println("matched: "+matched+"\t"+start+"\t"+end);
				boolean isSegment = true;
				if (Character.isDigit(docText.charAt(start))) {
					if((start-1)>=0 && Character.isDigit(docText.charAt(start-1))){
						isSegment=true;
					}else{
						isSegment = false;
					}
				} else {
					// if (!Character.isDigit(docText.charAt(start))) {
					if ((start - 1) >= 0
							&& (docText.charAt(start - 1) == ' ')
							|| docText.charAt(start - 1) == Character
									.toUpperCase(docText.charAt(start - 1))) {
						isSegment = false;
					}
					if (start - 2 >= 0 && (docText.charAt(start - 2) == ' ')) {
						isSegment = false;
					}
					if (start - 3 >= 0 && (docText.charAt(start - 3) == ' ')) {
						isSegment = false;
					}
				}
				
				boolean isInsideBkrt=Utils.isInsideBracket(brackatedExpression,start+1);
				//System.out.println("isInsideBracket: "+isInsideBkrt+"\t"+"isSegment: "+isSegment);
				if(isInsideBkrt){
					isSegment=false;
				}
				if (start + 1 < end && isSegment) {
					posList.add(start + 1);
				}

				// System.out.println(matched + "\t" + start + "\t" + end);
			}
			int shift = 0;
			for (int i = 0; i < posList.size(); i++) {
				int pos = posList.get(i) + shift;
				// System.out.println();
				String segment1 = docText.substring(0, pos);
				String segment2 = docText.substring(pos);
				docText = segment1 + "\n" + segment2;
				shift++;
			}
			//System.out.println(docText);

			testDoc.setText(docText);
			testDoc.addToIndexes();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
