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

public class OuputRunCasConsumer extends CasConsumer_ImplBase {

	int mDocNum;
	File mOutputFile = null;

	double THRESHOLD = 4.0;
	BufferedWriter out;


	int correct = 0;
	int unanswered = 0;
	int total = 0;
	double cAt1 = 0.0;

	// if is_test == true, don't show the c@1 measure on the screen because we don't know the gold standard answer.
	boolean IS_TEST = false;

	/**
	 * for writing to the output format.
	 */
	String team_id = "cmuq"; // TBD: needs to be replaced by real team id in the
								// task.
	int current_year = 13;
	String number_of_run = "01"; // TBD: needs to be passed in from the
									// descriptor..
	String language_pair = "enen"; // We only deal with

	int t_id = 1; // seems like Alzheimer task only has t_id == 1.
	int r_id = 1; // reading test id. currently not recorded in the
					// collectionreader...? TBD: needs to be changed according
					// to the input reading document format..
	int q_id = 0; // changing (from 1 - 10) according to the question id in a
					// document
	int a_id = 0; // answer id, changing from 1 - 5.

	@Override
	public void initialize() {
		/**
		 * string buffer for the output file.
		 */
		StringBuffer sb = new StringBuffer();
		
		mDocNum = 0;
		try {
			mOutputFile = new File(
					(String) getConfigParameterValue("OUTPUT_DIR")
							+ "/output.xml");

			THRESHOLD = (Float) getConfigParameterValue("THRESHOLD");
			
			IS_TEST = (Boolean) getConfigParameterValue("IS_TEST");
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * if (!mOutputFile.exists()) { mOutputDir.mkdirs(); }
		 */
		FileWriter fw;
		try {
			fw = new FileWriter(mOutputFile);

			out = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/**
		 * writing the initial headers of the output file..
		 */
		try {
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
			sb.append(String.format("<output run_id=\"%s%d%s%s\">\n", team_id, current_year, number_of_run, language_pair));
			sb.append(String.format("<topic t_id=\"%d\" >\n", t_id));
			//DEBUG
			//System.out.println(sb.toString());
			out.write(sb.toString());
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		finally {
			;
		}
	}

	@Override
	public void processCas(CAS aCAS) throws ResourceProcessException {
		// DEBUG System.out.println("!!!Entered here!!!");

		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}


		// serialize XCAS and write to output file
		try {
			writeToFile(jcas, out);

		} catch (IOException e) {
			throw new ResourceProcessException(e);
		}
	}

	/**
	 * (1). determine whether the question is answered correctly, or unanswered,
	 * or answered not correctly..
	 * 
	 * (2). Write a CAS to specific .dtd format mentioned in QA4MRE2013_Guidelines.pdf. 

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
	private void writeToFile(JCas jcas, BufferedWriter bw) throws IOException {

		FSIterator<Annotation> it = jcas.getAnnotationIndex().iterator();
		StringBuffer sb = new StringBuffer();
		
		while (it.hasNext()) {
			Annotation an = (it.next());
			// System.out.println(an);

			if (an instanceof TestDocument) {
				TestDocument doc = (TestDocument) an;
				
				//TBD: get reading-test id, such as r_id = doc.getReadingTestId()
				out.write(String.format("\t<reading-test r_id=\"%d\">\n", Integer.parseInt(doc.getReadingTestId())));
				
				FSList list = doc.getQaList();
				boolean answered = false;
				int selectedAnswerId = -1;
				while (list instanceof NonEmptyFSList) { // every question
					answered = false; //reset "answered" variable.
					selectedAnswerId = -1;
					
					FeatureStructure head = ((NonEmptyFSList) list).getHead();
					QuestionAnswerSet qas = (QuestionAnswerSet) head;
					Question q = qas.getQuestion();
					
					//DEBUG
					//System.out.println(q.getId());
					
					q_id = Integer.parseInt(q.getId());
					// DEBUG System.out.println("Question: " + q.getText());
					FSList aList = qas.getAnswerList();
					while (aList instanceof NonEmptyFSList) { // every answer
																// item
						FeatureStructure qaHead = ((NonEmptyFSList) aList)
								.getHead();
						Answer ans = (Answer) qaHead;
						a_id = Integer.parseInt(ans.getId());
						// DEBUG System.out.println("Answer: " + ans.getId() +
						// ", text: "
						// + ans.getText());
						if (ans.getIsCorrect()) {
							if (ans.getIsSelected()) {
								correct++;
							}
						}
						if (ans.getIsSelected()) {
							answered = true;
							selectedAnswerId = a_id;
						}
						aList = ((NonEmptyFSList) aList).getTail();
					}
					if (!answered) {
						unanswered++;
					}
					total++;
					
					// write the result of this question to output file.
					String questionOutput = writeQuestionToOutput(answered, selectedAnswerId, q_id);
					out.write(questionOutput);
					
					// do something with the next element
					list = ((NonEmptyFSList) list).getTail();
				}
				out.write("\t</reading-test>\n");
			}
		}

	}

	/**
	 * 
	 * @param answered boolean variable of whether a question is answered..
	 * @param selectedAnswerId the answer id selected by the question. 
	 */
	private String writeQuestionToOutput(boolean answered, int selectedAnswerId, int q_id) {
		StringBuffer sb = new StringBuffer();
		if(!answered)
		{
			sb.append(String.format("\t\t<question q_id =\"%d\" answered=\"NO\" />\n", (q_id+1)));
		}
		else
		{
			sb.append(String.format("\t\t<question q_id=\"%d\" answered=\"YES\">\n", (q_id+1)));
			sb.append(String.format("\t\t\t<answer a_id=\"%d\"/>\n", (selectedAnswerId+1)));
			sb.append("\t\t</question>\n");
		}
		return sb.toString();

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
		if (!IS_TEST) {
			if (correct == 0) {
				System.err
						.format("There is no questions that were answered correctly. Maybe because it is a test document? If so, please set cas consumer confirugration parameter \"IS_TEST\"=true.\n");
			}
			if (total > 0) {
				cAt1 = (((double) correct) / ((double) total) * unanswered + (double) correct)
						* (1.0 / total);
			}

			System.out
					.format("Total number of questions: %d\nTotal number of correct answers: %d\nTotal number of unanswered questions: %d\nThe c@1 score is: %.2f.\n",
							total, correct, unanswered, cAt1);
		}
		/**
		 * finish up output.
		 */
		try {
			out.write("</topic>\n");
			out.write("</output>");
			out.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
