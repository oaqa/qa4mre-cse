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

public class WordSimilarityAnalyzer extends JCasAnnotator_ImplBase {
	class Choice {
		int pivot;
		double score;

		public Choice(int p, double s) {
			pivot = p;
			score = s;
		}

		public int getPivot() {
			return pivot;
		}

		public double getScore() {
			return score;
		}
	}

	class Result {
		int pivot;
		double score;
		ArrayList<Choice> choices;

		public Result(int p, double s) {
			pivot = p;
			score = s;
			choices = new ArrayList<Choice>();
		}

		public int getPivot() {
			return pivot;
		}

		public ArrayList<Choice> getChoices() {
			return choices;
		}

		public void addChoice(Choice c) {
			choices.add(c);
		}

		public double getScore() {
			return score;
		}
	}

	// private StanfordCoreNLP stanfordAnnotator;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		// stanfordAnnotator = new StanfordCoreNLP(props);
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
//		Depsimilar ds = null;
//		try {
//			ds = new Depsimilar("data/similarityModel",
//					"data/dependencyData/dpTree.xml");
//		} catch (Exception e1) {
//			System.out.println("Load Fail");
//			e1.printStackTrace();
//		}
		//NGDSimilarityCalculator sc = new NGDSimilarityCalculator();
		DiscoSimilarity sim = new DiscoSimilarity();
		ArrayList<Sentence> text = Utils.getSentenceListFromSourceDocCAS(jCas);
		ArrayList<QuestionAnswerSet> qasl = Utils
				.getQuestionAnswerSetFromTestDocCAS(jCas);
		int count = 0;
		ArrayList<Result> choice = new ArrayList<Result>();
		for (QuestionAnswerSet qas : qasl) {
			int pivot = 0;
			double maxScore = 0.0;
			System.out.println("Question" + count);
			try {
				//ArrayList<Double> qdis = new ArrayList<Double>();
				String question = qas.getQuestion().getText();
//				Sentence qsent = new Sentence(jCas);
//				qsent.setText(question.getText());
//				qsent.setTokenList(question.getTokenList());
//				qsent.setDependencyList(question.getDependencies());
				for (int i =0; i<text.size(); i++) {
					Sentence sent = text.get(i);
					String line = sent.getText();
					// System.out.println(line);
					double score = sim.getSimilarity(line, question);
					// double score = sc.calculateDistance(line, question);
					// System.out.println(line + "\t" +question+"\t"+ score);
					if (score>maxScore){
						maxScore = score;
						pivot = i;
					}
				}
				Result r = new Result(pivot, maxScore);
				ArrayList<Answer> ansL = Utils.fromFSListToCollection(qas.getAnswerList(),Answer.class);
				//ArrayList<ArrayList<Double>> adisl = new ArrayList<ArrayList<Double>>();
				// int count2 = 0;
				for (int j = 0; j<ansL.size();j++) {
					int pa = 0;
					double maxS = 0.0;
					// System.out.println("answer " + count2++);
					Answer ans = ansL.get(j);
					//ArrayList<Double> adis = new ArrayList<Double>();
					String answer = ans.getText();
//					ansent.setDependencyList(ans.getDependencies());
//					ansent.setTokenList(ans.getTokenList());
//					ansent.setText(ans.getText());
					for (Sentence sent : text) {
						String line = sent.getText();
						double score = sim.getSimilarity(answer, line);
						if (score > maxS) {
							maxS = score;
							pa = j;
						}
					}
					Choice c = new Choice(pa, maxS);
					r.addChoice(c);
				}
				choice.add(r);
			} catch (Exception e) {
				e.printStackTrace();
			}
			count += 1;
		}
		ArrayList<Integer> finalChoice = getChoice(choice);
		for (int i = 0; i<finalChoice.size();i++){
			System.out.println("QuestionPos: "+choice.get(i).pivot+"QuestionSim: "+choice.get(i).score);
			for (Choice ch: choice.get(i).choices){
				System.out.println("AnswerPos: "+ch.pivot+"AnswerSim "+ch.score);
			}
			System.out.println("Question "+(i+1)+"finalChoice:"+finalChoice.get(i));
			System.out.println("----------------------");
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
	public ArrayList<Integer> getChoice(ArrayList<Result> choice){
		ArrayList<Integer> finalChoice = new ArrayList<Integer>();
		for (int i=0;i<choice.size();i++){
			double maxScore = 0.0;
			int c = 0;
			Result r = choice.get(i);
			for (int j=0;j<r.getChoices().size();j++){
				Choice ch = r.getChoices().get(j);
				double tempScore = (ch.getScore()+r.getScore())/(Math.abs(r.getPivot()-ch.getPivot()));
				if (tempScore>maxScore){
					maxScore = tempScore;
					c = j;
				}
			}
			finalChoice.add(c+1);
		}
		return finalChoice;
	}
}
