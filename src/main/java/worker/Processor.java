package worker;

import database.Stateupgrader;
import lobby.Job;

import java.io.File;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Processor {

    public void workProcessor() throws InterruptedException {

        try {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
            int min = 500;
            int max = 3000;

            int randomtime = min + new Random().nextInt(max - min);

        try{
            Thread.sleep(randomtime);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return;
        }
    }
}
