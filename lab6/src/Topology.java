import java.util.*;

/**
 * Reprezentuje topologie grafu polaczen miedzy wezlami systemu.
 *
 * Domyslna topologia – 6 wezlow (0-5), krawedzie:
 *   0-1, 0-2, 1-3, 1-4, 2-4, 2-5, 3-4
 *
 *        0
 *       / \
 *      1   2
 *     / \ / \
 *    3   4   5
 *     \ /
 *      (3-4)
 */
public class Topology {

    private final Map<Integer, List<Integer>> adjacency = new LinkedHashMap<>();

    /** Buduje domyslna, sztywno zakodowana topologie 6-wezlowa. */
    public Topology() {
        addEdge(0, 1);
        addEdge(0, 2);
        addEdge(1, 3);
        addEdge(1, 4);
        addEdge(2, 4);
        addEdge(2, 5);
        addEdge(3, 4);
    }

    private void addEdge(int u, int v) {
        adjacency.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
        adjacency.computeIfAbsent(v, k -> new ArrayList<>()).add(u);
    }

    /** Zwraca liste sasiadow podanego wezla. */
    public List<Integer> getNeighbors(int nodeId) {
        return Collections.unmodifiableList(
                adjacency.getOrDefault(nodeId, Collections.emptyList()));
    }

    /** Zwraca zbior identyfikatorow wszystkich wezlow w grafie. */
    public Set<Integer> getNodeIds() {
        return adjacency.keySet();
    }

    /** Drukuje topologie na standardowe wyjscie. */
    public void print() {
        System.out.println("Topologia grafu:");
        for (Map.Entry<Integer, List<Integer>> e : adjacency.entrySet()) {
            System.out.println("  Wezel " + e.getKey() + " -> " + e.getValue());
        }
    }
}
