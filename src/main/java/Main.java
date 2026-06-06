
import database.*;
import server.Orchestrator;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        TaskGroup database = new TaskGroup();
        Scanner sc = new Scanner(System.in);

        Orchestrator orchestrator = new Orchestrator(database);

            System.out.println("""
                    1. JOB Processing.
                    2. CURD Operations.
                    """);
            int key = sc.nextInt();
            sc.nextLine();

            if (key == 1) {
                System.out.println("Enter the amount of time that the server should run : ");
                int run = sc.nextInt();
                System.out.println("Enter what number of workers should work : ");
                int workercount = sc.nextInt();

                orchestrator.start(run, workercount);
            } else if (key == 2) {
                orchestrator.startCURD();
            }


    }
}
