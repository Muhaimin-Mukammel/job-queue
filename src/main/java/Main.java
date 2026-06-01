
import server.Orchestrator;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter the amount of time that the server should run : ");
        int run = sc.nextInt();

        Orchestrator orchestrator = new Orchestrator();
        orchestrator.start(run);

    }
}
