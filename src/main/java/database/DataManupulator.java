package database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

public class DataManupulator {
    public static void main(String[] args) throws IOException {
        Database database = new Database();
        ObjectMapper mapper = new ObjectMapper();
        TreeMap<Integer, Database> data = new TreeMap<>();
        if(database.getFile().exists() && database.getFile().length() > 0){
            data = mapper.readValue(database.getFile(), new TypeReference<TreeMap<Integer, Database>>() {});
        }else{
            System.out.println("File do not exists!!!");
        }
        

    }
}
