/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nb_classifier;

/**
 *
 * @author David Klecker
 */

import NB_Classifier.FeatureExtractor;
import NB_Classifier.FeatureExtractor.TokenObjectClass;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NB_Classifier {

    public static int m_TotalNumberOfClasses;
    public static int m_TotalNumberofDocuments;
    public static ArrayList<ProbabilityOfClass> pProbabilityOfClassVector;
    public static ArrayList<FeatureExtractor> pFeatures;

        
    public static class ProbabilityOfClass{
        String m_ClassName;
        double m_Probability;
        int m_NumberOfDocumentsTrainedForClass;

        public ProbabilityOfClass(String ClassName){
            m_ClassName = ClassName;
            m_Probability = 0.0;
            m_NumberOfDocumentsTrainedForClass = 0;
        }
    }   

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        String Class_Politics_Guns      = "./20_newsgroups/talk.politics.guns";
        String Class_Politics_Mideast   = "./20_newsgroups/talk.politics.mideast";
        String Class_Politics_Misc      = "./20_newsgroups/talk.politics.misc";
        pFeatures                       = new ArrayList<FeatureExtractor>();
        pProbabilityOfClassVector       = new ArrayList<ProbabilityOfClass>();        
        m_TotalNumberofDocuments        = 0;
        
        m_TotalNumberofDocuments += TrainClass(Class_Politics_Guns, "Class Politics.Guns");
        m_TotalNumberOfClasses++;
        
        m_TotalNumberofDocuments += TrainClass(Class_Politics_Mideast, "Class Politics.Mideast");
        m_TotalNumberOfClasses++;

        m_TotalNumberofDocuments += TrainClass(Class_Politics_Misc, "Class Politics.Misc");
        m_TotalNumberOfClasses++;

        BuildProbabilityOfClass();        

        BuildProbabilityOfEvidence();
        
        System.out.println("Total Number of Documents: "+m_TotalNumberofDocuments);
        System.out.println("Total Number of Classes: "+m_TotalNumberOfClasses);
        System.out.println("Done!");
    }
    
    public static void BuildProbabilityOfClass(){
        
        pProbabilityOfClassVector.forEach((vector) -> {
            double prob = (double)vector.m_NumberOfDocumentsTrainedForClass / (double)m_TotalNumberofDocuments;
            vector.m_Probability = prob;
        });
    }
        
    public static void BuildProbabilityOfEvidence(){
        HashMap<String, TokenObjectClass> FullTokenMap = new HashMap<String, TokenObjectClass>();
        
        pFeatures.forEach((feature) -> {
            feature.Vector.forEach((featureVector) -> {
                HashMap<String, TokenObjectClass> TokenMap = featureVector.m_TokenMap;
                Set set = TokenMap.entrySet();
                                
                Iterator iterator = set.iterator();
                while(iterator.hasNext()) {
                    Map.Entry mentry    = (Map.Entry)iterator.next();
                    System.out.println(mentry.toString());
                    if(FullTokenMap.containsKey(mentry.getKey())){
                        TokenObjectClass token = (TokenObjectClass)FullTokenMap.get(mentry.getKey());
                        System.out.println(token.GetWord());
                        System.out.println(token.GetCount());
                        token.SetCount(token.GetCount() + 1);
                    }
                    else{
                        TokenObjectClass pToken = new TokenObjectClass((String)mentry.getKey(), 1);
                        
                        FullTokenMap.put((String)mentry.getKey(), pToken);
                   }
                }
            });
        });
        
        HashMap<String, TokenObjectClass> SortedFullTokenMap = FeatureExtractor.sortByValues(FullTokenMap);

        int NumberOfWordsInFullBag = FullTokenMap.size();
        Set set = SortedFullTokenMap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry    = (Map.Entry)iterator.next();
            TokenObjectClass token = (TokenObjectClass)mentry.getValue();
            double probabilityofevidence = (double)token.GetCount() / (double)NumberOfWordsInFullBag;
            token.SetProbability((float)probabilityofevidence);
            
            System.out.printf ("Word:%-15s existed in %-4d of the documents. Prob:%-5.3f\n", token.GetWord(), token.GetCount(), token.GetProbability());
        }
    }
    
    public static int TrainClass(String ClassName, String ClassDescription) throws IOException {
            
        FeatureExtractor pFeatureExtractor          = new FeatureExtractor(ClassName);
        //pFeatureExtractor.m_ClassOfDocuments      = ClassDescription;
        ProbabilityOfClass pClass                   = new ProbabilityOfClass(ClassDescription);
        int NumberOfDocumentsThisClass              = pFeatureExtractor.TrainWithDocuments();
        pClass.m_NumberOfDocumentsTrainedForClass   = NumberOfDocumentsThisClass;

        pProbabilityOfClassVector.add(pClass);
        pFeatures.add(pFeatureExtractor);
        return NumberOfDocumentsThisClass;
    }
    
}
