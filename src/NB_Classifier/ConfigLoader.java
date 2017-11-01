/*
 * Allows the use of a named configuration file to set corpus variables
 */
package NB_Classifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author jabar
 */
public final class ConfigLoader {
    private HashMap<String, String> classes;
    private HashMap<String, String> defaults;
    private final String file;
    private Boolean loaded = false;
    
    public ConfigLoader() {
        file = "corpus_config.txt";
        createDefaults();
        setDefaults();
    }
    
    public ConfigLoader(String[] array, String name){
        file = name;
        classes = new HashMap();
        for (String array1 : array)
            classes.put(array1,"");
        createDefaults();
    }
    
    private void createDefaults(){
        defaults = new HashMap();
        defaults.put("Class_Politics_Guns","./20_newsgroups_Train/talk.politics.guns");
        defaults.put("Class_Politics_Mideast","./20_newsgroups_Train/talk.politics.mideast");
        defaults.put("Class_Politics_Misc","./20_newsgroups_Train/talk.politics.misc");

        defaults.put("Class_Politics_Guns_Test","./20_newsgroups_Test/talk.politics.guns");
        defaults.put("Class_Politics_Mideast_Test","./20_newsgroups_Test/talk.politics.mideast");
        defaults.put("Class_Politics_Misc_Test","./20_newsgroups_Test/talk.politics.misc");        
    }
    
    public void setDefaults(){
        classes = new HashMap();
        defaults.entrySet().stream().map((entry) -> entry.getKey()).forEachOrdered((key) -> {
            classes.put(key, defaults.get(key));
        });
    }
    
    public String getValue(String key){
        return classes.get(key);
    }
    
    public Boolean testFileExists(){
        File f = new File(file);
        if(f.exists() && !f.isDirectory()) loaded = true;
        return loaded;
    }
    
    public void loadConfig(){
        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(file);

            // load a properties file
            prop.load(input);

            // set corpus path locations
            classes.entrySet().stream().map((entry) -> entry.getKey()).forEachOrdered((key) -> {
                String value = (prop.containsKey(key)) ? prop.getProperty(key) : (defaults.containsKey(key)) ? defaults.get(key) : "";
                classes.replace(key, value);
            });
        } catch (IOException ex) {

        } finally {
                if (input != null) {
                        try {
                                input.close();
                        } catch (IOException e) {
                        }
                }
        }
    }
}
