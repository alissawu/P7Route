import java.security.InvalidAlgorithmParameterException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 * To do: Add your name(s) as additional authors
 * @author Brandon Fain
 * @author Owen Astrachan modified in Fall 2023
 *
 */

public class GraphProcessor {
    private Map<Point, Integer> componentMap;
    private Map<Point, List<Point>> adjacencyMap;
    private List<Point> vertexList;
    private int vertexCount;
    private int edgeCount;

    public void initialize(FileInputStream file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        if (!scanner.hasNextInt()) {
            scanner.close();
            throw new FileNotFoundException("Couldn't read .graph");
        }

        vertexCount = scanner.nextInt();
        edgeCount = scanner.nextInt();
        scanner.nextLine(); // Move to the next line

        vertexList = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            String[] line = scanner.nextLine().split(" ");
            vertexList.add(new Point(Double.parseDouble(line[1]), Double.parseDouble(line[2])));
        }

        adjacencyMap = new HashMap<>();
        for (int i = 0; i < edgeCount; i++) {
            String[] edgeInfo = scanner.nextLine().split(" ");
            Point source = vertexList.get(Integer.parseInt(edgeInfo[0]));
            Point destination = vertexList.get(Integer.parseInt(edgeInfo[1]));
            adjacencyMap.computeIfAbsent(source, k -> new ArrayList<>()).add(destination);
            adjacencyMap.computeIfAbsent(destination, k -> new ArrayList<>()).add(source);
        }

        buildComponents();
        scanner.close();
    }
    /**
     * NOT USED IN FALL 2023, no need to implement
     * @return list of all vertices in graph
     */
    public List<Point> getVertices(){
        return null;
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * @return all edges in graph
     */
    public List<Point[]> getEdges(){
        return null;
    }


    private void buildComponents() {
        componentMap = new HashMap<>();
        Set<Point> seen = new HashSet<>();
        int componentId = 0;

        for (Point vertex : vertexList) {
            if (!seen.contains(vertex)) {
                exploreComponent(vertex, componentId++, seen);
            }
        }
    }

    private void exploreComponent(Point start, int componentId, Set<Point> seen) {
        Stack<Point> stack = new Stack<>();
        stack.push(start);
        seen.add(start);
        componentMap.put(start, componentId);

        while (!stack.isEmpty()) {
            Point current = stack.pop();
            for (Point neighbor : adjacencyMap.get(current)) {
                if (seen.add(neighbor)) { 
                    componentMap.put(neighbor, componentId);
                    stack.push(neighbor);
                }
            }
        }
    }

    public Point nearestPoint(Point p) {
        Point nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Point vertex : vertexList) {
            double distance = p.distance(vertex);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = vertex;
            }
        }

        return nearest;
    }

    public double routeDistance(List<Point> route) {
        double d = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            d += route.get(i).distance(route.get(i + 1));
        }
        return d;
    }

    public boolean connected(Point p1, Point p2) {
        return componentMap.getOrDefault(p1, -1).equals(componentMap.getOrDefault(p2, -2));
    }

    public List<Point> route(Point start, Point end) throws InvalidAlgorithmParameterException {
        if (start.equals(end)) {
            throw new InvalidAlgorithmParameterException("Start and end points are the same.");
        }
        if (!adjacencyMap.containsKey(start) || !adjacencyMap.containsKey(end)) {
            throw new InvalidAlgorithmParameterException("Invalid start or end point.");
        }
        if (!connected(start, end)) {
            throw new InvalidAlgorithmParameterException("No path exists between the start and end points.");
        }
    
        Map<Point, Double> distTo = new HashMap<>();
        Map<Point, Point> pathTo = new HashMap<>();
        PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingDouble(distTo::get));
        Set<Point> visited = new HashSet<>();
    
        for (Point p : adjacencyMap.keySet()) {
            distTo.put(p, Double.MAX_VALUE);
        }
        distTo.put(start, 0.0);
        queue.add(start);
    
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            visited.add(current);
    
            for (Point neighbor : adjacencyMap.get(current)) {
                if (visited.contains(neighbor)) continue;
                double newDist = distTo.get(current) + current.distance(neighbor);
                if (newDist < distTo.get(neighbor)) {
                    distTo.put(neighbor, newDist);
                    pathTo.put(neighbor, current);
                    if (!queue.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }
        }
    
        if (!pathTo.containsKey(end)) {
            throw new InvalidAlgorithmParameterException("No path exists between the start and end points.");
        }
    
        return buildPath(start, end, pathTo);
    }
    

    private List<Point> buildPath(Point start, Point end, Map<Point, Point> pathTo) {
        LinkedList<Point> path = new LinkedList<>();
        Point current = end;
        while (current != null && !current.equals(start)) {
            path.addFirst(current);
            current = pathTo.get(current);
        }

        path.addFirst(start); // Add start point to the beginning of the path
        return path;
    }
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String name = "data/usa.graph";
        GraphProcessor gp = new GraphProcessor();
        gp.initialize(new FileInputStream(name));
        System.out.println("running GraphProcessor");
    }

}
