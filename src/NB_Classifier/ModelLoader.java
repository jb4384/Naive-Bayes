package NB_Classifier;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

/**
 *
 * @author jabar
 */
public final class ModelLoader {
    public POSTaggerME tagger;
    public DictionaryLemmatizer lemmatizer;
    public String[] rejects = null;
    
    public ModelLoader() throws FileNotFoundException, IOException{
        buildPOSTagger();
        buildDictionary();
        buildRejectionDictionary();
    }
    
    private void buildPOSTagger(){
        POSModel model = new POSModelLoader()	
                .load(new File("en-pos-maxent.bin"));
        tagger = new POSTaggerME(model);
    }

    private void buildDictionary() throws FileNotFoundException, IOException{
        // loading the dictionary to input stream
      InputStream dictLemmatizer = new FileInputStream("./dictionaries/main.txt");
      // loading the lemmatizer with dictionary
      lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
    }

    private void buildRejectionDictionary() throws FileNotFoundException, IOException{
        List<String> items = new ArrayList<>();

        FileInputStream fstream = new FileInputStream("./dictionaries/line_rejection.txt"); 
        DataInputStream data_input = new DataInputStream(fstream); 
        BufferedReader buffer = new BufferedReader(new InputStreamReader(data_input)); 
        String str_line; 

        while ((str_line = buffer.readLine()) != null) { 
            str_line = str_line.trim(); 
            if ((str_line.length()!=0)) { 
                items.add(str_line);
            } 
        }
        rejects = (String[])items.toArray(new String[items.size()]);
    }
    
    public String[] lemmatize(String[] words, String[] postags) throws IOException {
        if (lemmatizer == null) {
            try (InputStream is = getClass().getResourceAsStream("./dictionary.txt")) {
                lemmatizer = new DictionaryLemmatizer(is);
            }
        }
        String[] lemma = lemmatizer.lemmatize(words, postags);
        return lemma;
    }
    
    public String testRejection(String line) {
        for (String reject : rejects)
            if (line.startsWith(reject)) line = "";
        return line;
    }
}

