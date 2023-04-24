package orbits.math;

public class Ball {
    private final Position position = new Position(0, 0);
    private double radius;

    public void radius(double radius) {
        this.radius = radius;
    }

    public double radius() {
        return radius;
    }

    public Position position() {
        return position;
    }
}
