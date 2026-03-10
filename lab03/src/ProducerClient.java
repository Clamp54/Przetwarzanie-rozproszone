import java.io.*;
import java.net.*;

public class ProducerClient {
    public static void main(String[] args) throws Exception {
        String brand = args[0];
        int count = 0;
        while (true) {
            String vin = brand + "-" + (++count);
            try (Socket s = new Socket("localhost", 12345)) {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                out.println("PUT " + vin);
                if (in.readLine().equals("OK")) {
                    System.out.println("Producent " + brand + " dodal " + vin);
                    Thread.sleep(2000);
                } else {
                    count--;
                    Thread.sleep(1000);
                }
            }
        }
    }
}