import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

public class Graphics {
    private Simulation simulation;
    private final Group simGroup;

    public Graphics(Simulation simulation, Group simGroup) {
        this.simulation = simulation;
        this.simGroup = simGroup;
    }

    private double lineWidth = 0.25;
    private double lineOpacity = 0.4;

    private int maxPoints = 2000;
    private Glow starGlow = new Glow(0.45);

    private List<Polyline> trails = new ArrayList<>();
    public List<Polyline> getTrails() { return trails; }

    private List<Polyline> orbits = new ArrayList<>();
    public List<Polyline> getOrbits() { return orbits; }

    public void update(int orbitDisplayMode) {
        List<Body> bodies = simulation.getBodies();
        List<Circle> bodyShapes = simulation.getBodyShapes();
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
                    if (body.main == null) drawOrbit(body, bodies.get(0), orbits.get(i));
                    else drawOrbit(body, body.main, orbits.get(i));
                }
            }
            if (i == 0) bodyShape.setEffect(starGlow);
        }
    }

    public void addBodyShape(Body body) {
        List<Circle> bodyShapes = simulation.getBodyShapes();
        Circle bodyShape = new Circle(body.x, body.y, body.radius, body.color);
        bodyShapes.add(bodyShape);

        Polyline trail = new Polyline();
        trail.setStroke(body.color);
        trail.setStrokeWidth(lineWidth);
        trail.setOpacity(lineOpacity);
        trails.add(trail);

        Polyline orbit = new Polyline();
        orbit.setStroke(body.color);
        orbit.setStrokeWidth(lineWidth);
        orbit.setOpacity(lineOpacity);
        orbits.add(orbit);

        simGroup.getChildren().addAll(bodyShape, trail, orbit);
    }

    public void init() {
        initOrbits();
        initBodies();
    }
    public void initBodies() {
        List<Circle> bodyShapes = simulation.getBodyShapes();
        List<Body> bodies = simulation.getBodies();
        for (int i = 0; i < bodies.size(); i++) {
            if (!simGroup.getChildren().contains(bodyShapes.get(i))) {
                if (bodies.get(i).hasAtmosphere) initAtmosphere(bodies.get(i));
                simGroup.getChildren().add(bodyShapes.get(i));
            }
        }
    }
    public void initAtmosphere(Body body) {
        List<Circle> bodyShapes = simulation.getBodyShapes();
        List<Body> bodies = simulation.getBodies();

        Atmosphere atmosphere = body.atmosphere;
        Circle atmShape = new Circle(body.radius + atmosphere.radius, atmosphere.color);
        atmShape.setOpacity(atmosphere.opacity);
        atmShape.centerXProperty().bind(bodyShapes.get(bodies.indexOf(body)).centerXProperty());
        atmShape.centerYProperty().bind(bodyShapes.get(bodies.indexOf(body)).centerYProperty());
        simGroup.getChildren().add(atmShape);
    }
    public void initOrbits() {
        List<Body> bodies = simulation.getBodies();
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            if (body.initialized) continue;
            body.init();
            Color color = body.color;

            Polyline trail = new Polyline();
            trail.setStroke(color);
            trail.setStrokeWidth(lineWidth);
            trail.setOpacity(lineOpacity);
            trails.add(trail);

            Polyline orbit = new Polyline();
            orbit.setStroke(color);
            orbit.setStrokeWidth(lineWidth);
            orbit.setOpacity(lineOpacity);
            orbits.add(orbit);

            simGroup.getChildren().addAll(trail, orbit);
        }
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
        double mu = Physics.getG() * (main.mass + body.mass);

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

        double[] points = new double[2 * 1800];
        for (int k = 0; k < 1800; k++) {
            double theta = 2 * Math.PI * k / 1800;
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
}
