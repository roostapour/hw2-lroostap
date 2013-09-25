package edu.cmu.deiis.lroostap.annotators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;

/**
 *
 * ElementAnnotator detects question and answers in the input text.
 * @author Laleh
 *
 */
public class ElementAnnotator extends JCasAnnotator_ImplBase {

  //question pattern
  private Pattern questionPattern = Pattern.compile("Q .+\\?");
  //answer pattern
  private Pattern answerPattern = Pattern.compile("A (0|1) .+\\.");

  /**
   * Generates Question and Answer
   */
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String text = aJCas.getDocumentText();
    String thisProcessorClassName = this.getClass().getName();
    int pos = 0;
    
    //detecting question
    Matcher matcher = questionPattern.matcher(text);
    while (matcher.find(pos)) {
      Question question = new Question(aJCas);
      question.setBegin(matcher.start() + 2);
      pos = matcher.end();
      question.setEnd(pos);
      question.setConfidence(1);
      question.addToIndexes();
      question.setCasProcessorId(thisProcessorClassName);
    }

    //detecting answer
    matcher = answerPattern.matcher(text);
    while (matcher.find(pos)) {
      Answer answer = new Answer(aJCas);
      answer.setBegin(matcher.start() + 4);
      pos = matcher.end();
      answer.setEnd(pos);
      answer.setConfidence(1);
      answer.setIsCorrect(matcher.group(1).equals("1"));
      answer.addToIndexes();
      answer.setCasProcessorId(thisProcessorClassName);
    }

  }

}
