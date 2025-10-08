import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

public class Graphics {
    private Simulation simulation;
    private final Group simGroup;
    private final Pane simPane;
    private final Controller controller;
    private Circle selected;
    public Graphics(Simulation simulation, Group simGroup, Pane simPane, Controller controller) {
        this.simulation = simulation;
        this.simGroup = simGroup;
        this.simPane = simPane;
        this.controller = controller;

        this.selected = new Circle();
        selected.setOpacity(0.2);
        simPane.getChildren().add(0, selected);
    }
    private double lineWidth = Config.getDouble("graphics.lineWidth");
    private double lineOpacity = Config.getDouble("graphics.lineOpacity");
    private int maxPoints = Config.getInt("graphics.maxPoints");

    private List<Polyline> trails = new ArrayList<>();
    public List<Polyline> getTrails() { return trails; }
    private List<Polyline> orbits = new ArrayList<>();

    private List<Polyline> trailsV = new ArrayList<>();
    private List<Polyline> orbitsV = new ArrayList<>();

    private double dist = 0;
    public double getDist() { return dist; }

    private boolean oldR = true;

    public void update(int orbitDisplayMode, boolean isRelative) {
        List<Body> bodies = simulation.getBodies();
        List<Circle> bodyShapes = simulation.getBodyShapes();

        List<Vessel> vessels = simulation.getVessels();
        List<Circle> vesselShapes = simulation.getVesselShapes();

        for (int i = 0; i < vessels.size(); i++) {
            Vessel vessel = vessels.get(i);
            Circle vesselShape = vesselShapes.get(i);

            vesselShape.setCenterX(vessel.x);
            vesselShape.setCenterY(vessel.y);
            Body parent = vessel.main;

            switch (orbitDisplayMode) {
                case 1: {
                    if (oldR != isRelative) {
                        for (Polyline trail : trailsV) {
                            trail.getPoints().clear();
                            trail.setTranslateX(0);
                            trail.setTranslateY(0);
                        }
                        oldR = isRelative;
                        System.out.println("Switched to " + ((isRelative) ? "relative" : "absolute") + " mode");
                    }
                    orbitsV.get(i).setVisible(false);
                    double scale = simPane.getScaleX();
                    trailsV.get(i).setStrokeWidth(lineWidth/scale);
                    double rx = (isRelative) ? vessel.x - parent.x : vessel.x;
                    double ry = (isRelative) ? vessel.y - parent.y : vessel.y;
                    trailsV.get(i).getPoints().addAll(rx, ry);
                    if (trailsV.get(i).getPoints().size() > maxPoints) {
                        trailsV.get(i).getPoints().remove(0, 2);
                    }
                    trailsV.get(i).setTranslateX((isRelative) ? parent.x : 0);
                    trailsV.get(i).setTranslateY((isRelative) ? parent.y : 0);
                    break;
                }
                case 2: {
                    trailsV.get(i).getPoints().clear();
                    orbitsV.get(i).setStrokeWidth(lineWidth/simPane.getScaleX());
                    drawOrbit(null, vessel, parent, orbitsV.get(i));
                }
            }
            
        }

        for (int i = 0; i < bodies.size(); i++) {
            Circle bodyShape = bodyShapes.get(i);
            Body body = bodies.get(i);

            bodyShape.setCenterX(body.x);
            bodyShape.setCenterY(body.y);

            if (body == controller.getSelectedBody()) {
                Body main = body.main;
                if (main != null) {
                    dist = Math.sqrt(Math.pow((body.x-main.x), 2) + Math.pow((body.y - main.y), 2)) - main.radius;
                } 
                selected.setFill(body.getColor());
                selected.setLayoutX(body.x);
                selected.setLayoutY(body.y);
                selected.setRadius(5 / simPane.getScaleX());
                selected.setVisible(true);
            }

            Body parent = (body.main != null) ? body.main : bodies.get(0);
            switch (orbitDisplayMode) {
                case 1: {
                    if (oldR != isRelative) {
                        for (Polyline trail : trails) {
                            trail.getPoints().clear();
                            trail.setTranslateX(0);
                            trail.setTranslateY(0);
                        }
                        oldR = isRelative;
                        System.out.println("Switched to " + ((isRelative) ? "relative" : "absolute") + " mode");
                    }
                    orbits.get(i).setVisible(false);
                    double scale = simPane.getScaleX();
                    trails.get(i).setStrokeWidth(lineWidth/scale);
                    double rx = (isRelative) ? body.x - parent.x : body.x;
                    double ry = (isRelative) ? body.y - parent.y : body.y;
                    //double tx = (body.x - rx) * scale;
                    //double ty = (body.y - ry) * scale;
                    trails.get(i).getPoints().addAll(rx, ry);
                    if (trails.get(i).getPoints().size() > maxPoints) {
                        trails.get(i).getPoints().remove(0, 2);
                    }
                    trails.get(i).setTranslateX((isRelative) ? parent.x : 0);
                    trails.get(i).setTranslateY((isRelative) ? parent.y : 0);
                    break;
                }
                case 2: {
                    trails.get(i).getPoints().clear();
                    orbits.get(i).setStrokeWidth(lineWidth/simPane.getScaleX());
                    drawOrbit(body, null, parent, orbits.get(i));
                }
            }
        }
    }
    public void init() {
        initOrbits();
        initBodies();
    }
    public void initBodies() {
        List<Body> bodies = simulation.getBodies();
        List<Circle> bodyShapes = simulation.getBodyShapes();
        for (int i = 0; i < bodies.size(); i++) {
            if (!simGroup.getChildren().contains(bodyShapes.get(i))) {
                if (bodies.get(i).hasAtmosphere) initAtmosphere(bodies.get(i));
                simGroup.getChildren().add(bodyShapes.get(i));
            }
        }
        List<Vessel> vessels = simulation.getVessels();
        List<Circle> vesselShapes = simulation.getVesselShapes();
        for (int i = 0; i < vessels.size(); i++) {
            if (!simGroup.getChildren().contains(vesselShapes.get(i))) {
                simGroup.getChildren().add(vesselShapes.get(i));
            }
        }
    }
    public void initAtmosphere(Body body) {
        List<Circle> bodyShapes = simulation.getBodyShapes();
        List<Body> bodies = simulation.getBodies();

        Atmosphere atmosphere = body.atmosphere;
        Circle atmShape = new Circle(body.radius + atmosphere.radius, atmosphere.color);
        atmShape.setCache(false);
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
            if (body.main == null) body.main = bodies.get(0);
            Color color = body.getColor();
            
            Polyline trail = new Polyline();
            trail.setStroke(color);
            trail.setStrokeWidth(lineWidth);
            trail.setOpacity(lineOpacity);
            trail.setCache(false);
            trails.add(trail);

            Polyline orbit = new Polyline();
            orbit.setStroke(color);
            orbit.setStrokeWidth(lineWidth);
            orbit.setOpacity(lineOpacity);
            orbit.setCache(false);
            orbits.add(orbit);

            simGroup.getChildren().addAll(trail, orbit);
        }
        List<Vessel> vessels = simulation.getVessels();
        for (int i = 0; i < vessels.size(); i++) {
            Vessel vessel = vessels.get(i);
            if (vessel.initialized) continue;
            vessel.init();
            
            Color color = vessel.getColor();

            Polyline trail = new Polyline();
            trail.setStroke(color);
            trail.setStrokeWidth(lineWidth);
            trail.setOpacity(lineOpacity);
            trail.setCache(false);
            trailsV.add(trail);

            Polyline orbit = new Polyline();
            orbit.setStroke(color);
            orbit.setStrokeWidth(lineWidth);
            orbit.setOpacity(lineOpacity);
            orbit.setCache(false);
            orbitsV.add(orbit);

            simGroup.getChildren().addAll(trail, orbit);
        }
    }

    private void drawOrbit(Body planet, Vessel vessel, Body main, Polyline orbitLine) {
        var body = (planet == null) ? vessel : planet;
        double rx = body.x - main.x;
        double ry = body.y - main.y;
        double vx = body.vx - main.vx;
        double vy = body.vy - main.vy;

        double r = Math.hypot(rx, ry);
        if (r < 1e-6) {
            orbitLine.setVisible(false);
            return;
        }
        double mu = Config.getDouble("physics.G") * (main.mass + body.mass);

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
