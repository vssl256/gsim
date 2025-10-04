import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.shape.Circle;

public class Simulation {
    private List<Body> bodies = new ArrayList<>();
    public List<Body> getBodies() { return bodies; }

    private List<Circle> bodyShapes = new ArrayList<>();
    public List<Circle> getBodyShapes() { return bodyShapes; }

    private List<Body> loadedBodies = new ArrayList<>();

    public void addBody(String name, double x, double y, double mass, double radius, String color) {
        Body body = new Body(name, x, y, mass, radius, color);
        bodies.add(body);
        Circle bodyShape = new Circle(x, y, radius, body.getColor());
        bodyShape.setCache(false);
        bodyShapes.add(bodyShape);
    }
    public void clearSimulation() {
        bodies.clear();
        bodyShapes.clear();
    }
    public void readJSON(String path) {
        clearSimulation();
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            Type listType = new TypeToken<List<Body>>() {}.getType();
            loadedBodies = gson.fromJson(reader, listType);
            loadBodies();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void loadBodies() {
        for (Body body : loadedBodies) {
            addBody(body.name, body.x, body.y, body.mass, body.radius, body.color);
        }
    }
    public void saveJSON(String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(bodies, bodies.getClass());
        try (FileWriter writer = new FileWriter("test.json")) {
            writer.write(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
