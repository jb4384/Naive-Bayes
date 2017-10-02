/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NB_Classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.*;
import opennlp.tools.util.Span;
import opennlp.tools.lemmatizer.DictionaryLemmatizer;

/**
 *
 * @author David Klecker
 */

public class FeatureExtractor {

    public String stopwords[] = {"jan", "feb", "mar", "apr", "may", "june", "july", "aug", "sept", "oct", "nov", "dec", "mon", "tue", "wed", "thur", "fri", "sat", "sun", "edu", "a", "about", "above", "above", "across", "after", "afterwards", "again", "against", "all", "almost", "alone", "along", "already", "also","although","always","am","among", "amongst", "amoungst", "amount",  "an", "and", "another", "any","anyhow","anyone","anything","anyway", "anywhere", "are", "around", "as",  "at", "back","be","became", "because","become","becomes", "becoming", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bill", "both", "bottom","but", "by", "call", "can", "cannot", "cant", "co", "con", "could", "couldnt", "cry", "de", "describe", "detail", "do", "done", "down", "due", "during", "each", "eg", "eight", "either", "eleven","else", "elsewhere", "empty", "enough", "etc", "even", "ever", "every", "everyone", "everything", "everywhere", "except", "few", "fifteen", "fify", "fill", "find", "fire", "first", "five", "for", "former", "formerly", "forty", "found", "four", "from", "front", "full", "further", "get", "give", "go", "had", "has", "hasnt", "have", "he", "hence", "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "how", "however", "hundred", "ie", "if", "in", "inc", "indeed", "interest", "into", "is", "it", "its", "itself", "keep", "last", "latter", "latterly", "least", "less", "ltd", "made", "many", "may", "me", "meanwhile", "might", "mill", "mine", "more", "moreover", "most", "mostly", "move", "much", "must", "my", "myself", "name", "namely", "neither", "never", "nevertheless", "next", "nine", "no", "nobody", "none", "noone", "nor", "not", "nothing", "now", "nowhere", "of", "off", "often", "on", "once", "one", "only", "onto", "or", "other", "others", "otherwise", "our", "ours", "ourselves", "out", "over", "own","part", "per", "perhaps", "please", "put", "rather", "re", "same", "see", "seem", "seemed", "seeming", "seems", "serious", "several", "she", "should", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "still", "such", "system", "take", "ten", "than", "that", "the", "their", "them", "themselves", "then", "thence", "there", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they", "thickv", "thin", "third", "this", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "top", "toward", "towards", "twelve", "twenty", "two", "un", "under", "until", "up", "upon", "us", "very", "via", "was", "we", "well", "were", "what", "whatever", "when", "whence", "whenever", "where", "whereafter", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whoever", "whole", "whom", "whose", "why", "will", "with", "within", "without", "would", "yet", "you", "your", "yours", "yourself", "yourselves", "the", "2000", "faq", };     
    public ArrayList<DocumentObjectClass> Vector; 
    public DocumentObjectClass FullDocumentBagOfWords;
    public String m_ClassOfDocuments;
    public String m_ClassPath;
    public double ProbabilityOfClass;    
    public DictionaryLemmatizer lemmatizer;
    
    public FeatureExtractor(String Documents, String Description) throws IOException  {
        m_ClassPath         = Documents;
        m_ClassOfDocuments  = Description;
        Vector              = new ArrayList<DocumentObjectClass>();
                        
        FullDocumentBagOfWords = new DocumentObjectClass(new HashMap<String, TokenObjectClass>(), Documents, 0, "");
        buildDictionary();
    }

    public class TokenObjectClass{

        private String m_Word;
        private Float m_TermFrequency;
        private Float m_PoE;
        private Float m_PoL;
        private Integer m_Count;
        
