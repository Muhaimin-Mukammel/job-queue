package database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.util.TreeMap;


public class Stateupgrader {

    private final Object lock = new Object();

    public void upgrade(File file, int id, String state) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        if (!file.exists() || file.length() == 0) {throw new IllegalStateException("DB file missing or empty");}

        synchronized (lock) {
            TreeMap<Integer, TaskGroup> data = mapper.readValue(file, new TypeReference<TreeMap<Integer, TaskGroup>>() {});

            TaskGroup db = data.get(id);

            if (db == null) {throw new IllegalArgumentException("No DB entry for id: " + id);}
            db.setStatus(state);

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        }
    }
}
