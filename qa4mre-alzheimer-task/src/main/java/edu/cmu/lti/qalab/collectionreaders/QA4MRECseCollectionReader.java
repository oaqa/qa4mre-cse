package edu.cmu.lti.qalab.collectionreaders;

import java.io.IOException;
import java.util.HashMap;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.collection.impl.CollectionReaderDescription_impl;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import com.google.common.collect.Maps;

public class QA4MRECseCollectionReader extends CollectionReader_ImplBase { // extends
																			// AbstractCollectionReader
																			// {

	private QA4MRETestDocReader delegate;

	private int id;

	@Override
	public void initialize() throws ResourceInitializationException {
		super.initialize();
		delegate = new QA4MRETestDocReader();
		HashMap<String, Object> params = Maps.newHashMap();
		params.put("UIMA_CONTEXT", super.getUimaContext());
		delegate.initialize(new CollectionReaderDescription_impl(), params);
		id = 0;
	}

	/*
	 * protected DataElement getNextElement() throws Exception { return new
	 * DataElement(getDataset(), String.valueOf(id++), null, null); }
	 */

	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		delegate.getNext(aCAS);
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return delegate.hasNext();
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Progress[] getProgress() {
		return new Progress[] { new ProgressImpl(delegate.nCurrFile,
				delegate.documents.getLength(), Progress.ENTITIES) };
	}
}
