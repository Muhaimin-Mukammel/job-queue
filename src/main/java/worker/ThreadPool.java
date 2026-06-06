package worker;

import database.TaskGroup;
import lobby.Job;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {
    private final BlockingQueue<Job> queue;
    private final TaskGroup database;

    // Constructors
    public ThreadPool(BlockingQueue<Job> queue, TaskGroup database){
        this.queue = queue;
        this.database = database;
    }
    private final List<Thread> workers = new ArrayList<>();
    private final List<Worker> workerObject = new ArrayList<>();
    LiveInformation liveInformation = new LiveInformation();

    public void startThreadpool(int NumberOfWorker) throws Exception {
        if(workers.isEmpty() == false){
            throw new IllegalStateException("Thread pool is already running");
        }
        for ( int i = 1 ; i <= NumberOfWorker; i++){
            Worker worker = new Worker(queue, database, "Worker " + i + ":", liveInformation);
            Thread thread = new Thread(worker);
            workerObject.add(worker);
            workers.add(thread);
            thread.start();

        }
    }

    public void shutdownThreadpool(){
        for (Thread worker : workers){
            worker.interrupt();
        }

        for( Thread worker : workers){
            try {
                worker.join();
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        workers.clear();
    }

    public List<Information> info(){
        List<Information> workerinfo = new ArrayList<>();

        for ( Worker worker : workerObject){
            workerinfo.add(
                    new Information(
                            worker.getNumberofworkdone(),
                            worker.getNameofthethread()
                    )
            );
        }
        return workerinfo;
    }
    public LiveInformation getLiveInformation(){
        return  this.liveInformation;
    }
    public void clearWorkers(){
        workerObject.clear();
    }
}