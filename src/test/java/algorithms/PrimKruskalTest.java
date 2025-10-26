package algorithms;

import graphs.Graph;
import graphs.Edge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PrimKruskalTest {

    @Test
    @DisplayName("Prim vs Kruskal: стоимость совпадает, размер MST = V-1 (связный граф)")
    void mstCostsEqualAndSizeVminus1() {
        // граф из задания (5 вершин, 7 рёбер)
        Graph g = new Graph(5);
        g.addUndirectedEdge(0,1,4); // A-B
        g.addUndirectedEdge(0,2,3); // A-C
        g.addUndirectedEdge(1,2,2); // B-C
        g.addUndirectedEdge(1,3,5); // B-D
        g.addUndirectedEdge(2,3,7); // C-D
        g.addUndirectedEdge(2,4,8); // C-E
        g.addUndirectedEdge(3,4,6); // D-E

        Prim.Result pr = new Prim().run(g, 0);
        Kruskal.Result kr = new Kruskal().run(g);

        assertEquals(pr.cost, kr.cost, "Стоимость MST у Prim и Kruskal должна совпадать");
        assertEquals(g.n() - 1, pr.mst.size(), "Prim: число рёбер MST = V-1");
        assertEquals(g.n() - 1, kr.mst.size(), "Kruskal: число рёбер MST = V-1");

        // Дополнительно: отсутствие петель и корректные индексы
        for (Edge e : pr.mst) {
            assertNotEquals(e.u, e.v, "В MST не должно быть петель");
            assertTrue(e.u >= 0 && e.u < g.n());
            assertTrue(e.v >= 0 && e.v < g.n());
        }
    }

    @Test
    @DisplayName("Несвязный граф: размер MST < V-1 (алгоритмы корректно отрабатывают)")
    void disconnectedGraphHandled() {
        Graph g = new Graph(4);
        g.addUndirectedEdge(0,1,1);
        // Компонента (2,3) отдельно:
        g.addUndirectedEdge(2,3,2);

        Prim.Result pr = new Prim().run(g, 0);
        Kruskal.Result kr = new Kruskal().run(g);

        assertTrue(pr.mst.size() < g.n() - 1 || kr.mst.size() < g.n() - 1,
                "На несвязном графе MST не может содержать V-1 рёбер");
    }

    @Test
    @DisplayName("Связный граф из 6 вершин: минимальные рёбра и отсутствие циклов")
    void simpleSixVertices() {
        Graph g = new Graph(6);
        // цепочка 0-1-2-3-4-5
        g.addUndirectedEdge(0,1,1);
        g.addUndirectedEdge(1,2,1);
        g.addUndirectedEdge(2,3,1);
        g.addUndirectedEdge(3,4,1);
        g.addUndirectedEdge(4,5,1);
        // тяжёлые «перемычки»
        g.addUndirectedEdge(0,2,10);
        g.addUndirectedEdge(1,3,10);
        g.addUndirectedEdge(2,4,10);
        g.addUndirectedEdge(3,5,10);

        Prim.Result pr = new Prim().run(g, 0);
        Kruskal.Result kr = new Kruskal().run(g);

        assertEquals(5, pr.mst.size());
        assertEquals(5, kr.mst.size());
        assertEquals(pr.cost, kr.cost);
        assertEquals(5, pr.cost, "Здесь MST — просто цепочка из 5 рёбер по весу 1");
    }
}
