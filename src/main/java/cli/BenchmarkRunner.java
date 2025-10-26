package cli;

import algorithms.Kruskal;
import algorithms.Prim;
import graphs.Edge;
import graphs.Graph;
import inpout.JSONReader;
import inpout.JSONWriter;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkRunner {

    public static void main(String[] args) throws Exception {
        // вход/выход
        String inputPath  = args.length > 0 ? args[0] : "input/assign_3_input.json";
        String outputPath = args.length > 1 ? args[1] : "output.json";

        // читаем
        JSONReader.AllDatasets all = inpout.JSONReader.readAll(inputPath);

        // готовим результат
        JSONWriter.ResultsFile out = new JSONWriter.ResultsFile();
        out.results = new ArrayList<>();

        int gid = 1;
        for (JSONReader.Dataset ds : all.datasets) {
            for (JSONReader.GraphJson gj : ds.graphs) {

                // построим Graph
                int n = (gj.nodes != null && !gj.nodes.isEmpty()) ? gj.nodes.size() : gj.vertices;
                Map<String, Integer> idx = buildIndex(gj, n);
                Graph g = new Graph(n);
                for (JSONReader.EdgeJson e : gj.edges) {
                    int u = idx.get(e.from);
                    int v = idx.get(e.to);
                    g.addUndirectedEdge(u, v, e.weight);
                }

                // Prim
                Prim.Result pr = new Prim().run(g, 0);
                // Kruskal
                Kruskal.Result kr = new Kruskal().run(g);

                // упаковка в формат примера
                JSONWriter.ResultItem item = new JSONWriter.ResultItem();
                item.graph_id = gid++;
                item.id = gj.id;
                item.category = ds.category;

                JSONWriter.InputStats st = new JSONWriter.InputStats();
                st.vertices = g.n();
                st.edges = g.edges().size();
                item.input_stats = st;

                item.prim = toAlgoResult(pr, gj);
                item.kruskal = toAlgoResult(kr, gj);

                out.results.add(item);
            }
        }

        inpout.JSONWriter.writeResults(outputPath, out);
        System.out.println("✔ Written results to: " + outputPath);
    }

    private static Map<String,Integer> buildIndex(JSONReader.GraphJson gj, int n){
        Map<String,Integer> map = new HashMap<>();
        if (gj.nodes != null && !gj.nodes.isEmpty()) {
            for (int i = 0; i < gj.nodes.size(); i++) map.put(gj.nodes.get(i), i);
        } else {
            for (int i = 0; i < n; i++) map.put("N"+(i+1), i);
        }
        return map;
    }

    private static JSONWriter.AlgoResult toAlgoResult(Object res, JSONReader.GraphJson gj) {
        JSONWriter.AlgoResult a = new JSONWriter.AlgoResult();
        List<String> names = gj.nodes;
        if (names == null || names.isEmpty()) {
            names = new ArrayList<>();
            for (int i = 1; i <= gj.vertices; i++) names.add("N"+i);
        }

        if (res instanceof Prim.Result pr) {
            a.total_cost = pr.cost;
            a.execution_time_ms = round2(pr.M.timeMs());
            a.operations_count = pr.M.primOps();
            a.mst_edges = mapEdges(pr.mst, names);
        } else if (res instanceof Kruskal.Result kr) {
            a.total_cost = kr.cost;
            a.execution_time_ms = round2(kr.M.timeMs());
            a.operations_count = kr.M.kruskalOps();
            a.mst_edges = mapEdges(kr.mst, names);
        }
        return a;
    }

    private static List<JSONWriter.EdgeOut> mapEdges(List<Edge> list, List<String> names) {
        List<JSONWriter.EdgeOut> out = new ArrayList<>();
        for (Edge e: list) {
            out.add(new JSONWriter.EdgeOut(
                    names.get(e.u),
                    names.get(e.v),
                    e.w
            ));
        }
        return out;
    }

    private static double round2(double x){ return Math.round(x * 100.0) / 100.0; }
}
