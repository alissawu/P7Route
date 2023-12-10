/**
 * Demonstrates the calculation of shortest paths in the US Highway
 * network, showing the functionality of GraphProcessor and using
 * Visualize
 * @author Owen Astrachan (preliminary)
 * @author Alissa Wu
 */

 import java.io.*;
import java.util.*;

public class GraphDemo {
    private Map<String, Point> cityMap;

    public GraphDemo() {
        cityMap = new HashMap<>();
    }

    public void readData(String filename) throws IOException {
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(",");
                String name = data[0] + " " + data[1];
                cityMap.put(name, new Point(Double.parseDouble(data[2]), Double.parseDouble(data[3])));
            }
        }
    }

    public void userInteract(GraphProcessor graphProcessor, Visualize visualizer) {
        String start = "Miami FL"; // Default start location
        String end = "Seattle WA"; // Default end location

        // Uncomment for user interaction from terminal
        /*
        Scanner in = new Scanner(System.in);
        System.out.print("Enter source location: ");
        start = in.nextLine();
        System.out.print("Enter destination location: ");
        end = in.nextLine();
        in.close();
        */

        if (!cityMap.containsKey(start) || !cityMap.containsKey(end)) {
            System.out.printf("One or both of the specified cities cannot be found in the graph.\n");
            return;
        }

        Point nearStart = graphProcessor.nearestPoint(cityMap.get(start));
        Point nearEnd = graphProcessor.nearestPoint(cityMap.get(end));

        try {
            List<Point> path = graphProcessor.route(nearStart, nearEnd);
            double dist = graphProcessor.routeDistance(path);
            System.out.printf("Short path has %d points and is %2.3f miles in length.\n", path.size(), dist);
            visualizer.drawRoute(path);
        } catch (Exception e) {
            System.out.println("An error occurred while finding the route: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String usaCityFile = "data/uscities.csv";

        String[] durhamData = {"images/durham.png", "data/durham.vis", "data/durham.graph"};
        String[] usaData = {"images/usa.png", "data/usa.vis", "data/usa.graph"};
        String[] simpleData = {"images/simple.png", "data/simple.vis", "data/simple.graph"};

        // Modify below to use different datasets 
        String[] useThisData = usaData;

        try {
            GraphDemo demo = new GraphDemo();
            demo.readData(usaCityFile);

            GraphProcessor graphProcessor = new GraphProcessor();
            graphProcessor.initialize(new FileInputStream(useThisData[2]));
            Visualize visualizer = new Visualize(useThisData[1], useThisData[0]);
            demo.userInteract(graphProcessor, visualizer);
        } catch (IOException e) {
            System.err.println("An error occurred while initializing the graph data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
