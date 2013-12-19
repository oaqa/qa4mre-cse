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
import scala.collection.mutable.Buffer
import CandidateAnsScorer._
import java.util.Collection
class CandidateAnsScorer extends JCasAnnotator_ImplBase {

  var numCandidates: Int = 0

  final override def initialize(context: UimaContext) = {
    super.initialize(context)
    numCandidates = context.getConfigParameterValue("K_CANDIDATES").asInstanceOf[Int]
  }

  def getCandidateAnswers(qaSet: QuestionAnswerSet, candSent: CandidateSentence)(cas: JCas): Buffer[CandidateAnswer] = ???

  final override def process(cas: JCas) = {
    val testDoc = Utils.getTestDocumentFromCAS(cas)
    val qaSets = Utils.getQuestionAnswerSetFromTestDocCAS(cas).asScala
    qaSets.foreach(qaSet => {
      val candSents = for (candSent <- getCandidateSentList(qaSet).take(numCandidates)) yield {
        val candAnswers = getCandidateAnswers(qaSet, candSent)(cas)
        val fsCandAnsList = getCollectionFromFS(cas, candAnswers)
        candSent.setCandAnswerList(fsCandAnsList)
        candSent
      }
      println("================================================");
      val fsCandSentList = Utils.fromCollectionToFSList(cas, candSents.asJava)
      qaSet.setCandidateSentenceList(fsCandSentList)
    })

    val fsQASet = Utils.fromCollectionToFSList(cas, qaSets.asJava);
    testDoc.setQaList(fsQASet);
  }
}

object CandidateAnsScorer {
  def getFSCollection[C <: org.apache.uima.jcas.cas.TOP](fsList: FSList, cls: Class[C]) =
    Utils.fromFSListToCollection(fsList, cls).asScala

  def getCollectionFromFS[C <: org.apache.uima.jcas.tcas.Annotation](cas: JCas, col: Buffer[C]) =
    Utils.fromCollectionToFSList(cas, col.asJava)

  def getCandidateSentList(qaSet: QuestionAnswerSet) =
    getFSCollection(qaSet.getCandidateSentenceList, classOf[CandidateSentence])
  def countMatches(l1: Traversable[String], l2: Traversable[String]) =
    {
      for {
        x <- l1
        y <- l2
      } yield {
        val max = Set(x, y).maxBy(_.length)
        val min = Set(x, y).filterNot(_ == max)
        if (max.contains(min)) 1 else 0
      }
    }.sum

  def getChoiceList(q: QuestionAnswerSet) =
    getFSCollection(q.getAnswerList(), classOf[Answer])
}