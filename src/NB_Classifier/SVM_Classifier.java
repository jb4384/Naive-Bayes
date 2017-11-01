/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NB_Classifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import NB_Classifier.FeatureExtractor.*;
import NB_Classifier.JsonHandler;
import NB_Classifier.ModelLoader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author David Klecker
 */
public class SVM_Classifier {

    public static int m_TotalNumberOfClasses;
    public static int m_TotalNumberofDocuments;
    public static ArrayList<FeatureExtractor> pFeatures;
    public static ModelLoader model;        
    public static HashMap<String, FeatureExtractor.TokenObjectClass> FullTokenMap;
    

    public static void main(String[] args) throws IOException {
        model = new ModelLoader();
 
        String Class_Politics_Guns      = "";
        String Class_Politics_Mideast   = "";
        String Class_Politics_Misc      = "";
        
        String[] classes = {"Class_Politics_Guns","Class_Politics_Mideast","Class_Politics_Misc"};
        String file = "corpus_config.txt";
        
        ConfigLoader config = new ConfigLoader(classes,file);
        if (config.testFileExists()){
            config.loadConfig();
            Class_Politics_Guns      = config.getValue(classes[0]);
            Class_Politics_Mideast   = config.getValue(classes[1]);
            Class_Politics_Misc      = config.getValue(classes[2]);
        } else {
            Class_Politics_Guns      = "./20_newsgroups_Train - Small/talk.politics.guns";
            Class_Politics_Mideast   = "./20_newsgroups_Train - Small/talk.politics.mideast";
            Class_Politics_Misc      = "./20_newsgroups_Train - Small/talk.politics.misc";
        }

        /*pFeatures                       = new ArrayList<>();
        
        TrainClass(Class_Politics_Guns, "Class Politics.Guns");
        //TrainClass(Class_Politics_Mideast, "Class Politics.Mideast");
        //TrainClass(Class_Politics_Misc, "Class Politics.Misc");
*/
        System.out.println("Done!");
    }

    public static void TrainClass(String ClassPath, String ClassDescription) throws IOException {
        FeatureExtractor pFeatureExtractor          = new FeatureExtractor(ClassPath, ClassDescription, model);
        m_TotalNumberofDocuments += pFeatureExtractor.TrainWithDocuments("SVM");

        PrintWriter writer = new PrintWriter("BagOfWordMatrix.csv", "UTF-8");

        for(int i=0; i<pFeatureExtractor.Vector.size(); i++){
            DocumentObjectClass pDoc = pFeatureExtractor.Vector.get(i);

            writer.printf("1 ");
            System.out.println(" ");
            
            Set set = pDoc.m_TokenMap.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {

                Map.Entry mentry = (Map.Entry)iterator.next();
                FeatureExtractor.TokenObjectClass token = (FeatureExtractor.TokenObjectClass) mentry.getValue();                    
            
                writer.printf ("%d:%d ", token.GetSVMVectorValue(), token.GetCount());
                System.out.printf("%d:%d ", token.GetSVMVectorValue(), token.GetCount());
            }
            writer.println("");
        }
        writer.close();

        pFeatures.add(pFeatureExtractor);
    }
}
