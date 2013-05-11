package edu.cmu.lti.qalab.casconsumers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;

public class NaiveScorerCasConsumer extends CasConsumer_ImplBase {

	int mDocNum;
	File mOutputDir = null;

	double THRESHOLD = 4.0;
	BufferedWriter out;
	
	int correct = 0;
	int unanswered = 0;
	int total = 0;
	double cAt1 = 0.0;

	@Override
	public void initialize() {

		mDocNum = 0;
		try {
			mOutputDir = new File(
					(String) getConfigParameterValue("OUTPUT_DIR"));

			THRESHOLD = Double
					.parseDouble((String) getConfigParameterValue("THRESHOLD"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * if (!mOutputFile.exists()) { mOutputDir.mkdirs(); }
		 */
		FileWriter fw;
		try {
			fw = new FileWriter(mOutputDir);
			out = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void processCas(CAS aCAS) throws ResourceProcessException {
		//DEBUG System.out.println("!!!Entered here!!!");

		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		// retreive the filename of the input file from the CAS
		File outFile = mOutputDir;

		// serialize XCAS and write to output file
		try {
			writeToFile(jcas, outFile);
		} catch (IOException e) {
			throw new ResourceProcessException(e);
		}
	}

	/**
	 * Write a CAS to specific .out format mentioned above. May occupy several
	 * lines in the output file.
	 * 
	 * @param aCas
	 *            CAS to serialize
	 * @param name
	 *            output file
	 * 
	 * @throws IOException
	 *             if an I/O failure occurs
	 * @throws SAXException
	 *             if an error occurs generating the XML text
	 */
	private void writeToFile(JCas jcas, File name) throws IOException {

		FSIterator<Annotation> it = jcas.getAnnotationIndex().iterator();
		StringBuffer sb = new StringBuffer();
		/**
		 * calculating C@1 measurement.. 
		 */
		

		while (it.hasNext()) {
			Annotation an = (it.next());
			//System.out.println(an);
			
			if (an instanceof TestDocument) {
				TestDocument doc = (TestDocument) an;
				FSList list = doc.getQaList();
				boolean answered = false;
				while (list instanceof NonEmptyFSList) { //every question
					FeatureStructure head = ((NonEmptyFSList) list).getHead();
					QuestionAnswerSet qas = (QuestionAnswerSet) head;
					Question q = qas.getQuestion();
					//DEBUG System.out.println("Question: " + q.getText());
					FSList aList = qas.getAnswerList();
					while (aList instanceof NonEmptyFSList) { //every answer item
						FeatureStructure qaHead = ((NonEmptyFSList) aList)
								.getHead();
						Answer ans = (Answer) qaHead;
						//DEBUG System.out.println("Answer: " + ans.getId() + ", text: "
						//		+ ans.getText());
						if(ans.getIsCorrect())
						{
							if(ans.getIsSelected())
							{
								correct++;
							}
						}
						if(ans.getIsSelected())
						{
							answered = true;
						}
						aList = ((NonEmptyFSList) aList).getTail();
					}
					if(!answered)
					{
						unanswered++;
					}
					total++;
					// do something with this element
					list = ((NonEmptyFSList) list).getTail();
				}
			}
		}
		
		

		try {
			// System.out.println(sb.toString());
			// out.write(sb.toString());
		} finally {
			;
		}
	}

	private void writeXmi(CAS aCas, File outFile) throws IOException,
			SAXException {
		FileOutputStream out = null;
		try {
			// write XMI
			out = new FileOutputStream(outFile);
			XmiCasSerializer ser = new XmiCasSerializer(aCas.getTypeSystem());
			XMLSerializer xmlSer = new XMLSerializer(out, false);
			ser.serialize(aCas, xmlSer.getContentHandler());
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Closes the file and other resources initialized during the process
	 * 
	 */
	@Override
	public void destroy() {
		if(total > 0)
		{
			cAt1 = (((double)correct)/((double) total) * unanswered + (double)correct) * (1.0/total);
		}
		
		System.out.format("Total number of questions: %d\nTotal number of correct answers: %d\nTotal number of unanswered questions: %d\nThe c@1 score is: %.2f.\n", total, correct, unanswered, cAt1);
	}
}
