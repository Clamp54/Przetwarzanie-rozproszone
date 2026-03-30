import java.util.*;
import java.util.concurrent.*;

/**
 * Aplikacja glowna systemu rozproszonego wyznaczajacego drzewo rozpinajace.
 *
 * Uzycie:
 *   java DistributedTreeApp [algorytm] [inicjator]
 *
 *   algorytm  – "echo" (domyslnie) lub "tarry"
 *   inicjator – numer wezla inicjujacego algorytm (domyslnie 0)
 *
 * Przyklady:
 *   java DistributedTreeApp
 *   java DistributedTreeApp echo 0
 *   java DistributedTreeApp tarry 3
 */
public class DistributedTreeApp {

    public static void main(String[] args) throws InterruptedException {
        String algorithm  = (args.length > 0) ? args[0].toLowerCase() : "echo";
        int initiatorId   = (args.length > 1) ? Integer.parseInt(args[1]) : 0;

        System.out.println("=== System Rozproszony – Drzewo Rozpinajace ===");
        System.out.println("Algorytm  : " + algorithm.toUpperCase());
        System.out.println("Inicjator : wezel " + initiatorId);
        System.out.println();

        // 1. Topologia grafu
        Topology topology = new Topology();
        topology.print();
        System.out.println();

        // 2. Menedzer drzewa – zbiera krawedzie wyznaczone przez algorytm
        TreeManager treeManager = new TreeManager();

        // 3. Zatrzask sygnalizujacy zakonczenie algorytmu
        CountDownLatch doneLatch = new CountDownLatch(1);

        // 4. Siec – kazdy wezel ma swoja kolejke wejsciowa (symulacja JMS)
        Map<Integer, BlockingQueue<Message>> network = new HashMap<>();
        for (int nodeId : topology.getNodeIds()) {
            network.put(nodeId, new LinkedBlockingQueue<>());
        }

        // 5. Tworzenie wezlow i ich handlerow wiadomosci
        Map<Integer, Node> nodes = new LinkedHashMap<>();
        for (int nodeId : topology.getNodeIds()) {
            List<Integer> neighbors  = topology.getNeighbors(nodeId);
            boolean       isInit     = (nodeId == initiatorId);
            Node          node       = new Node(nodeId, neighbors, network.get(nodeId), network);
            MessageHandler handler   = new MessageHandler(
                    nodeId, neighbors, isInit, treeManager, doneLatch, node::send);
            node.setHandler(handler);
            nodes.put(nodeId, node);
        }

        // 6. Uruchomienie watkow wezlow
        List<Thread> threads = new ArrayList<>();
        for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
            Thread t = new Thread(entry.getValue(), "Node-" + entry.getKey());
            t.setDaemon(true);
            threads.add(t);
            t.start();
        }

        // Krotka pauza – upewnia sie, ze wszystkie watki sa gotowe
        Thread.sleep(100);

        // 7. Uruchomienie algorytmu na inicjatorze
        MessageHandler initiatorHandler = nodes.get(initiatorId).getHandler();
        if ("tarry".equals(algorithm)) {
            initiatorHandler.startTarry();
        } else {
            initiatorHandler.startEcho();
        }

        // 8. Oczekiwanie na zakonczenie (maks. 30 sekund)
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);

        // 9. Zatrzymanie watkow
        for (Node node : nodes.values()) {
            node.stop();
        }
        for (Thread t : threads) {
            t.interrupt();
        }

        // 10. Wynik
        System.out.println();
        if (completed) {
            treeManager.printTree();
        } else {
            System.err.println("BLAD: Algorytm nie zakonczyl sie w wymaganym czasie!");
        }
    }
}
