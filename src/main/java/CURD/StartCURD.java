package CURD;

import database.TaskGroup;

import java.io.IOException;
import java.util.Scanner;

public class StartCURD {
    private final TaskGroup database;

    public StartCURD(TaskGroup database) throws IOException {
        this.database = database;
    }

    public void STARTCURD() throws IOException {
        APIcalls apIcalls = new APIcalls(database);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("""
                1. Get data.
                2. post data.
                3. update data.
                4. delete data.
                5. to exit execution.
                """);
            int key = scanner.nextInt();

            if (key == 1) {
                apIcalls.getdata();
            } else if (key == 2) {
                apIcalls.postdata();
            } else if (key == 3) {
                apIcalls.patchdata();
            } else if (key == 4) {
                apIcalls.deletedata();
            } else if (key == 5) {
                System.out.println("----Execution finished----");
                scanner.close();
                break;
            } else {
                System.out.println("Invalid input!!!!!");
            }
        }
    }
}
