package edu.cmu.lti.qalab.annotators;

import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import abner.Tagger;
import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.utils.Utils;

public class QuestionNEAnnotator extends JCasAnnotator_ImplBase {

	Tagger abnerTagger = null;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		abnerTagger = new Tagger(Tagger.BIOCREATIVE);

	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		// TestDocument testDoc=Utils.getTestDocumentFromCAS(jCas);

		ArrayList<Question> questionList = Utils
				.getQuestionListFromTestDocCAS(jCas);
		ArrayList<ArrayList<Answer>> answerList = Utils
				.getAnswerListFromTestDocCAS(jCas);

		for (int i = 0; i < questionList.size(); i++) {
			Question question = questionList.get(i);
			String nerTagged = abnerTagger.tagABNER(question.getText());

			// System.out.println(nerTagged);
			ArrayList<NER> abnerList = new ArrayList<NER>();
			try {
				abnerList = this.extractNER(nerTagged, jCas);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ArrayList<NER> stanfordNerList = this.extractStanfordNER(question, jCas);
				if (stanfordNerList.size() > 0) {
					abnerList.addAll(stanfordNerList);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			FSList fsNERList = Utils.createNERList(jCas, abnerList);
			question.setNerList(fsNERList);
			question.addToIndexes();

		}

		for (int i = 0; i < answerList.size(); i++) {
			ArrayList<Answer> choiceList = answerList.get(i);

			for (int j = 0; j < choiceList.size(); j++) {
				Answer ans = choiceList.get(j);
				String nerTagged = abnerTagger.tagABNER(ans.getText());

				// System.out.println(nerTagged);
				ArrayList<NER> abnerList = new ArrayList<NER>();
				try {
					abnerList = this.extractNER(nerTagged, jCas);
				} catch (Exception e) {
					
					e.printStackTrace();
				}
				try {
					ArrayList<NER> stanfordNerList = this.extractStanfordNER(ans, jCas);
					if (stanfordNerList.size() > 0) {
						abnerList.addAll(stanfordNerList);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				FSList fsNERList = Utils.createNERList(jCas, abnerList);
				ans.setNerList(fsNERList);
				ans.addToIndexes();
				choiceList.set(j, ans);
			}

		}

	}

	public ArrayList<NER> extractStanfordNER(Question sentence, JCas jCas)
			throws Exception {
		ArrayList<NER> nerList = new ArrayList<NER>();
		ArrayList<Token> tokenList = Utils
				.getTokenListFromQuestion(sentence);
		// Ram/O goes/O to/O Carnegie/ORGANIZATION Mellon/ORGANIZATION
		// University/ORGANIZATION
		String ner = "";
		String type = "";
		for (int i = 0; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			if (!token.getNer().equals("O")) {
				if (!type.equals(token.getNer())) {
					ner = "";
					type = "";
					ner = token.getText();
					type = token.getNer();
				}else{
					ner += token.getText();
					type = token.getNer();
				}
			} else {
				if (!type.equals("") ){
					NER ne = new NER(jCas);
					ne.setText(ner);
					ne.setTag(type);
					ne.setWeight(1.0);
					nerList.add(ne);
				}
				ner = "";
				type = "";
			}
		}
		return nerList;
	}

	public ArrayList<NER> extractStanfordNER(Answer sentence, JCas jCas)
			throws Exception {
		ArrayList<NER> nerList = new ArrayList<NER>();
		ArrayList<Token> tokenList = Utils
				.getTokenListFromAnswer(sentence);
		// Ram/O goes/O to/O Carnegie/ORGANIZATION Mellon/ORGANIZATION
		// University/ORGANIZATION
		String ner = "";
		String type = "";
		for (int i = 0; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			if (!token.getNer().equals("O")) {
				if (!type.equals(token.getNer())) {
					ner = "";
					type = "";
					ner = token.getText();
					type = token.getNer();
				}else{
					ner += token.getText();
					type = token.getNer();
				}
			} else {
				if (!type.equals("") ){
					NER ne = new NER(jCas);
					ne.setText(ner);
					ne.setTag(type);
					ne.setWeight(1.0);
					nerList.add(ne);
				}
				ner = "";
				type = "";
			}
		}
		return nerList;
	}

	public ArrayList<NER> extractNER(String tagged, JCas jCas) throws Exception {

		ArrayList<NER> nerList = new ArrayList<NER>();
		String words[] = tagged.split("[ ]");
		String ner = "";
		String type = "";
		for (int i = 0; i < words.length; i++) {
			words[i] = words[i].trim();
			if (words[i].equals("")) {
				continue;
			}
			String rec[] = words[i].split("[|]");
			if (!rec[1].endsWith("O")) {
				if (rec[1].startsWith("B-")) {
					ner = ner.trim();
					if (!ner.equals("")) {
						NER ne = new NER(jCas);
						ne.setText(ner);
						ne.setTag(type);
						nerList.add(ne);
						// System.out.println(ner + "\t" + type);
					}

					ner = "";
					ner += rec[0] + " ";
					type = rec[1];
				} else {
					ner += rec[0] + " ";
				}
			}
		}
		ner = ner.trim();
		if (!ner.trim().equals("")) {
			NER ne = new NER(jCas);
			ne.setText(ner);
			ne.setTag(type);
			nerList.add(ne);
			// System.out.println(ner + "\t" + type);
		}
		return nerList;
	}

}
