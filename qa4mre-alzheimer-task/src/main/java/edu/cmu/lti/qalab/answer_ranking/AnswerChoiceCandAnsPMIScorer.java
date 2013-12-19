package edu.cmu.lti.qalab.answer_ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
 
import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateAnswer;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class AnswerChoiceCandAnsPMIScorer extends JCasAnnotator_ImplBase {

	private SolrWrapper solrWrapper;
	HashSet<String> hshStopWords = new HashSet<String>();
	int K_CANDIDATES = 5;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		String serverUrl = (String) context
				.getConfigParameterValue("SOLR_SERVER_URL");
		K_CANDIDATES = (Integer) context
				.getConfigParameterValue("K_CANDIDATES");

		try {
			this.solrWrapper = new SolrWrapper(serverUrl);
			// loadStopWords(stopFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
		// String testDocId = testDoc.getId();
		ArrayList<QuestionAnswerSet> qaSet = Utils
				.getQuestionAnswerSetFromTestDocCAS(aJCas);

		for (int i = 0; i < qaSet.size(); i++) {

			Question question = qaSet.get(i).getQuestion();
			System.out.println("Question: " + question.getText());
			ArrayList<Answer> choiceList = Utils.fromFSListToCollection(qaSet
					.get(i).getAnswerList(), Answer.class);
			ArrayList<CandidateSentence> candSentList = Utils
					.fromFSListToCollection(qaSet.get(i)
							.getCandidateSentenceList(),
							CandidateSentence.class);

			int topK = Math.min(K_CANDIDATES, candSentList.size());
			for (int c = 0; c < topK; c++) {

				CandidateSentence candSent = candSentList.get(c);

				ArrayList<NounPhrase> candSentNouns = Utils
						.fromFSListToCollection(candSent.getSentence()
								.getPhraseList(), NounPhrase.class);
				ArrayList<NER> candSentNers = Utils.fromFSListToCollection(
						candSent.getSentence().getNerList(), NER.class);

				ArrayList<CandidateAnswer> candAnsList = new ArrayList<CandidateAnswer>();
				for (int j = 0; j < choiceList.size(); j++) {
					double score1 = 0.0;
					Answer answer = choiceList.get(j);

					for (int k = 0; k < candSentNouns.size(); k++) {
						try {
							score1 += scoreCoOccurInSameDoc(candSentNouns
									.get(k).getText(), choiceList.get(j));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					for (int k = 0; k < candSentNers.size(); k++) {

						try {
							score1 += scoreCoOccurInSameDoc(candSentNers.get(k)
									.getText(), choiceList.get(j));
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

					System.out.println(choiceList.get(j).getText() + "\t"
							+ score1 + "\t" + ((score1)));

					CandidateAnswer candAnswer = null;
					if (candSent.getCandAnswerList() == null) {
						candAnswer = new CandidateAnswer(aJCas);
					} else {
						candAnswer = Utils.fromFSListToCollection(
								candSent.getCandAnswerList(),
								CandidateAnswer.class).get(j);// new
																// CandidateAnswer(aJCas);;
					}
					candAnswer.setText(answer.getText());
					candAnswer.setQId(answer.getQuestionId());
					candAnswer.setChoiceIndex(j);
					candAnswer.setPMIScore(score1);
					candAnsList.add(candAnswer);
				}
				FSList fsCandAnsList = Utils.fromCollectionToFSList(aJCas,
						candAnsList);
				candSent.setCandAnswerList(fsCandAnsList);
				candSentList.set(c, candSent);
			}

			System.out
					.println("================================================");
			FSList fsCandSentList = Utils.fromCollectionToFSList(aJCas,
					candSentList);
			qaSet.get(i).setCandidateSentenceList(fsCandSentList);

		}
		FSList fsQASet = Utils.fromCollectionToFSList(aJCas, qaSet);
		testDoc.setQaList(fsQASet);

	}

	public double scoreCoOccurInSameDoc(String question, Answer choice)
			throws Exception {
		// String choiceTokens[] = choice.split("[ ]");
		ArrayList<NounPhrase> choiceNounPhrases = Utils.fromFSListToCollection(
				choice.getNounPhraseList(), NounPhrase.class);
		double score = 0.0;

		for (int i = 0; i < choiceNounPhrases.size(); i++) {
			// score1(choicei) = hits(problem AND choicei) / hits(choicei)
			String choiceNounPhrase = choiceNounPhrases.get(i).getText();
			if (question.split("[ ]").length > 1) {
				question = "\"" + question + "\"";
			}
			if (choiceNounPhrase.split("[ ]").length > 1) {
				choiceNounPhrase = "\"" + choiceNounPhrase + "\"";
			}

			String query = question + " AND " + choiceNounPhrase;
			// System.out.println(query);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("q", query);
			params.put("rows", "1");
			SolrParams solrParams = new MapSolrParams(params);
			QueryResponse rsp = null;
			long combinedHits = 0;
			try {
				rsp = solrWrapper.getServer().query(solrParams);
				combinedHits = rsp.getResults().getNumFound();
			} catch (Exception e) {
				// System.out.println(e + "\t" + query);
			}

			// System.out.println(query+"\t"+combinedHits);

			query = choiceNounPhrase;
			// System.out.println(query);
			params = new HashMap<String, String>();
			params.put("q", query);
			params.put("rows", "1");
			solrParams = new MapSolrParams(params);

			long nHits1 = 0;
			try {
				rsp = solrWrapper.getServer().query(solrParams);
				nHits1 = rsp.getResults().getNumFound();
			} catch (Exception e) {
				// System.out.println(e+"\t"+query);
			}
			// System.out.println(query+"\t"+nHits1);

			/*
			 * query = question; // System.out.println(query); params = new
			 * HashMap<String, String>(); params.put("q", query);
			 * params.put("rows", "1"); solrParams = new MapSolrParams(params);
			 * rsp = solrWrapper.getServer().query(solrParams); long nHits2 =
			 * rsp.getResults().getNumFound(); //
			 * System.out.println(query+"\t"+nHits2);
			 */

			// score += myLog(combinedHits, nHits1, nHits2);
			if (nHits1 != 0) {
				score += (double) combinedHits / nHits1;
			}
		}
		if (choiceNounPhrases.size() > 0) {
			// score=score/choiceNounPhrases.size();
		}
		return score;
	}

	public double myLog(long combined, long nHits1, long nHits2) {
		if (combined == 0 || nHits1 == 0 || nHits2 == 0) {
			return 0;
		}
		double logValue = Math.log(combined) - Math.log(nHits1)
				- Math.log(nHits2);
		return logValue;
	}

}
