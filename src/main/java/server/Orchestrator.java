package server;

import database.Task;
import database.TaskGroup;
import lobby.JobAdder;
import lobby.Lobby;
import worker.WorkerManager;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Orchestrator {

    private final TaskGroup database;
    private final int workercount;
    public Orchestrator(TaskGroup database, int workercount){
        this.database = database;
        this.workercount = workercount;
    }

    // Scheduled Executor Service
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start(int RunTimeInSecond) throws IOException {
        Lobby lobby = new Lobby();
        JobAdder jobAdder = new JobAdder(database);
        jobAdder.init();
        BlockingQueue<Task> queue = jobAdder.getQueue();
        WorkerManager workerManager = new WorkerManager(queue, database, workercount);
        workerManager.startWorkerManager();


        scheduler.schedule(() -> {
            lobby.shutdown();
            workerManager.shutDownworkers();
            scheduler.shutdown();
        }, RunTimeInSecond, TimeUnit.SECONDS);
    }
}