        public String GetWord()                    {return m_Word;}
        public void SetWord(String value)          {m_Word = value;}
        public Float GetTermFrequency()            {return m_TermFrequency;}
        public void SetTermFrequency(Float value)  {m_TermFrequency = value;}
        public Float GetPoE()                       {return m_PoE;}
        public void SetPoE(Float value)             {m_PoE = value;}
        public Float GetPoL()                       {return m_PoL;}
        public void SetPoL(Float value)             {m_PoL = value;}
        public Integer GetCount()                  {return m_Count;}
        public void SetCount(Integer value)       {m_Count = value;}
        
        public TokenObjectClass(String Word, Integer Count){
            m_Word          = Word;
            m_Count         = Count;
            m_TermFrequency = new Float(0.0);
        }
    }
    
    public class DocumentObjectClass{
        private String m_DocumentName;
        private int m_WordsInDocument;
        private String m_Classification; //Headline, Sports, Entertainment. 
        public HashMap<String, TokenObjectClass> m_TokenMap;
        
        public String GetDocumentName()             {return m_DocumentName;}
        public void SetDocumentName(String value)   {m_DocumentName = value;}
        public int GetWordsInDocument()             {return m_WordsInDocument;}
        public void SetWordsInDocument(int value)   {m_WordsInDocument = value;}
        public String GetClassification()           {return m_Classification;}
        public void SetClassification(String value) {m_Classification = value;}
                
        public DocumentObjectClass(HashMap<String, TokenObjectClass> TokenMap, String Document, int WordCount, String Classification){
            m_TokenMap              = TokenMap;
            m_DocumentName          = Document;
            int m_WordsInDocument   = WordCount;
            m_Classification        = Classification;
        }
    }
   
    public void TrainWithDocuments() throws FileNotFoundException, IOException{
        File dir                = new File(m_ClassPath);
        File[] directoryListing = dir.listFiles();
        HashMap<String, TokenObjectClass> m_FullMapForAllDocuments = new HashMap<String, TokenObjectClass>();

        int numberofFiles = 0;

        if (directoryListing != null) {
            for (File child : directoryListing) {
                numberofFiles++;
                System.out.println(numberofFiles+ "/" +directoryListing.length+ ". Reading "+ child.getPath());
                
                Scanner sc2 = null;
                
                try {
                    sc2             = new Scanner(new File(child.getPath()));
                    String document = "";
                    while (sc2.hasNextLine()) {
                        document += " "+sc2.nextLine();
                    }
                            
                    document = document.replaceAll("/"," ");
                    String stopWordsPattern = String.join("|", stopwords);
                    Pattern pattern         = Pattern.compile("\\b(?:" + stopWordsPattern + ")\\b\\s*", Pattern.CASE_INSENSITIVE);
                    Matcher matcher         = pattern.matcher(document);
                    document                = matcher.replaceAll("");

                    WhitespaceTokenizer whitespaceTokenizer = WhitespaceTokenizer.INSTANCE;  
       
                    //Tokenizing the given paragraph 
                    String tokens[] = whitespaceTokenizer.tokenize(document);
                
                    String word             = "";
                    
                    HashMap<String, TokenObjectClass> m_Map = new HashMap<String, TokenObjectClass>();
                    for(int i=0; i<tokens.length;i++)
                    {
                        String token = tokens[i];
                        word = TrimWord(token);
                        
                        if(word.isEmpty()) continue;
                        
                        TokenObjectClass m_Token = m_Map.get(word);
                        if(m_Token != null){
                            Integer count = m_Token.GetCount();
                            if (count >= 6) System.out.println(word+ " " + count);
                            m_Token.SetCount((m_Map.containsKey(word)) ? new Integer(count.intValue() + 1) : new Integer(1));
                        }
                        else{
                            TokenObjectClass pToken = new TokenObjectClass(word, 1);
                            m_Map.put(word, pToken);
                        }
                    }

                    HashMap<String, TokenObjectClass> SortedMap = sortByValues(m_Map);
                    
                    DocumentObjectClass pDoc = new DocumentObjectClass(SortedMap, child.getName(), SortedMap.size(), m_ClassOfDocuments);

                    Set set = SortedMap.entrySet();
                    Iterator iterator = set.iterator();
                    int WordCount = SortedMap.size();
                    
                    while(iterator.hasNext()) {
                        Map.Entry mentry        = (Map.Entry)iterator.next();
                        TokenObjectClass pToken = (TokenObjectClass) mentry.getValue();
                        int value               = (int)pToken.m_Count;
                        pToken.m_TermFrequency  = 100*(value / (float)WordCount);
                    }
                    
                    Vector.add(pDoc);
                    
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  
                }
            }
        } else {
          // Handle the case where dir is not really a directory.
          // Checking dir.isDirectory() above would not be sufficient
          // to avoid race conditions with another process that deletes
          // directories.
        }  
        for(int i=0; i<Vector.size(); i++)
        {
            DocumentObjectClass pDoc = Vector.get(i);
            Set set = pDoc.m_TokenMap.entrySet();
            Iterator iterator = set.iterator();
                    
            while(iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                TokenObjectClass token = (TokenObjectClass) mentry.getValue();
                
                if(m_FullMapForAllDocuments.containsKey(token.GetWord())){
                    TokenObjectClass token2 = m_FullMapForAllDocuments.get(mentry.getKey());
                    token2.SetCount(token2.GetCount() + 1);
                }
                else{
                    TokenObjectClass newToken = new TokenObjectClass((String)mentry.getKey(), 1);
                    m_FullMapForAllDocuments.put((String)mentry.getKey(), newToken);
                }
            }
        }
        
        Set set = m_FullMapForAllDocuments.entrySet();
        Iterator iterator = set.iterator();

        while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();
            TokenObjectClass token = (TokenObjectClass) mentry.getValue();
            
            double PoL = (double)token.GetCount() /(double) Vector.size();
            token.SetPoL((float)PoL);
        }
        
