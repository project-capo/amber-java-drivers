package pl.edu.agh.capo.maze;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MazeMap {

    private List<Wall> walls;

    private List<Gate> gates;

    private List<Space> spaces;

    private List<Node> nodes;

    @SerializedName("space-walls")
    private List<SpaceWall> spaceWalls;

    @SerializedName("space-gates")
    private List<SpaceGate> spaceGates;

    @SerializedName("space-nodes")
    private List<SpaceNode> spaceNodes;

    @SerializedName("gate-nodes")
    private List<GateNode> gateNodes;

    @SerializedName("node-nodes")
    private List<NodeNode> nodeNodes;


    public List<SpaceWall> getSpaceWalls() {
        return spaceWalls;
    }

    public void setSpaceWalls(List<SpaceWall> spaceWalls) {
        this.spaceWalls = spaceWalls;
    }

    public List<Wall> getWalls() {
        return walls;
    }

    public void setWalls(List<Wall> walls) {
        this.walls = walls;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public void setGates(List<Gate> gates) {
        this.gates = gates;
    }

    public List<Space> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<Space> spaces) {
        this.spaces = spaces;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<SpaceGate> getSpaceGates() {
        return spaceGates;
    }

    public void setSpaceGates(List<SpaceGate> spaceGates) {
        this.spaceGates = spaceGates;
    }

    public List<SpaceNode> getSpaceNodes() {
        return spaceNodes;
    }

    public void setSpaceNodes(List<SpaceNode> spaceNodes) {
        this.spaceNodes = spaceNodes;
    }

    public List<GateNode> getGateNodes() {
        return gateNodes;
    }

    public void setGateNodes(List<GateNode> gateNodes) {
        this.gateNodes = gateNodes;
    }

    public List<NodeNode> getNodeNodes() {
        return nodeNodes;
    }

    public void setNodeNodes(List<NodeNode> nodeNodes) {
        this.nodeNodes = nodeNodes;
    }
}
