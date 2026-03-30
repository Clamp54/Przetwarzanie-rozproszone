import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Obsluguje wiadomosci EN, QU (algorytm Echo) oraz TOKEN (algorytm Tarry'ego)
 * dla jednego wezla w systemie rozproszonym.
 *
 * Algorytm Echo (wiadomosci EN / QU):
 *   - Inicjator wysyla EN do wszystkich sasiadow.
 *   - Wezel, ktory otrzymal EN po raz pierwszy, zapamietuje nadawce jako rodzica
 *     i rozsyla EN do pozostalych sasiadow.
 *   - Kiedy wezel otrzymal EN lub QU od wszystkich sasiadow, wysyla QU do rodzica.
 *   - Drogi wiadomosci QU tworza drzewo rozpinajace.
 *
 * Algorytm Tarry'ego (wiadomosc TOKEN):
 *   - Token jest przesylany do kazdego sasiada dokladnie raz.
 *   - Jesli regula 1 nie moze byc zastosowana, token wraca do rodzica.
 *   - Kiedy token powroci do inicjatora, caly graf zostal przemierzony.
 *   - Pierwsze przejscia krawedzi tworza drzewo rozpinajace.
 */
public class MessageHandler {

    private final int nodeId;
    private final List<Integer> neighbors;
    private final boolean isInitiator;
    private final TreeManager treeManager;
    private final CountDownLatch doneLatch;
    private final Consumer<Message> sender;

    // --- Stan algorytmu Echo ---
    private boolean echoStarted = false;
    private int     echoParent  = -1;
    private final Set<Integer> echoReceived = new HashSet<>();
    private boolean echoQuSent  = false;

    // --- Stan algorytmu Tarry'ego ---
    private int tarryParent = -1;
    private final Set<Integer> tokenSentTo = new HashSet<>();

    public MessageHandler(int nodeId, List<Integer> neighbors, boolean isInitiator,
                          TreeManager treeManager, CountDownLatch doneLatch,
                          Consumer<Message> sender) {
        this.nodeId      = nodeId;
        this.neighbors   = new ArrayList<>(neighbors);
        this.isInitiator = isInitiator;
        this.treeManager = treeManager;
        this.doneLatch   = doneLatch;
        this.sender      = sender;
    }

    /** Glowny punkt wejscia – przekazuje wiadomosc do wlasciwej metody. */
    public void handle(Message msg) {
        System.out.println("[Node " + nodeId + "] Odebrano: " + msg);
        switch (msg.getType()) {
            case EN:    handleEN(msg);    break;
            case QU:    handleQU(msg);    break;
            case TOKEN: handleToken(msg); break;
        }
    }

    // =========================================================
    //  Algorytm Echo
    // =========================================================

    /** Uruchamia algorytm Echo na wezle inicjujacym. */
    public void startEcho() {
        echoStarted = true;
        System.out.println("[Node " + nodeId + "] ECHO: Inicjator startuje – wysylam EN do " + neighbors);
        for (int neighbor : neighbors) {
            sender.accept(new Message(Message.Type.EN, nodeId, neighbor));
        }
    }

    private void handleEN(Message msg) {
        int from = msg.getSenderId();

        if (!echoStarted) {
            // Pierwsze EN – ustaw rodzica i rozeslij EN do pozostalych sasiadow
            echoStarted = true;
            echoParent  = from;
            System.out.println("[Node " + nodeId + "] ECHO: Rodzic = " + echoParent);
            echoReceived.add(from);

            for (int neighbor : neighbors) {
                if (neighbor != from) {
                    sender.accept(new Message(Message.Type.EN, nodeId, neighbor));
                }
            }
        } else {
            // Kolejne EN od innego sasiada – tylko zarejestruj
            echoReceived.add(from);
        }

        checkEchoCompletion();
    }

    private void handleQU(Message msg) {
        echoReceived.add(msg.getSenderId());
        checkEchoCompletion();
    }

    /**
     * Sprawdza, czy wezel otrzymal odpowiedz od wszystkich sasiadow.
     * Jesli tak – wysyla QU do rodzica (lub sygnalizuje sukces inicjatorowi).
     */
    private void checkEchoCompletion() {
        if (!echoQuSent && echoReceived.size() >= neighbors.size()) {
            echoQuSent = true;
            if (isInitiator) {
                System.out.println("[Node " + nodeId + "] ECHO: Sukces! Drzewo rozpinajace wyznaczone.");
                doneLatch.countDown();
            } else {
                // Krawedz od rodzica do tego wezla nalezy do drzewa
                treeManager.addEdge(echoParent, nodeId);
                sender.accept(new Message(Message.Type.QU, nodeId, echoParent));
            }
        }
    }

    // =========================================================
    //  Algorytm Tarry'ego
    // =========================================================

    /** Uruchamia algorytm Tarry'ego na wezle inicjujacym. */
    public void startTarry() {
        System.out.println("[Node " + nodeId + "] TARRY: Inicjator startuje");
        sendTokenToNextNeighbor();
    }

    private void handleToken(Message msg) {
        int from = msg.getSenderId();

        if (!isInitiator && tarryParent == -1) {
            // Pierwsza wizyta tokenu – ustaw rodzica i zapisz krawedz drzewa
            tarryParent = from;
            treeManager.addEdge(tarryParent, nodeId);
            System.out.println("[Node " + nodeId + "] TARRY: Rodzic = " + tarryParent);
        }

        sendTokenToNextNeighbor();
    }

    /**
     * Wysyla token do pierwszego nieodwiedzonego sasiada (innego niz rodzic).
     * Jesli taki nie istnieje – odsyla token do rodzica lub konczy algorytm
     * (jesli jest inicjatorem).
     */
    private void sendTokenToNextNeighbor() {
        for (int neighbor : neighbors) {
            if (neighbor != tarryParent && !tokenSentTo.contains(neighbor)) {
                tokenSentTo.add(neighbor);
                sender.accept(new Message(Message.Type.TOKEN, nodeId, neighbor));
                return;
            }
        }

        // Brak nieodwiedzonych sasiadow (poza rodzicem)
        if (isInitiator) {
            System.out.println("[Node " + nodeId + "] TARRY: Sukces! Drzewo rozpinajace wyznaczone.");
            doneLatch.countDown();
        } else {
            sender.accept(new Message(Message.Type.TOKEN, nodeId, tarryParent));
        }
    }
}
