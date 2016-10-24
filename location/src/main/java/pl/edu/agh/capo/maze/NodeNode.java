package pl.edu.agh.capo.maze;

public class NodeNode {

    private String nodeFromId;

    private String nodeToId;

    private double cost;

    private double blocked;

    public String getNodeFromId() {
        return nodeFromId;
    }

    public void setNodeFromId(String nodeFromId) {
        this.nodeFromId = nodeFromId;
    }

    public String getNodeToId() {
        return nodeToId;
    }

    public void setNodeToId(String nodeToId) {
        this.nodeToId = nodeToId;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getBlocked() {
        return blocked;
    }

    public void setBlocked(double blocked) {
        this.blocked = blocked;
    }
}
