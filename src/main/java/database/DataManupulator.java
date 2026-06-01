package database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.TreeMap;
import java.util.List;


public class DataManupulator {
     Database database = new Database();
     private TreeMap<Integer, Database> data = new TreeMap<>();
     private Task task = new Task();
     public void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        if(database.getFile().exists() && database.getFile().length() > 0){
            data = mapper.readValue(database.getFile(), new TypeReference<TreeMap<Integer, Database>>() {});
        }else{
            System.out.println("File do not exists!!!");
        }
    }
    public TreeMap<Integer, Database> getData(){
         return this.data = data;
    }
    public Task manupulatedata(int id){
        database = data.get(id);
        return task = (Task) database.getTask();
    }
    public Task gettask(){
         return this.task;
    }

}
