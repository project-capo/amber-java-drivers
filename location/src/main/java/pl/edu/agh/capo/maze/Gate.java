package pl.edu.agh.capo.maze;

public class Gate {

    private String id;

    private String kind;

    private double blocked;

    private Coordinates from;

    private Coordinates to;

    public Coordinates getTo() {
        return to;
    }

    public void setTo(Coordinates to) {
        this.to = to;
    }

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

    public double getBlocked() {
        return blocked;
    }

    public void setBlocked(double blocked) {
        this.blocked = blocked;
    }

    public Coordinates getFrom() {
        return from;
    }

    public void setFrom(Coordinates from) {
        this.from = from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gate gate = (Gate) o;

        if (Double.compare(gate.blocked, blocked) != 0) return false;
        if (id != null ? !id.equals(gate.id) : gate.id != null) return false;
        if (kind != null ? !kind.equals(gate.kind) : gate.kind != null) return false;
        if (from != null ? !from.equals(gate.from) : gate.from != null) return false;
        return !(to != null ? !to.equals(gate.to) : gate.to != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        temp = Double.doubleToLongBits(blocked);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (from != null ? from.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }
}
