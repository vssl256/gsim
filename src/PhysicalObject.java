public abstract class PhysicalObject {
    protected String name;
    protected String color;
    protected double x, y, x0, y0, vx, vy, vx0, vy0, mass, e;
    protected Body main;
    protected boolean initialized = false;
}
