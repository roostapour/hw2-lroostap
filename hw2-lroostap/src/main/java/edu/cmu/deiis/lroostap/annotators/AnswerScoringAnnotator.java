package edu.cmu.deiis.lroostap.annotators;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.*;

/**
 * 
 * Calculates score for each answer based on the Ngram matching algorithm and prints the results.
 * @author Laleh
 *
 */
public class AnswerScoringAnnotator extends JCasAnnotator_ImplBase {

  /**
   * Calculates score for each answer based on an Ngram matching algorithm.
   */
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {

    FSIterator<Annotation> nGramIter = aJCas.getAnnotationIndex(NGram.type).iterator();
    /**
     * creating a HashMap to link a question/answer to a list of NGrams 
     */
    HashMap<Annotation, ArrayList<NGram>> nGramHashMap = new HashMap<Annotation, ArrayList<NGram>>();
    Annotation question = null;

    while (nGramIter.hasNext()) {
        NGram nGram = (NGram) nGramIter.next();
        Annotation owner = nGram.getOwner();
        if (nGramHashMap.get(owner) == null) {
          nGramHashMap.put(owner, new ArrayList<NGram>());
        }
        nGramHashMap.get(owner).add(nGram);
        if (question == null && owner.getClass() == Question.class) {
          question = owner;
        }
      
    }

    ArrayList<NGram> questionNGramArrayList = nGramHashMap.get(question);
    Set<Annotation> annotationKeyList = nGramHashMap.keySet();
    annotationKeyList.remove(question);
    
    /**
     * A simple NGram matching technique between a question and an answer
     */
    ArrayList<AnswerScore> answerScoreList = new ArrayList<AnswerScore>();
    for (Annotation answer : annotationKeyList) {
      float numberOfMatches = (float) 0;
      float totalNumber = (float) 0;
      for (NGram answerNGram : nGramHashMap.get(answer)) {
        totalNumber = totalNumber + answerNGram.getN();
        for (NGram questionNGram : questionNGramArrayList) {
          if (questionNGram.getCoveredText().equals(answerNGram.getCoveredText())) {
            numberOfMatches = numberOfMatches + answerNGram.getN();
            break;
          }
        }
      }
      
      /**
       * Creating an AnswerScore and set the variables
       */
      AnswerScore ansScore = new AnswerScore(aJCas);
      ansScore.setCasProcessorId(this.getClass().getName());
      ansScore.setAnswer((Answer) answer);
      ansScore.setBegin(answer.getBegin());
      ansScore.setEnd(answer.getEnd());
      ansScore.addToIndexes();
      ansScore.setConfidence(1);
      ansScore.setScore((float) numberOfMatches / totalNumber);
      answerScoreList.add(ansScore);
    }
      
    /**
     * printing the result
     */
    try {
      printResult(question, answerScoreList);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  
  
  /**
   * This method prints the question, answers, the assigned scores to each answer, and the precision.
   * @param question
   * @param scoreList
   * @throws Exception
   */
  private void printResult(Annotation question, ArrayList<AnswerScore> scoreList) throws Exception {   
  
  /**
   * Sorting the answerScoreList according to the score
   */
    Comparator<AnswerScore> cmp = new Comparator<AnswerScore>() {
      public int compare(AnswerScore a, AnswerScore b) {
        return -Double.compare(a.getScore(), b.getScore());
      }
    };
    Collections.sort(scoreList, cmp);
   
    /**
     * printing the question
     */
    System.out.print("Question: " + question.getCoveredText() + "\n");

    
    /**
     * printing the answers with their scores
     */
    for (AnswerScore score : scoreList) {
      String isCorrect = "";
      if (score.getAnswer().getIsCorrect()){
        isCorrect = "+";
      }else{
        isCorrect = "-";
      }
      System.out.format("%s %.3f %s\n", isCorrect, score.getScore(), score.getCoveredText());
    }
    
    
    /**
     * calculating the precision at n where n is the number of correct answers
     */
    int correctAnswers = 0;
    for (int i = 0; i < scoreList.size(); ++i) {
      if (scoreList.get(i).getAnswer().getIsCorrect())
        ++correctAnswers;
    }
    
    int correctAnswerinN = 0;
    for (int i = 0; i < correctAnswers; ++i) {
      if (scoreList.get(i).getAnswer().getIsCorrect())
        ++correctAnswerinN;
    }
    System.out.format("Precision at %d : %.5f%n%n\n", correctAnswers, ((float) correctAnswerinN / correctAnswers));
    
  }

}