        FullDocumentBagOfWords.m_TokenMap = m_FullMapForAllDocuments;
    }
        
    private String TrimWord(String word)
    {      
        word = word.toLowerCase();
        
        if(word.length() == 1 || word.length() == 2) return "";
        if(word.contains(":")) return "";
        if(word.contains("<")) return "";
        if(word.contains(">")) return "";

        Pattern p = Pattern.compile("[a-zA-Z0-9]+[.][a-zA-Z0-9]+");
        Matcher m = p.matcher(word);
        if (m.find()) return "";

        p = Pattern.compile("[0-9]");
        m = p.matcher(word);
        if (m.find()) return "";
        
        p = Pattern.compile("[(].");
        m = p.matcher(word);
        if (m.find()) return "";

        p = Pattern.compile(".[)]");
        m = p.matcher(word);
        if (m.find()) return "";

        p = Pattern.compile("[-]+");
        m = p.matcher(word);
        if (m.find()) return "";

        word = word.replaceAll("[^a-zA-Z0-9\\s]", "");
        if(word.length() == 0) return "";

        return word;
    } 

    public TokenObjectClass CreateNewTokenObject(String Word, int Count){
        
        return new TokenObjectClass(Word, Count);
    }
    
    public static HashMap sortByValues(HashMap map) { 
        List list = new LinkedList(map.entrySet());
        
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                Map.Entry<String, TokenObjectClass> Token1 = (Map.Entry<String, TokenObjectClass>)o1;
                Map.Entry<String, TokenObjectClass> Token2 = (Map.Entry<String, TokenObjectClass>)o2;
                
                return (int) ((Comparable) (((TokenObjectClass)Token2.getValue()).m_Count).compareTo(((TokenObjectClass)Token1.getValue()).m_Count));
            }
       });
        
       HashMap sortedHashMap = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
              Map.Entry entry = (Map.Entry) it.next();
              sortedHashMap.put(entry.getKey(), entry.getValue());
       } 
       return sortedHashMap;
    }  
    
    private void buildDictionary() throws FileNotFoundException, IOException{
          // loading the dictionary to input stream
        InputStream dictLemmatizer = new FileInputStream("./dictionary.txt");
        // loading the lemmatizer with dictionary
        lemmatizer = new DictionaryLemmatizer(dictLemmatizer);
   }
}
