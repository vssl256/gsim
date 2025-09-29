public class Body {

    public double x, y;
    public double vx, vy;
    public double mass;
    public double radius;
    public double x0, y0;
    public double vx0, vy0;
    public double e;

    public Body(double x, double y, double mass, double radius) {
        this.x = this.x0 = x;
        this.y = this.y0 = y;
        this.vx = this.vx0 = vx = 0;
        this.vy = this.vy0 = vy = 0;
        this.mass = mass;
        this.radius = radius;
    }
    public void reset() {
        this.x = x0;
        this.y = y0;
        this.vx = vx0;
        this.vy = vy0;
    }
    public void setOrbit(Body main, double G, double eccentricity) {
        double dx = this.x - main.x;
        double dy = this.y - main.y;
        double r = Math.sqrt(dx*dx + dy*dy);
        double vCircular = Math.sqrt(G * main.mass / r);
        double v = vCircular * Math.sqrt((1 + eccentricity) / (1 - eccentricity));
        this.vx = main.vx + v * dy / r;
        this.vy = main.vy - v * dx / r;
        this.vx0 = this.vx;
        this.vy0 = this.vy;
        this.e = eccentricity;
    }
}
