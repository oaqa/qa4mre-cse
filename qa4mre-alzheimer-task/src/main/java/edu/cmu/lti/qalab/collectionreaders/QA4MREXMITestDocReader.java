package edu.cmu.lti.qalab.collectionreaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.xml.sax.SAXException;

import edu.cmu.lti.qalab.types.SourceDocument;
import edu.cmu.lti.qalab.types.TestDocument;

/**
 * 
 * @author alkesh
 * 
 *         GeneFileReader reads the input file and convert each sentence in CAS
 */
public class QA4MREXMITestDocReader extends CollectionReader_ImplBase {

	File documents[] = null;
	int nCurrFile = 0;

	
	@Override
	public void initialize() throws ResourceInitializationException {
		
		File inputDir = new File(
				(String) getConfigParameterValue("INPUT_DIR"));
		documents = inputDir.listFiles();
		System.out.println("Total files: "+documents.length);
	}

	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {

		
		String text = "";
		try {
			
			File currentFile = (File) documents[nCurrFile];
		    FileInputStream inputStream = new FileInputStream(currentFile);
		    try {
		        XmiCasDeserializer.deserialize(inputStream, aCAS, true);
		    } catch (SAXException e) {
		      throw new CollectionException(e);
		    } finally {
		      inputStream.close();
		      inputStream=null;
		    }
		    System.out.println("Read: "
					+ documents[nCurrFile].getAbsolutePath());

			
		}catch (Exception e) {
			e.printStackTrace();			
		} 
		
		JCas jcas;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			throw new CollectionException(e);
		}
		
		TestDocument doc = new TestDocument(jcas);
		/*String fileId = documents[nCurrFile].getName();
		doc.setId(fileId);
		doc.setText(text);
		doc.addToIndexes();*/
		nCurrFile++;
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
		return new Progress[] { new ProgressImpl(nCurrFile,
				documents.length, Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return nCurrFile < documents.length;
	}

}
