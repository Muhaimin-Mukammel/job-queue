package database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;

public class Stateupgrader {

    private static final Object lock = new Object();
    private static final ObjectMapper mapper = new ObjectMapper();
    private final File file;

    public Stateupgrader(File file) throws Exception {
        this.file = file;
    }

    public void upgrade(String id, int taskNo, String state) throws Exception {
        synchronized (lock) {
            // Read the entire file
            ObjectNode root = root = (ObjectNode) mapper.readTree(file);
            if (root == null) return;

            // find the value of the key (id)
            JsonNode dbNode = root.get(String.valueOf(id));

            // Safety check
            if (dbNode == null || !dbNode.isObject()) return;

            // Store the JsonNode into a more specific ObjectNode ( ObjectNode is used instead of JsonNode because ObjectNode has powerful keywords
            // like .get and .put;
            ObjectNode node = (ObjectNode) dbNode;

            // Find the value of task in node object;
            JsonNode tasksNode = node.get("task");

            // Safety check
            if (tasksNode == null || !tasksNode.isArray()) return;

            // Value of task is an array, so the value (tasksNode) is converted into a ArrayNode for traversing through it;
            ArrayNode tasks = (ArrayNode) tasksNode;

            for (JsonNode t : tasks) {

                if (!t.isObject()) continue;
                // Same thing as before
                ObjectNode task = (ObjectNode) t;
                JsonNode taskNoNode = task.get("taskno");

                if (taskNoNode == null) continue;

                if (taskNoNode.asInt() == taskNo) {
                    task.put("status", state);
                    break;
                }
            }
            //Writer
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        }
    }
}