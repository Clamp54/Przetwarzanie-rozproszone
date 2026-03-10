import java.io.*;
import java.net.*;
import java.util.*;

public class BufferServer {
    private static List<String> cars = new ArrayList<>();
    private static int capacity = 10;

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(12345);
        while (true) {
            try (Socket s = server.accept();
                 Scanner in = new Scanner(s.getInputStream());
                 PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

                if (in.hasNextLine()) {
                    String line = in.nextLine();
                    String[] parts = line.split(" ");
                    String cmd = parts[0];

                    if (cmd.equals("PUT")) {
                        String vin = parts[1];
                        if (cars.size() < capacity) {
                            cars.add(vin);
                            System.out.println("Przyjeto: " + vin);
                            out.println("OK");
                        } else {
                            out.println("FULL");
                        }
                    } else if (cmd.equals("GET_SPECIFIC")) {
                        String brand = parts[1];
                        String found = null;
                        for (String car : cars) {
                            if (car.startsWith(brand)) {
                                found = car;
                                break;
                            }
                        }
                        if (found != null) {
                            cars.remove(found);
                            System.out.println("Wydano: " + found);
                            out.println(found);
                        } else {
                            out.println("WAIT");
                        }
                    }
                }
            }
        }
    }
}