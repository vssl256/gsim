import javafx.scene.paint.Color;

public class Vessel extends PhysicalObject {
    public Vessel(String name, double x, double y, double mass, Body main, String color) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.mass = mass;
        this.main = main;
        this.color = color;
        this.vx = this.vx0 = vx = 0;
        this.vy = this.vy0 = vy = 0;
    }
    public Color getColor() { return Color.valueOf(color); }
    public void setMain(Body main) { this.main = main; }
    public void init() { this.initialized = true; }

    public void setOrbit(Body main, double eccentricity) {
        double G = Config.getDouble("physics.G");
        double dx = this.x - main.x;
        double dy = this.y - main.y;
        double r = Math.sqrt(dx*dx + dy*dy);
        double vCircular = Math.sqrt(G * main.mass / r);
        double v2 = vCircular * Math.sqrt((1.0 + eccentricity) / (1.0 - eccentricity));
        this.vx = main.vx + v2 * dy / r;
        this.vy = main.vy - v2 * dx / r;
        this.vx0 = this.vx;
        this.vy0 = this.vy;
        this.e = eccentricity;
        System.out.println(this.main.name);
    }
}
