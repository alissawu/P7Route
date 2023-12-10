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
        List<Point> pathResult = new ArrayList<>();
    
        Set<Point> visitedNodes = new HashSet<>();
        Map<Point, Double> distanceTracker = new HashMap<>();
        Map<Point, Point> pathMap = new HashMap<>();
    
        Comparator<Point> pointComparator = (point1, point2) -> {
            double distance1 = distanceTracker.getOrDefault(point1, Double.MAX_VALUE);
            double distance2 = distanceTracker.getOrDefault(point2, Double.MAX_VALUE);
            return Double.compare(distance1, distance2);
        };
    
        PriorityQueue<Point> pointsToExplore = new PriorityQueue<>(pointComparator);
    
        if (!adjacencyMap.containsKey(start) || !adjacencyMap.containsKey(end)) {
            throw new InvalidAlgorithmParameterException("No path exists between start and end points.");
        }
    
        distanceTracker.put(start, 0.0);
        pointsToExplore.add(start);
    
        while (!pointsToExplore.isEmpty()) {
            Point currentPoint = pointsToExplore.remove();
    
            if (currentPoint.equals(end)) {
                break;
            }
    
            visitedNodes.add(currentPoint);
            for (Point neighbor : adjacencyMap.get(currentPoint)) {
                if (visitedNodes.contains(neighbor)) continue;
    
                double newDistance = distanceTracker.getOrDefault(currentPoint, Double.MAX_VALUE) + currentPoint.distance(neighbor);
                if (newDistance < distanceTracker.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    pathMap.put(neighbor, currentPoint);
                    distanceTracker.put(neighbor, newDistance);
                    pointsToExplore.remove(neighbor); // Update priority queue
                    pointsToExplore.add(neighbor);
                }
            }
        }
    
        if (!pathMap.containsKey(end)) {
            throw new InvalidAlgorithmParameterException("No path exists between the specified start and end points.");
        }
    
        for (Point current = end; current != null; current = pathMap.get(current)) {
            pathResult.add(current);
        }
        Collections.reverse(pathResult);
    
        if (pathResult.get(0).equals(start)) {
            return pathResult;
        } else {
            throw new InvalidAlgorithmParameterException("No path exists.");
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        String name = "data/usa.graph";
        GraphProcessor gp = new GraphProcessor();
        gp.initialize(new FileInputStream(name));
        System.out.println("running GraphProcessor");
    }

}
