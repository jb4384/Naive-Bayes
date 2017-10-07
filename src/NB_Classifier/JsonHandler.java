package NB_Classifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;

/**
 * This class handles Json requests for various processes
 * 
 */
public class JsonHandler {
    public JsonArrayBuilder builder;
    public JsonArray json = null;
    
    public JsonHandler(){
        builder = Json.createArrayBuilder();
    }

    public void build(){
        json = builder.build();
    }
    
    public void addBuilder(JsonObject obj){
        builder.add(obj);
    }
    
    public Boolean write(String directory,String filename) throws IOException {
        if (json.isEmpty()) return false;
            try {
            File file = new File(directory+filename+".json");
            file.getParentFile().mkdirs();
            file.createNewFile(); // if file already exists will do nothing 
            FileOutputStream os = new FileOutputStream(file, false); 

            try (JsonWriter jsonWriter = Json.createWriter(os)) {
                jsonWriter.writeArray(json);
            } 
        } catch (Throwable t) {
            return false;
        }
        return true;
    }
}
