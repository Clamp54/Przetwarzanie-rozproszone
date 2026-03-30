/**
 * Reprezentuje wiadomosc przesylana miedzy wezlami w systemie rozproszonym.
 * Typy wiadomosci:
 *   EN    - sygnał włączenia krawędzi (algorytm Echo)
 *   QU    - potwierdzenie / kwit (algorytm Echo)
 *   TOKEN - token przemierzajacy graf (algorytm Tarry'ego)
 */
public class Message {

    public enum Type { EN, QU, TOKEN }

    private final Type type;
    private final int senderId;
    private final int receiverId;

    public Message(Type type, int senderId, int receiverId) {
        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public Type getType()      { return type;       }
    public int  getSenderId()  { return senderId;   }
    public int  getReceiverId(){ return receiverId; }

    @Override
    public String toString() {
        return type + "(" + senderId + "->" + receiverId + ")";
    }
}
