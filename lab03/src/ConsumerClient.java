import java.io.*;
import java.net.*;

public class ConsumerClient {
    public static void main(String[] args) throws Exception {
        String targetBrand = args[0];
        while (true) {
            try (Socket s = new Socket("localhost", 12345)) {
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                out.println("GET_SPECIFIC " + targetBrand);
                String response = in.readLine();

                if (response.equals("WAIT")) {
                    System.out.println("Brak modelu " + targetBrand + ", czekam...");
                    Thread.sleep(2000);
                } else {
                    System.out.println("Klient kupil wymarzony: " + response);
                    break;
                }
            }
        }
    }
}