package edu.cmu.lti.qalab.collectionreaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.SourceDocument;
import edu.cmu.lti.qalab.types.TestDocument;

/**
 * 
 * @author alkesh
 * 
 *         GeneFileReader reads the input file and convert each sentence in CAS
 */
public class EntranceExamCorpusReader extends CollectionReader_ImplBase {

	File documents[] = null;
	int nCurrFile = 0;

	@Override
	public void initialize() throws ResourceInitializationException {
		try {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			File inputDir = new File(
					(String) getConfigParameterValue("INPUT_DIR"));
			documents = inputDir.listFiles(new OnlyNXML("xml"));
			System.out.println("Total files: " + documents.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException(e);
		}

		// open input file list iterator
		BufferedReader bfr = null;
		String docText = "";
		String fileId;
		ArrayList<QuestionAnswerSet> questionAnswersList = new ArrayList<QuestionAnswerSet>();
		try {

			File fXmlFile = new File(documents[nCurrFile].getAbsolutePath());
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());

			NodeList nList = doc.getElementsByTagName("doc");
			Node nNode = nList.item(0);
			System.out.println("\nCurrent Element :" + nNode.getNodeName());
			Element eElement = (Element) nNode;
			System.out.println("doc id : " + eElement.getAttribute("d_id"));
			fileId = eElement.getAttribute("d_id");
			System.out.println(eElement.getChildNodes().item(0).getNodeValue());
			docText = eElement.getChildNodes().item(0).getNodeValue();

			NodeList qList = doc.getElementsByTagName("question");

			for (int temp = 0; temp < qList.getLength(); temp++) {
				nNode = qList.item(temp);
				System.out.println("\nCurrent Element :" + nNode.getNodeName());
				eElement = (Element) nNode;
				System.out.println("q id : " + eElement.getAttribute("q_id"));
				String q_id = eElement.getAttribute("q_id");
				NodeList nlist = eElement.getElementsByTagName("q_str");
				System.out.println("question:"
						+ nlist.item(0).getChildNodes().item(0).getNodeValue());
				String questionStr = nlist.item(0).getChildNodes().item(0)
						.getNodeValue();
				Question question = new Question(jcas);
				question.setText(questionStr);
				question.setId(q_id);
				ArrayList<Answer> answerCollection = new ArrayList<Answer>();
				NodeList alist = eElement.getElementsByTagName("answer");
				for (int tmp = 0; tmp < alist.getLength(); tmp++) {
					nNode = alist.item(tmp);
					eElement = (Element) nNode;
					System.out.println("a id : "
							+ eElement.getAttribute("a_id"));
					String a_id = eElement.getAttribute("a_id");
					System.out.println("answer:"
							+ alist.item(tmp).getChildNodes().item(0)
									.getNodeValue());
					String answer = alist.item(tmp).getChildNodes().item(0)
							.getNodeValue();
					Answer ans = new Answer(jcas);
					ans.setId(a_id);
					ans.setText(answer);
					answerCollection.add(ans);
				}
				FSList answerFSList=this.createAnswerFSList(jcas, answerCollection);			
				QuestionAnswerSet questionAnswers=new QuestionAnswerSet(jcas);
				questionAnswers.setQuestion(question);
				questionAnswers.setAnswerList(answerFSList);	
				questionAnswersList.add(questionAnswers);
			}
			FSList quetionAnswersFSList=this.createQuestionAnswersFSList(jcas,questionAnswersList);
			
			//put document in CAS
			jcas.setDocumentText(docText);
			TestDocument testDoc=new TestDocument(jcas);
			testDoc.setId(fileId);
			testDoc.setText(docText);
			testDoc.setQaList(quetionAnswersFSList); 				
			SourceDocument newDoc = new SourceDocument(jcas);
			newDoc.setId(fileId);
			newDoc.setText(docText);
			newDoc.addToIndexes();
			testDoc.addToIndexes();
			newDoc.addToIndexes();
		} catch (Exception e) {
			e.printStackTrace();
		}

		nCurrFile++;

	}
	public FSList createQuestionAnswersFSList(JCas aJCas,
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
	 * Closes the file and other resources initialized during the process
	 * 
	 */

	@Override
	public void close() throws IOException {
		System.out.println("Closing AlzheimerNXMLReader");
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(nCurrFile, documents.length,
				Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		// return nCurrFile < 10;
		return nCurrFile < documents.length;
	}

	private class OnlyNXML implements FilenameFilter {
		String ext;

		public OnlyNXML(String ext) {
			this.ext = "." + ext;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(ext);
		}
	}

}
