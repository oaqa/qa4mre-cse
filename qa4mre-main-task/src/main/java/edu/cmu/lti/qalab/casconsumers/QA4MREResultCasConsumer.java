package edu.cmu.lti.qalab.casconsumers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

import edu.cmu.lti.qalab.types.SourceDocument;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class QA4MREResultCasConsumer extends CasConsumer_ImplBase {

	int mDocNum;
	File mOutputDir = null;

	double THRESHOLD = 4.0;

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

	}

	@Override
	public void processCas(CAS aCAS) throws ResourceProcessException {

		JCas jCas;
		try {
			jCas = aCAS.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		TestDocument srcDoc = Utils.getTestDocumentFromCAS(jCas);
		
		String docId = srcDoc.getId();
		String outFileName = mOutputDir + "/" + docId + ".xmi";
		try {
			File outFile = new File(outFileName);
			this.writeXmi(aCAS, outFile);
		} catch (Exception e) {
			e.printStackTrace();
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

	}
}
