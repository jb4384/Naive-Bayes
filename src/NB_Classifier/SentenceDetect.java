/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NB_Classifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
 
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
 
/**
 * Sentence Detection in openNLP using Java
 */
public class SentenceDetect {
 
    /**
     * This method is used to detect sentences in a paragraph/string
     * @param paragraph
     * @return sentences
     * @throws IOException
     */
    public String[] sentenceDetect(String paragraph) throws IOException { 
        try ( // refer to model file "en-sent,bin", available at link http://opennlp.sourceforge.net/models-1.5/
                InputStream is = new FileInputStream("en-sent.bin")) {
            SentenceModel model = new SentenceModel(is);
            
            // feed the model to SentenceDetectorME class
            SentenceDetectorME sdetector = new SentenceDetectorME(model);
            
            // detect sentences in the paragraph
            String sentences[] = sdetector.sentDetect(paragraph);
            return sentences;
        }
    }
}