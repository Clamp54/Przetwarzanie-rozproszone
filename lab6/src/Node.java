import java.util.*;
import java.util.concurrent.*;

/**
 * Wezel w systemie rozproszonym.
 *
 * Kazdy wezel dziala we wlasnym watku. Wiadomosci odbiera ze swojej kolejki
 * (inbox) i przetwarza je przez MessageHandler. Wysylanie wiadomosci polega na
 * wrzuceniu ich do kolejki wezla-odbiorcy, co symuluje asynchroniczna komunikacje
 * (analogicznie do kolejek JMS).
 */
public class Node implements Runnable {

    private final int id;
    private final List<Integer> neighbors;
    private final BlockingQueue<Message> inbox;
    private final Map<Integer, BlockingQueue<Message>> network;
    private MessageHandler handler;
    private volatile boolean running = true;

    /**
     * @param id        identyfikator wezla
     * @param neighbors lista sasiadow w grafie
     * @param inbox     kolejka wejsciowa tego wezla
     * @param network   mapa id -> kolejka, wspoldzielona przez caly system
     */
    public Node(int id, List<Integer> neighbors,
                BlockingQueue<Message> inbox,
                Map<Integer, BlockingQueue<Message>> network) {
        this.id        = id;
        this.neighbors = Collections.unmodifiableList(new ArrayList<>(neighbors));
        this.inbox     = inbox;
        this.network   = network;
    }

    public void setHandler(MessageHandler handler) { this.handler = handler; }
    public MessageHandler getHandler() { return handler; }
    public int getId() { return id; }

    /**
     * Wysyla wiadomosc do wezla-odbiorcy poprzez jego kolejke wejsciowa.
     */
    public void send(Message msg) {
        BlockingQueue<Message> dest = network.get(msg.getReceiverId());
        if (dest != null) {
            dest.offer(msg);
        } else {
            System.err.println("[Node " + id + "] Nieznany odbiorca: " + msg.getReceiverId());
        }
    }

    /** Zatrzymuje watek wezla po zakonczeniu aktualnie przetwarzanej wiadomosci. */
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        System.out.println("[Node " + id + "] Uruchomiony, sasiedzi: " + neighbors);
        try {
            while (running) {
                // Czekaj na wiadomosc maks. 5 sekund; po timeoucie zakoncz watek
                Message msg = inbox.poll(5, TimeUnit.SECONDS);
                if (msg == null) {
                    break;
                }
                handler.handle(msg);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[Node " + id + "] Zakonczony");
    }
}
