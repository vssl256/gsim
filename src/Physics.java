import java.util.List;

public class Physics {
    private static final double G = Config.getDouble("physics.G");
    private double dt = Config.getDouble("physics.dt");

    private List<Body> bodies;
    private List<Vessel> vessels;
    public Physics(Simulation simulation) {
        this.bodies = simulation.getBodies();
        this.vessels = simulation.getVessels();
    }

    public void step() {
        for (int i = 0; i < vessels.size(); i++) {
            Vessel vessel = vessels.get(i);
            double ax = 0, ay = 0;
            for (int j = 0; j < bodies.size(); j++) {
                Body body = bodies.get(i);
                double dx = body.x - vessel.x;
                double dy = body.y - vessel.y;
                double r = Math.sqrt(dx*dx + dy*dy);
                double F = (G * vessel.mass * body.mass / (r*r));
                ax += F * dx / (r * vessel.mass);
                ay += F * dy / (r * vessel.mass);
            }
            vessel.vx += ax * dt;
            vessel.vy += ay * dt;
            vessel.x += vessel.vx * dt;
            vessel.y += vessel.vy * dt;
        }

        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            double ax = 0, ay = 0;
            for (int j = 0; j < bodies.size(); j++) {
                if (i == j) continue;
                Body other = bodies.get(j);
                double dx = other.x - body.x;
                double dy = other.y - body.y;
                double r = Math.sqrt(dx*dx + dy*dy);
                double F = (G * body.mass * other.mass / (r*r));
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
