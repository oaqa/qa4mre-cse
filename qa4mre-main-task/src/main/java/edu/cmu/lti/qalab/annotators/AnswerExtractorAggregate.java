package edu.cmu.lti.qalab.annotators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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

public class AnswerExtractorAggregate extends JCasAnnotator_ImplBase {

	private SolrWrapper solrWrapper;
	HashSet<String> hshStopWords = new HashSet<String>();

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		String serverUrl = (String) context
				.getConfigParameterValue("SOLR_SERVER_URL");
		// String stopFile = (String)
		// context.getConfigParameterValue("SOLR_CORE");
		try {
			this.solrWrapper = new SolrWrapper(serverUrl);
			// loadStopWords(stopFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadStopWords(String fileName) throws Exception {
		BufferedReader bfr = new BufferedReader(new FileReader(fileName));
		String str;

		while ((str = bfr.readLine()) != null) {
			hshStopWords.add(str.trim());
		}
		bfr.close();
		bfr = null;

	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub

		TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
		ArrayList<QuestionAnswerSet> qaSet = Utils.fromFSListToCollection(
				testDoc.getQaList(), QuestionAnswerSet.class);
		int matched = 0;
		int total = 0;
		int unanswered = 0;

		for (int i = 0; i < qaSet.size(); i++) {

			Question question = qaSet.get(i).getQuestion();
			System.out.println("Question: " + question.getText());
			ArrayList<Answer> choiceList = Utils.fromFSListToCollection(qaSet
					.get(i).getAnswerList(), Answer.class);
			ArrayList<CandidateSentence> candSentList = Utils
					.fromFSListToCollection(qaSet.get(i)
							.getCandidateSentenceList(),
							CandidateSentence.class);

			HashMap<String, CandidateAnswer> hshAnswer = new HashMap<String, CandidateAnswer>();
			int topK = Math.min(5, candSentList.size());
			int selectedIdx = 0;
			int correctIdx = 0;
			String correct = "";
			for (int c = 0; c < topK; c++) {

				CandidateSentence candSent = candSentList.get(c);

				ArrayList<NounPhrase> candSentNouns = Utils
						.fromFSListToCollection(candSent.getSentence()
								.getPhraseList(), NounPhrase.class);
				ArrayList<NER> candSentNers = Utils.fromFSListToCollection(
						candSent.getSentence().getNerList(), NER.class);

				double maxScore = Double.NEGATIVE_INFINITY;
				String bestChoice = "";

				for (int j = 0; j < choiceList.size(); j++) {
					double score1 = 0.0;
					double score2 = 0.0;
					Answer answer = choiceList.get(j);
					if (answer.getIsCorrect()) {
						correct = answer.getText();
						correctIdx = j;
					}

					for (int k = 0; k < candSentNouns.size(); k++) {
						try {
							score1 += scoreCoOccurInSameDoc(candSentNouns.get(k)
									.getText(), choiceList.get(j));

							//score2 += scoreCoOccurInSameWindow(candSentNouns
								//	.get(k).getText(), choiceList.get(j), 10);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					for (int k = 0; k < candSentNers.size(); k++) {

						try {
							score1 += scoreCoOccurInSameDoc(candSentNers.get(k)
									.getText(), choiceList.get(j));
							//score2 += scoreCoOccurInSameWindow(candSentNers
								//	.get(k).getText(), choiceList.get(j), 10);
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

					System.out.println(choiceList.get(j).getText() + "\t"
							+ score1 + "\t" + score2+"\t"+((score1+score2)));
					double totalScore = score1 + score2;
					if (totalScore > maxScore) {
						maxScore = totalScore;
						bestChoice = choiceList.get(j).getText();
						selectedIdx = j;
					}
					
					CandidateAnswer candAns = hshAnswer.get(choiceList.get(j).getText());
					if (candAns == null) {
						candAns = new CandidateAnswer(aJCas);
						candAns.setQId(String.valueOf(i));
						candAns.setText(choiceList.get(j).getText());
						candAns.setPMIScore(totalScore);
						candAns.setChoiceIndex(j);
					} else {
						double newScore = candAns.getPMIScore() +maxScore;
						candAns.setPMIScore(newScore);
					}
					hshAnswer.put(choiceList.get(j).getText(), candAns);
				}

				/*CandidateAnswer candAns = hshAnswer.get(bestChoice);
				if (candAns == null) {
					candAns = new CandidateAnswer(aJCas);
					candAns.setQId(String.valueOf(i));
					candAns.setText(bestChoice);
					candAns.setScore(maxScore);
					candAns.setChoiceIndex(selectedIdx);
				} else {
					double newScore = candAns.getScore() +maxScore;
					candAns.setScore(newScore);
				}
				hshAnswer.put(bestChoice, candAns);*/
			}

			CandidateAnswer bestChoice = null;
			try {
				bestChoice = findBestChoice(hshAnswer);
				selectedIdx=bestChoice.getChoiceIndex();
				choiceList.get(bestChoice.getChoiceIndex()).setIsSelected(true);

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out
					.println("Correct Choice: " + correctIdx + "\t" + correct);
			System.out.println("Best Choice: " + selectedIdx + "\t"
					+ bestChoice.getText());
			if (correctIdx == selectedIdx) {
				matched++;

			}
			total++;
			System.out
					.println("================================================");
			FSList fsChoiceList = Utils.fromCollectionToFSList(aJCas,
					choiceList);
			fsChoiceList.addToIndexes();

		}

		System.out.println("Correct: " + matched + "/" + total + "="
				+ ((matched * 100.0) / total) + "%");
		// TO DO: Reader of this pipe line should read from xmi generated by
		// SimpleRunCPE
		double cAt1 = (((double) matched) / ((double) total) * unanswered + (double) matched)
				* (1.0 / total);
		System.out.println("c@1 score:" + cAt1);
	}

	public CandidateAnswer findBestChoice(
			HashMap<String, CandidateAnswer> hshAnswer) throws Exception {

		Iterator<String> it = hshAnswer.keySet().iterator();
		CandidateAnswer candAns = null;
		double maxCount = 0;
		System.out.println("Aggregated counts; ");
		while (it.hasNext()) {
			String key = it.next();
			CandidateAnswer val = hshAnswer.get(key);
			double score = val.getPMIScore();
			System.out.println(key+"\t"+val.getText()+"\t"+score);
			if (score > maxCount) {
				maxCount = score;
				candAns = val;
			}

		}

		return candAns;
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
				//System.out.println(e + "\t" + query);
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
				//System.out.println(e+"\t"+query);
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
		if(choiceNounPhrases.size()>0){
			//score=score/choiceNounPhrases.size();
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

	public double scoreCoOccurInSameWindow(String question, Answer choice,
			int wordWindow) throws Exception {
		double score = 0.0;
		// String choiceTokens[] = choice.split("[ ]");
		ArrayList<NounPhrase> choiceNounPhrases = Utils.fromFSListToCollection(
				choice.getNounPhraseList(), NounPhrase.class);

		for (int i = 0; i < choiceNounPhrases.size(); i++) {
			// score2(choicei) = hits(problem NEAR choicei) / hits(choicei)
			String choiceNounPhrase = choiceNounPhrases.get(i).getText();
			if (question.split("[ ]").length > 1) {
				question = "\"" + question + "\"";
			}
			if (choiceNounPhrase.split("[ ]").length > 1) {
				choiceNounPhrase = "\"" + choiceNounPhrase + "\"";
			}

			String query = "\"" + question + " " + choiceNounPhrase + "\"~"
					+ wordWindow;
			// System.out.println(query);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("q", query);
			params.put("rows", "1");
			SolrParams solrParams = new MapSolrParams(params);
			QueryResponse rsp = null;
			long nearHits = 0;
			try {
				rsp = solrWrapper.getServer().query(solrParams);
				nearHits = rsp.getResults().getNumFound();
			} catch (Exception e) {
				//System.out.println(e+"\t"+query);
			}
			query = "\"" + choice + "\"";
			// System.out.println(query);
			params = new HashMap<String, String>();
			params.put("q", query);
			params.put("rows", "1");
			solrParams = new MapSolrParams(params);
			long nHits = 0;
			try {
				rsp = solrWrapper.getServer().query(solrParams);
				nHits = rsp.getResults().getNumFound();
			} catch (Exception e) {
				//System.out.println(e+"\t"+query);
			}
			if (nHits != 0) {
				score += (double) nearHits / nHits;
			}
		}
		if(choiceNounPhrases.size()>0){
			//score=score/choiceNounPhrases.size();
		}
		return score;
	}

}
