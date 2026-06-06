package database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

public class DataManupulator {

     private final TaskGroup database;
     private ObjectMapper mapper;
     // Constructor
     public DataManupulator(TaskGroup database){
        this.database = database;

        this.mapper = new ObjectMapper();
    }

    private LinkedHashMap<String, TaskGroup> data = new LinkedHashMap<>();
    // reader
    public void load() throws IOException {
        if(database.getFile().exists() && database.getFile().length() > 0){
            data = mapper.readValue(database.getFile(), new TypeReference<LinkedHashMap<String, TaskGroup>>() {});
        }else{
            System.out.println("File do not exists!!!");
        }
    }

    public void write(String id, String name,List<Task> task) throws IOException{
        data.put(id, new TaskGroup(id, name, task));
        mapper.writerWithDefaultPrettyPrinter().writeValue(database.getFile(), data);
    }
    public void writeAll(LinkedHashMap<String, TaskGroup> data) throws IOException {
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(database.getFile(), data);
    }

    public LinkedHashMap<String, TaskGroup> getData(){
         return this.data;
    }
}
