package database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.TreeMap;

public class DataManupulator {

     private final TaskGroup database;

     // Constructor
     public DataManupulator(TaskGroup database){
        this.database = database;
    }

    private TreeMap<Integer, TaskGroup> data = new TreeMap<>();
    // reader
    public void load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        if(database.getFile().exists() && database.getFile().length() > 0){
            data = mapper.readValue(database.getFile(), new TypeReference<TreeMap<Integer, TaskGroup>>() {});
        }else{
            System.out.println("File do not exists!!!");
        }
    }

    public TreeMap<Integer, TaskGroup> getData(){
         return this.data;
    }
}
