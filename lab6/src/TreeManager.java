import java.util.*;

/**
 * Zarzadza stanem drzewa rozpinajacego.
 * Przechowuje krawedzie wyznaczone przez algorytm i drukuje wynik.
 * Wszystkie metody sa watkobezpieczne.
 */
public class TreeManager {

    private final List<int[]> treeEdges = new ArrayList<>();

    /**
     * Dodaje krawedz do drzewa (ignoruje duplikaty bez wzgledu na kierunek).
     */
    public synchronized void addEdge(int u, int v) {
        for (int[] e : treeEdges) {
            if ((e[0] == u && e[1] == v) || (e[0] == v && e[1] == u)) {
                return; // krawedz juz istnieje
            }
        }
        treeEdges.add(new int[]{u, v});
        System.out.println("[TreeManager] Nowa krawedz drzewa: " + u + " -- " + v);
    }

    /** Zwraca liste krawedzi drzewa (kopia). */
    public synchronized List<int[]> getTreeEdges() {
        return new ArrayList<>(treeEdges);
    }

    /** Drukuje finalne drzewo rozpinajace. */
    public synchronized void printTree() {
        System.out.println();
        System.out.println("=== Drzewo Rozpinajace ===");
        if (treeEdges.isEmpty()) {
            System.out.println("  (brak krawedzi)");
        } else {
            for (int[] e : treeEdges) {
                System.out.println("  " + e[0] + " -- " + e[1]);
            }
        }
        System.out.println("Liczba krawedzi: " + treeEdges.size());
        System.out.println("=========================");
    }
}
