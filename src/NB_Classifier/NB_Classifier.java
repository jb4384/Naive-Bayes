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
import NB_Classifier.JsonHandler;
import NB_Classifier.ModelLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NB_Classifier{

    public static int m_TotalNumberOfClasses;
    public static int m_TotalNumberofDocuments;
    public static ArrayList<FeatureExtractor> pFeatures;
    public static ModelLoader model;        
    public static HashMap<String, TokenObjectClass> FullTokenMap;
    public static AccuracyClass Accuracy;

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        model = new ModelLoader();
        Accuracy = new AccuracyClass(0,0,0,0);
        
        String Class_Politics_Guns      = "./20_newsgroups_Train/talk.politics.guns";
        String Class_Politics_Mideast   = "./20_newsgroups_Train/talk.politics.mideast";
        String Class_Politics_Misc      = "./20_newsgroups_Train/talk.politics.misc";

        String Class_Politics_Guns_Test     = "./20_newsgroups_Test/talk.politics.guns";
        String Class_Politics_Mideast_Test  = "./20_newsgroups_Test/talk.politics.mideast";
        String Class_Politics_Misc_Test     = "./20_newsgroups_Test/talk.politics.misc";

        pFeatures                       = new ArrayList<>();
        
        TrainClass(Class_Politics_Guns, "Class Politics.Guns");
        
        TrainClass(Class_Politics_Mideast, "Class Politics.Mideast");

        TrainClass(Class_Politics_Misc, "Class Politics.Misc");

        BuildProbabilityOfClass(); //testing       

        BuildProbabilityOfEvidence();
        
        //Let's test this to see how well it can classify articles into guns
        TestClassifier(Class_Politics_Guns_Test, "Class Politics.Guns", true);
        TestClassifier(Class_Politics_Guns_Test, "Class Politics.Mideast", false);
        TestClassifier(Class_Politics_Guns_Test, "Class Politics.Misc", false);
        TestClassifier(Class_Politics_Mideast_Test, "Class Politics.Mideast", true);
        TestClassifier(Class_Politics_Mideast_Test, "Class Politics.Guns", false);
        TestClassifier(Class_Politics_Mideast_Test, "Class Politics.Misc", false);
        TestClassifier(Class_Politics_Misc_Test, "Class Politics.Misc", true);
        TestClassifier(Class_Politics_Misc_Test, "Class Politics.Guns", false);
        TestClassifier(Class_Politics_Misc_Test, "Class Politics.Mideast", false);

        System.out.println("Total Accuracy :" + Accuracy.TotalAccuracy());
        System.out.println("Sensitivity :" + Accuracy.GetSensitivity());
        System.out.println("Specificity :" + Accuracy.GetSpecifity());
        System.out.println("Precision: "+Accuracy.GetPrecision());
        System.out.println("Recall: "+Accuracy.GetRecall());
        System.out.println("True Positive: "+Accuracy.m_TruePositive);
        System.out.println("True Negative: "+Accuracy.m_TrueNegative);
        System.out.println("False Positive: "+Accuracy.m_FalsePositive);
        System.out.println("False Negative: "+Accuracy.m_FalseNegative);
        System.out.println("Done!");
    }
            
    public static void TrainClass(String ClassPath, String ClassDescription) throws IOException {
        FeatureExtractor pFeatureExtractor          = new FeatureExtractor(ClassPath, ClassDescription, model);
        m_TotalNumberofDocuments += pFeatureExtractor.TrainWithDocuments();

        pFeatures.add(pFeatureExtractor);
    }
    
    public static void TestClassifier(String ClassPath, String ClassDescription, boolean TrueHypothesis) throws IOException {
        FeatureExtractor pFeatureExtractor          = new FeatureExtractor(ClassPath, ClassDescription, model);
        pFeatureExtractor.TrainWithDocuments();
                
        for(int i=0; i<pFeatureExtractor.Vector.size(); i++){
            
            FeatureExtractor.DocumentObjectClass pDoc = pFeatureExtractor.Vector.get(i);
            
            double MaxProbability = 0.0;
            String LikelyClass = "";
        
            //Go through each of the features we created with the three training classes. A pLocalFeature object is a class. 
            for(int j=0; j<pFeatures.size(); j++){
            
                double Total    = 1.0;
                double TotalPOE = 1.0;

                //Get the Class which contains the bag of words for this class and the PoL calculations. We want the full bag, not the individual bag
                FeatureExtractor pLocalFeature = pFeatures.get(j);
                HashMap pLocalTokenMap = pLocalFeature.FullDocumentBagOfWords.m_TokenMap;
                
                Set set = pDoc.m_TokenMap.entrySet();
                Iterator iterator = set.iterator();
            
                //For doc get the token bag we created. 
                while(iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry)iterator.next();

                    //Locate the token in the class using the same token word as a key to search. 
                    TokenObjectClass pFoundToken = (TokenObjectClass) pLocalTokenMap.get(mentry.getKey());
                    
                    //TokenObjectClass pFoundTokenForPoE = (TokenObjectClass) FullTokenMap.get(mentry.getKey());
                    
                    //It may find it. If it does we can get the PoL calculation. 
                    if(pFoundToken != null){
                        Total *= (double) pFoundToken.GetPoL();
                        //System.out.println("P("+(String)mentry.getKey()+"|"+pLocalFeature.m_ClassOfDocuments+") = "+(double) pFoundToken.GetPoL());
                    }
                    //else
                        //Total *= 0.0;

                    //if(pFoundTokenForPoE != null){
                    //    TotalPOE *= (double) pFoundTokenForPoE.GetPoE();
                    //    System.out.println("P("+(String)mentry.getKey()+") = "+(double) pFoundTokenForPoE.GetPoE());
                    //}
                }

                Total *= pLocalFeature.ProbabilityOfClass;
                
               //Total = Total / TotalPOE;

                System.out.println("Class: "+pLocalFeature.m_ClassOfDocuments+":"+Total +">"+MaxProbability);
                
                if(Total > MaxProbability){
                    MaxProbability = Total;
                    LikelyClass = pLocalFeature.m_ClassOfDocuments;
                }
            }
            
            System.out.println(LikelyClass);
            
            if(TrueHypothesis){
                if(LikelyClass.equals(ClassDescription))
                    Accuracy.m_TruePositive++;
                else
                    Accuracy.m_FalseNegative++;
            }
            else{
                if(LikelyClass.equals(ClassDescription))
                    Accuracy.m_FalsePositive++;
                else
                    Accuracy.m_TrueNegative++;
            }
        }
    }

    public static void BuildProbabilityOfClass(){
        
        pFeatures.forEach((vector) -> {
            double prob = (double)vector.Vector.size() / (double)m_TotalNumberofDocuments;
            vector.ProbabilityOfClass = prob;
        });
    }
        
    public static void BuildProbabilityOfEvidence() throws IOException{
        
        FullTokenMap = new HashMap<String, TokenObjectClass>();
        int TotalNumberOfDocuments = 0;
        
        for(int i=0; i<pFeatures.size(); i++){
            FeatureExtractor feature = pFeatures.get(i);
            TotalNumberOfDocuments += feature.Vector.size();
        
            //System.out.println("\nShowing Bags of Words for document " + feature.m_ClassOfDocuments);
            Set set             = feature.FullDocumentBagOfWords.m_TokenMap.entrySet();
            Iterator iterator   = set.iterator();
            
            JsonHandler json = new JsonHandler();
           while(iterator.hasNext()) {
                Map.Entry mentry    = (Map.Entry)iterator.next();
                TokenObjectClass token = (TokenObjectClass) mentry.getValue();
                
                TokenObjectClass tokenLocal;
                
                if(FullTokenMap.containsKey(token.GetWord())){
                    tokenLocal = (TokenObjectClass)FullTokenMap.get(token.GetWord());
                    tokenLocal.SetCount(tokenLocal.GetCount() + token.GetCount());
                    tokenLocal.SetTermFrequency(tokenLocal.GetTermFrequency() + token.GetTermFrequency());
                  }
                else{
                    tokenLocal = feature.CreateNewTokenObject((String)mentry.getKey(), token.GetCount());
                    tokenLocal.SetTermFrequency(token.GetTermFrequency());
                    
                    FullTokenMap.put((String)mentry.getKey(), tokenLocal);
                }
                json.addBuilder(tokenLocal.getJsonObject());
                    
                //System.out.printf ("Word: %-15s exists in %-4d/%-4d of the documents. PoL:%-5.3f: %-15s\n", token.GetWord(), token.GetCount(), feature.Vector.size(), token.GetPoL(), feature.m_ClassPath);
            }
            json.build();
            json.write("./json/",(feature.m_ClassOfDocuments).split("\\s+")[1]);
        }

        HashMap<String, TokenObjectClass> SortedFullTokenMap = FeatureExtractor.sortByValues(FullTokenMap);

        JsonHandler json = new JsonHandler();
        int NumberOfWordsInFullBag = FullTokenMap.size();
        Set set = SortedFullTokenMap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry    = (Map.Entry)iterator.next();
            TokenObjectClass token = (TokenObjectClass)mentry.getValue();
            double probabilityofevidence = (double)token.GetCount() / (double)TotalNumberOfDocuments;
            token.SetPoE((float)probabilityofevidence);
                       
            json.addBuilder(token.getJsonObject());
            
            //System.out.printf ("%-15s exists in %-4d/%-4d of the documents. PoE:%-5.3f\n", token.GetWord(), token.GetCount(), TotalNumberOfDocuments, token.GetPoE());
        }
        json.build();
        json.write("./json/","bag.of.words");
    }
    
    public static class AccuracyClass{
       double m_TruePositive, m_TrueNegative, m_FalsePositive, m_FalseNegative; 
 
       public AccuracyClass(int TP, int TN, int FP, int FN){
           m_TruePositive = (double)TP;
           m_TrueNegative = (double)TN;
           m_FalsePositive = (double)FP;
           m_FalseNegative = (double)FN;
       }
       
       public double TruePositiveRate(){
           return m_TruePositive / (m_TruePositive + m_FalseNegative);
       }
       
       public double TrueNegativeRate(){
           return m_TrueNegative / (m_TrueNegative / m_FalsePositive);
       }
       
       public double TotalAccuracy(){
           return (m_TruePositive + m_TrueNegative) / (m_TruePositive + m_TrueNegative + m_FalsePositive + m_FalseNegative);
       }
       
       public double PositivePredictiveValue(){
           return m_TruePositive / (m_TruePositive + m_FalsePositive);
       }
       
       public double NegativePredictiveValue(){
           return m_TrueNegative / (m_TrueNegative / m_FalseNegative);
       }
       
       public double GetSensitivity(){
           return TruePositiveRate();
       }
       
       public double GetSpecifity(){
           return TrueNegativeRate();
       }
       
       public double GetPrecision(){
           return PositivePredictiveValue();
       }
       
       public double GetRecall(){
           return TruePositiveRate();
       }
       
       public double GetFMeasure(){
           return ((2* GetPrecision() * GetRecall()) / (GetPrecision() + GetRecall()));
       }
   }

}
