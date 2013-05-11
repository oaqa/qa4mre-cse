package edu.cmu.lti.qalab.annotators;

import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.dependency.Depsimilar;
import edu.cmu.lti.qalab.types.Dependency;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class JustForTestAnnotator extends JCasAnnotator_ImplBase{

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
	}
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		
		ArrayList<Sentence> senList =  Utils.getSentenceListFromSourceDocCAS(aJCas);
		Sentence sen1 = senList.get(0);
		Sentence sen2 = senList.get(1);
		System.out.println(sen1.getText());
		System.out.println(sen2.getText());
		FSList den1 = sen1.getDependencyList();
		den1.getNthElement(0);
		
		try {
			Depsimilar d = new Depsimilar("en-PubMedOA-20070501","data/dpTree.xml");
			System.out.println(d.getScore(sen1,sen2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
