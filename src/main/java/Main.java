import database.*;
import server.Orchestrator;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws IOException {
        TaskGroup database = new TaskGroup();
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the amount of time that the server should run : ");
        int run = sc.nextInt();
        System.out.println("Enter what number of workers should work : ");
        int workercount = sc.nextInt();

        Orchestrator orchestrator = new Orchestrator(database, workercount);
        orchestrator.start(run);

    }
}
