package lobby;

import database.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

public class JobAdder {
    private Queue<Task> queue = new LinkedList<>();
    volatile boolean running = true;
    public void init(){
        DataManupulator manupulate = new DataManupulator();
        TreeMap<Integer, Database> data = manupulate.getData();
        int id = 1;
        while (data.containsKey(id) &&  running){
            Task task = manupulate.manupulatedata(id);
            if(task != null){
                queue.offer(manupulate.gettask());
            }else{
                System.out.println("task is null in JobAdder ( line 19 )");
            }
            id++;
        }
    }
    public void shutdown(){
        running = false;
    }
}


