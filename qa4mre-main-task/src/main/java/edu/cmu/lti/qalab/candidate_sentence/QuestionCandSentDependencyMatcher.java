package edu.cmu.lti.qalab.candidate_sentence;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import de.linguatools.disco.DISCO;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.Dependency;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class QuestionCandSentDependencyMatcher extends JCasAnnotator_ImplBase {

	DISCO disco;
	DepTreeInfo tree;
	String filter = "";
	String postiveFilter = null;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		String path = (String) context.getConfigParameterValue("DISCO_PATH");
		String depTreePath = (String) context
				.getConfigParameterValue("DEPTREE_PATH");
		try {
			tree = new DepTreeInfo(depTreePath);
			disco = new DISCO(path, false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
		ArrayList<QuestionAnswerSet> qaSet = Utils.fromFSListToCollection(
				testDoc.getQaList(), QuestionAnswerSet.class);
		for (int i = 0; i < qaSet.size(); i++) {

			Question question = qaSet.get(i).getQuestion();
			System.out.println("Question: " + question.getText());
			ArrayList<Dependency> qDepList = Utils.fromFSListToCollection(
					question.getDependencies(), Dependency.class);

			ArrayList<CandidateSentence> candSentList = Utils
					.fromFSListToCollection(qaSet.get(i)
							.getCandidateSentenceList(),
							CandidateSentence.class);

			for (int j = 0; j < candSentList.size(); j++) {
				CandidateSentence candSent = candSentList.get(j);
				ArrayList<Dependency> cDepList = Utils.fromFSListToCollection(
						candSent.getSentence().getDependencyList(),
						Dependency.class);
				double depMatchScore = 0.0;
				try {
					//depMatchScore=this.compareDependencies(qDepList, cDepList);
					depMatchScore = this.getScore(qDepList, cDepList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				candSent.setDepMatchScore(depMatchScore);
				candSentList.set(j, candSent);
			}
			FSList fsCandSentList=Utils.fromCollectionToFSList(aJCas, candSentList);
			qaSet.get(i).setCandidateSentenceList(fsCandSentList);
		}
		
		testDoc.setQaList(Utils.fromCollectionToFSList(aJCas, qaSet));

	}

	public double getScore(Sentence target, Sentence candidate)
			throws IOException {
		ArrayList<Dependency> targtList = this
				.getDepencyListFromSentence(target);
		ArrayList<Dependency> candiList = this
				.getDepencyListFromSentence(candidate);

		double score = 0.0;
		int validTarget = 0;
		for (Dependency t : targtList) {
			String rel = t.getRelation();
			rel = rel.replaceFirst("_.*", "");
			if (!tree.containsDep(rel))
				continue;
			if (disco.frequency(t.getGovernor().getText()) == 0
					|| disco.frequency(t.getDependent().getText()) == 0)
				continue;
			if (filter.contains(rel)
					|| (postiveFilter != null && !postiveFilter.contains(rel)))
				continue;
			double max = 0.0;
			validTarget++;
			Dependency maxC = null;
			for (Dependency c : candiList) {
				double tmp = this.getScore(t, c);
				if (tmp > max) {
					maxC = c;
					max = tmp;
				}

			}
			score += max;
			String d1 = t.getRelation() + "(" + t.getGovernor().getText() + ","
					+ t.getDependent().getText() + ")";
			String d2 = maxC.getRelation() + "(" + maxC.getGovernor().getText()
					+ "," + maxC.getDependent().getText() + ")";
			System.out.println(max + " " + d1 + " <---> " + d2);

		}
		if (validTarget == 0)
			return -1;
		return 1.0 * score / validTarget;
	}

	public double getScore(ArrayList<Dependency> targetList,
			ArrayList<Dependency> candiList) throws IOException {

		double score = 0.0;
		int validTarget = 0;
		for (Dependency t : targetList) {
			String rel = t.getRelation();
			rel = rel.replaceFirst("_.*", "");
			if (!tree.containsDep(rel))
				continue;
			if (disco.frequency(t.getGovernor().getText()) == 0
					|| disco.frequency(t.getDependent().getText()) == 0)
				continue;
			if (filter.contains(rel)
					|| (postiveFilter != null && !postiveFilter.contains(rel)))
				continue;
			double max = 0.0;
			validTarget++;
			Dependency maxC = null;
			for (Dependency c : candiList) {
				double tmp = this.getScore(t, c);
				if (tmp > max) {
					maxC = c;
					max = tmp;
				}

			}
			score += max;
			String d1 = t.getRelation() + "(" + t.getGovernor().getText() + ","
					+ t.getDependent().getText() + ")";
			String d2 = maxC.getRelation() + "(" + maxC.getGovernor().getText()
					+ "," + maxC.getDependent().getText() + ")";
			System.out.println(max + " " + d1 + " <---> " + d2);

		}
		if (validTarget == 0)
			return -1;
		return 1.0 * score / validTarget;
	}

	ArrayList<Dependency> getDepencyListFromSentence(Sentence cent) {

		FSList depList = cent.getDependencyList();
		ArrayList<Dependency> dependencyList = new ArrayList<Dependency>();
		int i = 0;
		while (true) {

			Dependency dependency = null;
			try {
				dependency = (Dependency) depList.getNthElement(i);
			} catch (Exception e) {
				break;
			}
			dependencyList.add(dependency);
			i++;
		}

		return dependencyList;
	}

	public double getScore(Dependency parent, Dependency child)
			throws IOException {
		double score1;
		double score2;
		String[] pword = new String[2];
		String[] sword = new String[2];
		pword[0] = parent.getGovernor().getText();
		pword[1] = parent.getDependent().getText();
		sword[0] = child.getGovernor().getText();
		sword[1] = child.getDependent().getText();
		double[][] s = new double[2][2];
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				s[i][j] = Math.max(
						disco.firstOrderSimilarity(pword[i], sword[j]),
						disco.secondOrderSimilarity(pword[i], sword[j]));
			}
		}

		double tmp1 = Math.max(s[0][0], s[0][1]);
		double tmp2 = Math.max(s[1][0], s[1][1]);
		if (tmp1 == -1)
			tmp1 = 0;
		if (tmp2 == -1)
			tmp2 = 0;

		score1 = (tmp1 + tmp2) / 2;
		score2 = tree.getScore(parent.getRelation(), child.getRelation());
		// System.out.println(score1+" "+score2);
		return score1 * score2;
	}
	
	public int compareDependencies(ArrayList<Dependency> qDep,
			ArrayList<Dependency> candDep) throws Exception {
		int cmpCnt = 0;
		for (int i = 0; i < qDep.size(); i++) {
			String qRelation=qDep.get(i).getRelation();
			String qDependent=qDep.get(i).getDependent().getText();
			String qGovernor=qDep.get(i).getGovernor().getText();
			
			for (int j = 0; j < candDep.size(); j++) {
				String cRelation=candDep.get(j).getRelation();
				String cDependent=candDep.get(j).getDependent().getText();
				String cGovernor=candDep.get(j).getGovernor().getText();
				
				if (qRelation.equals(cRelation) && qDependent.equals(cDependent) && qGovernor.equals(cGovernor)) {
					cmpCnt++;
				}
			}
		}

		return cmpCnt;
	}


}
