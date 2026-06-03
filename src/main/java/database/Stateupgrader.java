package database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;

public class Stateupgrader {

    private static final Object lock = new Object();

    private static final ObjectMapper mapper = new ObjectMapper();

    public void upgrade(File file, int id, int taskNo, String state) throws Exception {

        synchronized (lock) {

            ObjectNode root = (ObjectNode) mapper.readTree(file);
            if (root == null) return;

            JsonNode dbNode = root.get(String.valueOf(id));
            if (dbNode == null || !dbNode.isObject()) return;
            ObjectNode node = (ObjectNode) dbNode;
            JsonNode tasksNode = node.get("task");
            if (tasksNode == null || !tasksNode.isArray()) return;
            ArrayNode tasks = (ArrayNode) tasksNode;
            for (JsonNode t : tasks) {
                if (!t.isObject()) continue;
                ObjectNode task = (ObjectNode) t;
                JsonNode taskNoNode = task.get("taskno");
                if (taskNoNode == null) continue;
                if (taskNoNode.asInt() == taskNo) {
                    task.put("status", state);
                    break;
                }
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        }
    }
}