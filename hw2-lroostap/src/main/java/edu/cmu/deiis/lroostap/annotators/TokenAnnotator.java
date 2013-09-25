package edu.cmu.deiis.lroostap.annotators;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.*;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

/**
 * 
 * TokenAnnotator uses stanford tokenizer to generate tokens.
 * @author Laleh
 *
 */
public class TokenAnnotator extends JCasAnnotator_ImplBase {

  private TokenizerFactory<Word> factory = PTBTokenizerFactory.newTokenizerFactory();
  
  /*
   * (non-Javadoc)
   * @see org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
   */
  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    
    Iterator<Annotation> sentenceIter = aJCas.getAnnotationIndex(Sentence.type).iterator();
    while (sentenceIter.hasNext()) {
      try {
        
        Sentence sentence = (Sentence) sentenceIter.next();
        FSArray tokens = tokenize(aJCas, sentence);
        sentence.setTokens(tokens);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
  
  }
  
  
  /**
   * Calls the tokenizer and creates an array of Tokens for each question or answer 
   * @param aJCas
   * @param annotationClass
   * @return an FSArray of Tokens
   * @throws Exception
   */
  
  private FSArray tokenize(JCas aJCas, Annotation annotationClass) throws Exception {
       
    ArrayList<Token> tokens = new ArrayList<Token>();
    String annotatedText = annotationClass.getCoveredText();
    int annotatedTextBegin = annotationClass.getBegin();
    Tokenizer<Word> tokenizer = factory.getTokenizer(new StringReader(annotatedText));
    for (Word word : tokenizer.tokenize()) {
      int wordBegin = annotatedTextBegin + word.beginPosition();
      int wordEnd = annotatedTextBegin + word.endPosition();

      Token tok = new Token(aJCas);
      tok.addToIndexes();
      tok.setBegin(wordBegin);
      tok.setEnd(wordEnd);
      tok.setCasProcessorId(this.getClass().getName());
      tok.setConfidence(1);
      tokens.add(tok);
    }
    FSArray tokenFSArray = new FSArray(aJCas, tokens.size());
    for (int i = 0; i < tokens.size(); ++i) {
      tokenFSArray.set(i, tokens.get(i));
    }
    return tokenFSArray;
   
  }


}