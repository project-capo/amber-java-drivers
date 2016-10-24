package pl.edu.agh.capo.maze;

public class Space {

    private String id;

    private String kind;

    private String name;

    private int expectedPersonCount;

    private double area;

    private double diameter;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExpectedPersonCount() {
        return expectedPersonCount;
    }

    public void setExpectedPersonCount(int expectedPersonCount) {
        this.expectedPersonCount = expectedPersonCount;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getDiameter() {
        return diameter;
    }

    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }
}
