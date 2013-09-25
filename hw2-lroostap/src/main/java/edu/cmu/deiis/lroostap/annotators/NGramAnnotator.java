package edu.cmu.deiis.lroostap.annotators;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.deiis.types.*;


/**
 * NGramAnnotator detects NGrams (1, 2, 3) for question and answers. 
 * @author Laleh
 *
 */

public class NGramAnnotator extends JCasAnnotator_ImplBase {

  /**
   * @param N : keeps the size on N in NGrams
   *
   */
  @Override
  public void process(JCas aJCas){
    /**
     * considering n = 1,2,3 unigram, bigram, trigram
     */
    for (int n = 1; n < 4; ++n) {
        try {
          Iterator<Annotation> sentenceIter = aJCas.getAnnotationIndex(Sentence.type).iterator();
          
          while (sentenceIter.hasNext()) {
            Sentence sentence = (Sentence) sentenceIter.next();
            FSArray tokens = (FSArray) sentence.getTokens();
            int k = tokens.size() + 1 - n;

            for (int i = 0; i < k; ++i) {
              NGram annotatedNgram = new NGram(aJCas);
              annotatedNgram.setN(n);
              annotatedNgram.setCasProcessorId(this.getClass().getName());
              annotatedNgram.setConfidence(1);
              annotatedNgram.addToIndexes();
              annotatedNgram.setElementType(Token.class.getName());
              annotatedNgram.setOwner(sentence);

              ArrayList<Token> tokenList = new ArrayList<Token>();
              for (int j = 0; j < n; ++j) {
                tokenList.add((Token) tokens.get(i + j));
              }
              
              annotatedNgram.setBegin(tokenList.get(0).getBegin());
              annotatedNgram.setEnd(tokenList.get(n - 1).getEnd());
              FSArray annotatedTokens = new FSArray(aJCas, n);
              for (int j = 0; j < n; ++j) {
                annotatedTokens.set(j, tokenList.get(j));
              }
              annotatedNgram.setElements(annotatedTokens);
            }
          }
                   
        } catch (Exception e) {
          e.printStackTrace();
        }
      
    }
  }

}
