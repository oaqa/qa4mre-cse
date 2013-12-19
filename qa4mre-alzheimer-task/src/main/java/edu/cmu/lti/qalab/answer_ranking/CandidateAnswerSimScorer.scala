package edu.cmu.lti.qalab.answer_ranking
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase
import org.apache.uima.UimaContext
import org.apache.uima.jcas.JCas
import edu.cmu.lti.qalab.utils.Utils
import scala.math._
import edu.cmu.lti.qalab.types.QuestionAnswerSet
import scala.collection.JavaConverters._
import edu.cmu.lti.qalab.types.CandidateSentence
import edu.cmu.lti.qalab.types.Answer
import edu.cmu.lti.qalab.types.NounPhrase
import edu.cmu.lti.qalab.types.NER
import org.apache.uima.jcas.cas.FSList
import edu.cmu.lti.qalab.types.CandidateAnswer
import edu.cmu.lti.qalab.types.Answer
import edu.cmu.lti.qalab.types.NER
import edu.cmu.lti.qalab.types.NounPhrase
import CandidateAnsScorer._
class CandidateAnswerSimScorer extends CandidateAnsScorer {

  override def getCandidateAnswers(qaSet: QuestionAnswerSet, candSent: CandidateSentence)(cas: JCas) =
    for (choice <- getChoiceList(qaSet)) yield {
      // k <- Range(0, min(numCandidates, getCandidateSentList(q).size))
      val question = qaSet.getQuestion()
      println("Question " + question)
      //get noun lists
      val choiceNpList = choice.getNounPhraseList()
      val sentNpList = candSent.getSentence().getPhraseList()
      //from cas
      val candSentNouns = getFSCollection(sentNpList, classOf[NounPhrase]).map(_.getText)
      val choiceNouns = getFSCollection(choiceNpList, classOf[NounPhrase]).map(_.getText)
      //get ner lists
      val choiceNerList = choice.getNerList()
      val sentNerList = candSent.getSentence().getNerList()
      //from cas
      val candSentNers = getFSCollection(sentNerList, classOf[NER]).map(_.getText)
      val choiceNers = getFSCollection(choiceNerList, classOf[NER]).map(_.getText)
      //compute matches
      val matches =
        countMatches(candSentNouns, choiceNouns) +
          countMatches(candSentNouns, candSentNers) +
          countMatches(candSentNers, choiceNers) +
          countMatches(candSentNers, choiceNouns)

      println(choice.getText() + "\t" + matches)

      val choiceIndex = getCandidateSentList(qaSet).indexOf(candSent)
      val answerList = candSent.getCandAnswerList()
      val candAnswer: CandidateAnswer =
        if (answerList == null)
          new CandidateAnswer(cas)
        else
          getFSCollection(answerList, classOf[CandidateAnswer])(choiceIndex)
      candAnswer.setText(choice.getText())
      candAnswer.setQId(choice.getQuestionId())
      candAnswer.setChoiceIndex(choiceIndex)
      candAnswer.setSimilarityScore(matches)
      candAnswer
    }
}
