package edu.cmu.lti.qalab.collectionreaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import edu.cmu.lti.qalab.types.SourceDocument;

/**
 * 
 * @author alkesh
 * 
 *         GeneFileReader reads the input file and convert each sentence in CAS
 */
public class AlzheimerBgCorpusReader extends CollectionReader_ImplBase {

	File documents[] = null;
	int nCurrFile = 0;

	@Override
	public void initialize() throws ResourceInitializationException {
		try {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			File inputDir = new File(
					(String) getConfigParameterValue("INPUT_DIR"));
			documents = inputDir.listFiles(new OnlyNXML("txt"));
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
		String text = "";
		try {
			bfr = new BufferedReader(new FileReader(documents[nCurrFile]));
			char chars[] = new char[4096];
			while ((bfr.read(chars)) != -1) {
				text += new String(chars).trim();
				chars = null;
				chars = new char[4096];
			}
			text = text.trim();
			System.out.println("Read: "
					+ documents[nCurrFile].getAbsolutePath());

			// put document in CAS
			jcas.setDocumentText(text);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bfr != null) {
				bfr.close();
				bfr = null;
			}
		}

		SourceDocument doc = new SourceDocument(jcas);
		String fileId = documents[nCurrFile].getName();
		doc.setId(fileId);
		doc.setText(text);
		doc.addToIndexes();
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
		return new Progress[] { new ProgressImpl(nCurrFile, documents.length,
				Progress.ENTITIES) };
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		//return nCurrFile < 10;
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
