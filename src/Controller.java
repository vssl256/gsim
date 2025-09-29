import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;

public class Controller {

    @FXML private Button resetButton;
    @FXML private Button addButton;
    @FXML private Text posText;
    @FXML private Pane simPane;
    @FXML private Slider speedSlider;
    @FXML private ToggleGroup group1;
    @FXML private Button speedReset;

    private final double G = 10;
    private double dt = 0.01;
    private final double epsilon = 0.1;
    private final int maxPoints = 2000;
    private double speedFactor = 1;
    private Glow starGlow = new Glow(0.45);
    private int orbitDisplayMode = 1;

    private double mouseX;
    private double mouseY;

    private List<Body> bodies = new ArrayList<>();
    private List<Circle> bodyShapes = new ArrayList<>();
    private List<Polyline> trails = new ArrayList<>();
    private List<Polyline> orbits = new ArrayList<>();
    
    @FXML
    private void initialize() {
        timer.start();
        addBody(0, 0, 33300, 20, Color.ORANGERED, Color.RED);
        addBody(100, 0, 100, 1, Color.DEEPSKYBLUE, Color.LIME);
        addBody(104, 0, 0.001, 0.3, Color.GRAY, Color.YELLOW);
        addBody(200, 0, 320, 5, Color.ORANGE, Color.VIOLET);
        addBody(30, 0, 3, 1, Color.DARKGRAY, Color.GOLD);
        Platform.runLater(() -> {
            simPane.widthProperty().addListener((obs, oldVal, newVal) -> centerSystem());
            simPane.heightProperty().addListener((obs, oldVal, newVal) -> centerSystem());
            centerSystem();
            for (int i = 1; i < bodies.size(); i++) {
                bodies.get(i).setOrbit(bodies.get(0), G, 0);
            }
            bodies.get(2).setOrbit(bodies.get(1), G, 0);
            bodies.get(4).setOrbit(bodies.get(0), G, 0.1);
        });
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedFactor = newVal.doubleValue();
        });
        resetButton.setOnAction(event -> {
        for (int i = 0; i < bodies.size(); i++) {
            bodies.get(i).reset();
            trails.get(i).getPoints().clear();
        }
        });
        speedReset.setOnAction(event -> {
            speedSlider.valueProperty().set(1.0);
        });
        group1.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            RadioButton selected = (RadioButton) newToggle;
            switch (selected.getText()) {
                case "Trails": orbitDisplayMode = 1; break;
                case "Orbits": orbitDisplayMode = 2; break;
            }
        });
        simPane.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });
        simPane.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - mouseX;
            double dy = event.getSceneY() - mouseY;

            simPane.setTranslateX(simPane.getTranslateX() + dx);
            simPane.setTranslateY(simPane.getTranslateY() + dy);

            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });
        simPane.setOnScroll(event -> {
            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
            simPane.setScaleX(simPane.getScaleX() * zoomFactor);
            simPane.setScaleY(simPane.getScaleY() * zoomFactor);
        });
    }

    public void updateCamera() {
        double totalMass = 0;
        double cmX = 0;
        double cmY = 0;

        for (Body body : bodies) {
            totalMass += body.mass;
            cmX += body.mass * body.x;
            cmY += body.mass * body.y;
        }

        cmX /= totalMass;
        cmY /= totalMass;

        double cx = simPane.getWidth() / 2;
        double cy = simPane.getHeight() / 2;

        simPane.setTranslateX(cx - cmX);
        simPane.setTranslateY(cy - cmY);
    }

    public void centerSystem() {
        double cx = simPane.getWidth() / 2;
        double cy = simPane.getHeight() / 2;
        Body main = bodies.get(0);
        double dx = cx - main.x;
        double dy = cy - main.y;
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            body.x += dx;
            body.y += dy;
            trails.get(i).getPoints().clear();
        }
    }


    public void updatePhysics() {
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            double ax = 0;
            double ay = 0;
            for (int j = 0; j < bodies.size(); j++) {
                if (i == j) continue;
                Body other = bodies.get(j);
                double dx = other.x - body.x;
                double dy = other.y - body.y;
                double r = Math.sqrt(dx*dx + dy*dy);
                double F = G * body.mass * other.mass / (r*r + epsilon*epsilon);
                ax += F * dx / (r * body.mass);
                ay += F * dy / (r * body.mass);
            }
            body.vx += ax * dt;
            body.vy += ay * dt;
            body.x += body.vx * dt;
            body.y += body.vy * dt;
        }
    }

    public void updateGraphics() {
        for (int i = 0; i < bodies.size(); i++) {
            Circle bodyShape = bodyShapes.get(i);
            Body body = bodies.get(i);
            bodyShape.setCenterX(body.x);
            bodyShape.setCenterY(body.y);
            switch (orbitDisplayMode) {
                case 1: {
                    orbits.get(i).setVisible(false);

                    trails.get(i).getPoints().addAll(body.x, body.y);
                    if (trails.get(i).getPoints().size() > maxPoints) {
                        trails.get(i).getPoints().remove(0, 2);
                    }
                    break;
                }
                case 2: {
                    trails.get(i).getPoints().clear();
                    drawOrbit(body, bodies.get(0), orbits.get(i));
                }
            }
                    
            if (i == 0) bodyShape.setEffect(starGlow);
        }
    }

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
        posText.setText(Double.toString(Math.floor(speedFactor)));
            for (int i = 0; i < speedFactor; i++) {
                updatePhysics();
                //updateCamera();
            }
            updateGraphics();
        }
    };

    public void addBody(double x, double y, double mass, double radius, Color color, Color orbitColor) {
        Body body = new Body(x, y, mass, radius);
        bodies.add(body);

        Circle bodyShape = new Circle(x, y, radius, color);
        simPane.getChildren().add(bodyShape);
        bodyShapes.add(bodyShape);
        
        Polyline trail = new Polyline();
        trail.setStroke(orbitColor);
        trail.setEffect(starGlow);
        trail.setStrokeWidth(0.5);
        trail.setOpacity(0.4);
        trail.setFill(null);
        simPane.getChildren().add(trail);
        trails.add(trail);

        Polyline orbitLine = new Polyline();
        orbitLine.setStroke(orbitColor);
        orbitLine.setStrokeWidth(0.5);
        orbitLine.setOpacity(0.4);
        orbitLine.setFill(null);
        simPane.getChildren().add(orbitLine);
        orbits.add(orbitLine);
    }

    private void drawOrbit(Body body, Body main, Polyline orbitLine) {
    double rx = body.x - main.x;
    double ry = body.y - main.y;
    double vx = body.vx - main.vx;
    double vy = body.vy - main.vy;

    double r = Math.hypot(rx, ry);
    if (r < 1e-6) {
        orbitLine.setVisible(false);
        return;
    }

    double mu = G * (main.mass + body.mass);

    double v2 = vx * vx + vy * vy;
    double energy = 0.5 * v2 - mu / r;

    if (energy >= 0) {
        orbitLine.setVisible(false); // не замкнутая орбита
        return;
    }

    double a = -mu / (2.0 * energy);

    double h = rx * vy - ry * vx;

    double e2 = 1.0 + (2.0 * energy * h * h) / (mu * mu);
    if (e2 < 0) e2 = 0;
    double e = Math.sqrt(e2);

    // вектор эксцентриситета
    double rDotV = rx * vx + ry * vy;
    double ex = ((v2 - mu / r) * rx - rDotV * vx) / mu;
    double ey = ((v2 - mu / r) * ry - rDotV * vy) / mu;
    double emag = Math.hypot(ex, ey);

    double ux, uy;
    if (emag < 1e-9) {
        ux = rx / r;
        uy = ry / r;
    } else {
        ux = ex / emag;
        uy = ey / emag;
    }

    double vxAxis = -uy;
    double vyAxis = ux;

    double[] points = new double[2 * 180];
    for (int k = 0; k < 180; k++) {
        double theta = 2 * Math.PI * k / 180;
        double r_orb = (a * (1 - e * e)) / (1 + e * Math.cos(theta));

        double x_orb = r_orb * Math.cos(theta);
        double y_orb = r_orb * Math.sin(theta);

        double xr = x_orb * ux + y_orb * vxAxis;
        double yr = x_orb * uy + y_orb * vyAxis;

        points[2 * k] = main.x + xr;
        points[2 * k + 1] = main.y + yr;
    }
    List<Double> list = Arrays.stream(points).boxed().collect(Collectors.toList());
    orbitLine.getPoints().setAll(list);
    orbitLine.setVisible(true);
}


    public void centerSystemDepr() {
        double totalMass = 0;
        double cmX = 0;
        double cmY = 0;

        // 1. считаем массу и центр масс
        for (Body b : bodies) {
            totalMass += b.mass;
            cmX += b.mass * b.x;
            cmY += b.mass * b.y;
        }
        cmX /= totalMass;
        cmY /= totalMass;

        // 2. смещаем все тела так, чтобы ЦМ оказался в (0,0)
        for (Body b : bodies) {
            b.x -= cmX;
            b.y -= cmY;
        }

        // 3. сдвигаем в центр Pane
        double offsetX = simPane.getWidth() / 2;
        double offsetY = simPane.getHeight() / 2;
        for (Body b : bodies) {
            b.x += offsetX;
            b.y += offsetY;
        }

        // 4. считаем суммарный импульс
        double px = 0;
        double py = 0;
        for (Body b : bodies) {
            px += b.mass * b.vx;
            py += b.mass * b.vy;
        }

        // 5. скорость центра масс
        double vcmX = px / totalMass;
        double vcmY = py / totalMass;

        // 6. вычитаем её из скоростей
        for (Body b : bodies) {
            b.vx -= vcmX;
            b.vy -= vcmY;
        }
    }
}
