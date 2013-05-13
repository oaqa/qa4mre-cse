package edu.cmu.lti.qalab.annotators;

import java.util.ArrayList;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.utils.Utils;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class NaiveAnalyzer extends JCasAnnotator_ImplBase {

	private StanfordCoreNLP stanfordAnnotator;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		stanfordAnnotator = new StanfordCoreNLP(props);
	}

	public Object getAnnotationObject(JCas jCas, int type) {

		FSIterator fsIt = jCas.getAnnotationIndex(type).iterator();
		Object obj = null;
		if (fsIt.hasNext()) {
			obj = fsIt.next();
		}
		return obj;
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		NGDSimilarityCalculator sc = new NGDSimilarityCalculator();
		ArrayList<Sentence> text = Utils.getSentenceListFromSourceDocCAS(jCas);
		ArrayList<QuestionAnswerSet> qasl = Utils
				.getQuestionAnswerSetFromTestDocCAS(jCas);
		int count = 0;
		for (QuestionAnswerSet qas : qasl) {
			System.out.println("really " + count++);
			try {
				ArrayList<Double> qdis = new ArrayList<Double>();
				String question = qas.getQuestion().getText();
				for (Sentence sent : text) {
					String line = sent.getText();
					String [] qword = question.split(" ");
					String [] sword = line.split(" ");
					double score = 0.0;
					for (String q:qword){
						for (String s:sword){
							double d = sc.calculateDistance(q, s);
							//System.out.println(q + "\t" +s+"\t"+ d);
							score+=d;
						}
					}
					//double score = sc.calculateDistance(line, question);
					//System.out.println(line + "\t" +question+"\t"+ score);
					qdis.add(score);
				}
				ArrayList<Answer> ansL = Utils.fromFSListToCollection(qas.getAnswerList(),Answer.class);
				ArrayList<ArrayList<Double>> adisl = new ArrayList<ArrayList<Double>>();
				int count2 = 0;
				for (Answer ans : ansL) {
					System.out.println("answer " + count2++);
					ArrayList<Double> adis = new ArrayList<Double>();
					String answer = ans.getText();
					for (Sentence sent : text) {
						double score = 0.0;
						String line = sent.getText();
						String [] aword = answer.split(" ");
						String [] sword = line.split(" ");
						for (String a:aword){
							for (String s:sword){
								double d = sc.calculateDistance(a, s);
								System.out.println(a + "\t" +s+"\t"+ d);
								score+=d;
							}
						}
						//double score = sc.calculateDistance(line, answer);
						System.out.println(line + "\t" +answer+"\t"+ score);
						adis.add(score);
					}
					adisl.add(adis);
				}
				int choice = getOneChoice(qdis, adisl);
				System.out.println("Final choice for this question:" + choice);
			} catch (Exception e) {
				System.out.println("");
			}
		}
	}

	public int getOneChoice(ArrayList<Double> qdis,
			ArrayList<ArrayList<Double>> adisl) {
		double[] score = new double[4];
		for (int i = 0; i <= qdis.size() - 1; i++) {
			for (int j = 0; j <= adisl.size() - 1; j++) {
				ArrayList<Double> adis = adisl.get(j);
				for (int k = 0; k <= adis.size() - 1; k++) {
					double s = (qdis.get(i) + adis.get(k)) * Math.abs(i - k);
					score[j] = Math.max(score[j], s);
				}
			}
		}
		double max = 0;
		int answer = 0;
		for (int i = 0; i <= 3; i++) {
			if (score[i] > max) {
				answer = i + 1;
				max = score[i];
			}
		}
		return answer;
	}
}
