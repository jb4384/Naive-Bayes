/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package NB_Classifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import opennlp.tools.tokenize.*;
import opennlp.tools.postag.POSSample;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

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
    public ModelLoader model;
    
    public FeatureExtractor(String Documents, String Description, ModelLoader model) throws IOException  {
        m_ClassPath         = Documents;
        m_ClassOfDocuments  = Description;
        Vector              = new ArrayList<>();
        FullDocumentBagOfWords = new DocumentObjectClass(new HashMap<>(), Documents, 0, "");
        this.model = model;
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
            m_PoE           = new Float(0.0);
            m_PoL           = new Float(0.0);
        }
 
        public TokenObjectClass(JsonObject json){
            m_Word          = json.getString("m_Word");
            m_Count         = Integer.valueOf(json.getString("m_Count"));
            m_TermFrequency = Float.valueOf(json.getString("m_TermFrequency"));
            m_PoE           = Float.valueOf(json.getString("m_PoE"));
            m_PoL           = Float.valueOf(json.getString("m_PoL"));
        }

        public JsonObject getJsonObject(){     
            JsonBuilderFactory factory = Json.createBuilderFactory(null);
            JsonObject value = factory.createObjectBuilder()
                .add("m_Word", m_Word)
                .add("m_Count", m_Count.toString())
                .add("m_TermFrequency", m_TermFrequency.toString())
                .add("m_PoE", m_PoE.toString())
                .add("m_PoL", m_PoL.toString())
                .build();
            return value;
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
            m_WordsInDocument   = WordCount;
            m_Classification        = Classification;
        }
    }

    public void TrainWithDocuments() throws FileNotFoundException, IOException{
        File dir                = new File(m_ClassPath);
        File[] directoryListing = dir.listFiles();
        HashMap<String, TokenObjectClass> m_FullMapForAllDocuments = new HashMap<>();

        int numberofFiles = 0;

        if (directoryListing != null) {
            for (File child : directoryListing) {
                numberofFiles++;
                System.out.println(numberofFiles+ "/" +directoryListing.length+ ". Reading "+ child.getPath());
                Map<String, Integer> keywords = new HashMap<>();
                try {
                    InputStreamFactory r = null;
                    try {
                        r = new MarkableFileInputStreamFactory(
                           new File(child.getPath()));
                    } catch (FileNotFoundException e) {}
                    Map<String, Integer> nounSet = new HashMap<>();
                    ObjectStream<String> lineStream = new PlainTextByLineStream(r,"UTF-8");

                   String line;

                   while ((line = lineStream.read()) != null) {
                       if ("".equals(model.testRejection(line))) continue;
                        String[] tokens = removeNull(SimpleTokenizer.INSTANCE.tokenize(line));
                        String[] tags = model.tagger.tag(tokens);

                        POSSample sample = new POSSample(tokens, tags);
                       for (String sentence : sample.getSentence()) {
                           String words[] = sample.getSentence();
                           for (String word : words)
                               word = word.toLowerCase();
                           String posTags[] = sample.getTags();
                           words = model.lemmatize(words, posTags);
                           for (int i = 0; i < words.length; i++){
                                if (words[i].length() > 2 && tags[i].matches("NN|NNP|NNS|NNPS")) {
                                    int count = (nounSet.containsKey(words[i])) ? nounSet.get(words[i]) + 1 : 1;
                                    nounSet.put(words[i], count);
                                }
                           }
                       }
                    }
                   Map<String, Integer> sortedSet = mapSorter.sortByComparator(nounSet,true);
                   sortedSet.entrySet().forEach((pair) -> {
                       keywords.put(pair.getKey(), Integer.parseInt(pair.getValue().toString()));
                    });
                    System.out.println(keywords.toString());

                } catch (IOException t) {}
/*                Scanner sc2 = null;
                
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
                    
                    HashMap<String, TokenObjectClass> m_Map = new HashMap<>();
                    for (String token : tokens) {
                        word = TrimWord(token);
                        if(word.isEmpty()) continue;
                        
                        TokenObjectClass m_Token = m_Map.get(word);
                        if(m_Token != null){
                            Integer count = m_Token.GetCount();
                            if (count >= 6) System.out.println(word+ " " + count);
                            m_Token.SetCount((m_Map.containsKey(word)) ? count.intValue() + 1 : 1);
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
                }
            }*/
        } /*else {
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
            }*/
        }

        
        /*Set set = m_FullMapForAllDocuments.entrySet();
        Iterator iterator = set.iterator();

        while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();
            TokenObjectClass token = (TokenObjectClass) mentry.getValue();
            
            double PoL = (double)token.GetCount() /(double) Vector.size();
            token.SetPoL((float)PoL);
        }
        
        FullDocumentBagOfWords.m_TokenMap = m_FullMapForAllDocuments;*/
    }

    public String[] removeNull(String[] a) {
       ArrayList<String> removedNull = new ArrayList<String>();
       for (String str : a)
          if (str != null)
             removedNull.add(str);
       return removedNull.toArray(new String[0]);
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
        Collections.sort(list, (Object o1, Object o2) -> {
            Map.Entry<String, TokenObjectClass> Token1 = (Map.Entry<String, TokenObjectClass>)o1;
            Map.Entry<String, TokenObjectClass> Token2 = (Map.Entry<String, TokenObjectClass>)o2;
            
            return (int) ((Comparable) (((TokenObjectClass)Token2.getValue()).m_Count).compareTo(((TokenObjectClass)Token1.getValue()).m_Count));
        });
        
       HashMap sortedHashMap = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
              Map.Entry entry = (Map.Entry) it.next();
              sortedHashMap.put(entry.getKey(), entry.getValue());
       } 
       return sortedHashMap;
    }  
    }
