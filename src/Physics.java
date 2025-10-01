import java.util.List;

public class Physics {
    private static final double G = 10;
    public static double getG() { return G; }

    private double dt = 0.001;
    private final double epsilon = 0.1;

    private List<Body> bodies;

    public Physics(Simulation simulation) {
        this.bodies = simulation.getBodies();
    }

    public void step() {
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
}
