import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Simulation {
    private List<Body> bodies = new ArrayList<>();
    public List<Body> getBodies() { return bodies; }

    private List<Circle> bodyShapes = new ArrayList<>();
    public List<Circle> getBodyShapes() { return bodyShapes; }

    public void addBody(String name, double x, double y, double mass, double radius, Color color) {
        Body body = new Body(name, x, y, mass, radius, color);
        bodies.add(body);
        Circle bodyShape = new Circle(x, y, radius, color);
        bodyShapes.add(bodyShape);
    }
    
}
