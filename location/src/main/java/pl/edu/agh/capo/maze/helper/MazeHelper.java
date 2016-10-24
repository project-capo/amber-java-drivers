package pl.edu.agh.capo.maze.helper;

import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.maze.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MazeHelper {

    public static List<Room> buildRooms(MazeMap maze) {
        List<Room> rooms = new ArrayList<>();
        for (Space space : maze.getSpaces()) {
            Room room = new Room(getRoomWalls(space.getId(), maze), getRoomGates(space.getId(), maze), space.getId());
            rooms.add(room);
        }
        for (Room room : rooms){
            Map<Integer, Room> gateRooms = new HashMap<>();
            for (int i = 0; i < room.getGates().size(); i++) {
                Room nextRoom = findRoom(room.getGates().get(i).getId(), room.getSpaceId(), rooms, maze);
                gateRooms.put(i, nextRoom);
            }
            room.setGateRooms(gateRooms);
        }
        return rooms;
    }

    private static Room findRoom(String gateId, String roomId, List<Room> rooms, MazeMap maze){
        for (SpaceGate spaceGate : maze.getSpaceGates()){
            if (spaceGate.getGateId().equals(gateId) && !spaceGate.getSpaceId().equals(roomId)){
                return findRoom(spaceGate.getSpaceId(), rooms);
            }
        }
        return null;
    }

    private static Room findRoom(String id, List<Room> rooms){
        for (Room room : rooms){
            if (room.getSpaceId().equals(id)){
                return room;
            }
        }
        return null;
    }

    private static List<Gate> getRoomGates(String spaceId, MazeMap maze) {
        List<Gate> gates = new ArrayList<>();
        for (SpaceGate spaceGate : maze.getSpaceGates()){
            if (!spaceGate.getSpaceId().equals(spaceId)){
                continue;
            }
            Gate gate = getGate(spaceGate.getGateId(), maze.getGates());
            if (gate == null){
                continue;
            }
            gates.add(gate);
        }
        return gates;
    }

    private static Gate getGate(String gateId, List<Gate> gates) {
        for (Gate gate : gates){
            if (gate.getId().equals(gateId)){
                return gate;
            }
        }
        return null;
    }

    private static List<Wall> getRoomWalls(String spaceId, MazeMap maze) {
        List<Wall> walls = new ArrayList<>();
        for (SpaceWall spaceWall : maze.getSpaceWalls()){
            if (!spaceWall.getSpaceId().equals(spaceId)){
                continue;
            }
            Wall wall = getWall(spaceWall.getWallId(), maze.getWalls());
            if (wall == null){
                continue;
            }
            walls.add(wall);
        }
        return walls;
    }

    private static Wall getWall(String wallId, List<Wall> walls) {
        for (Wall wall : walls){
            if (wall.getId().equals(wallId)){
                return wall;
            }
        }
        return null;
    }

    public static double getMinY(List<Wall> walls){
        double min = Double.MAX_VALUE;
        for (Wall wall : walls){
            if (wall.getFrom().getY() < min){
                min = wall.getFrom().getY();
            }
            if (wall.getTo().getY() < min){
                min = wall.getTo().getY();
            }
        }
        return min;
    }

    public static double getMaxY(List<Wall> walls){
        double max = Double.MIN_VALUE;
        for (Wall wall : walls){
            if (wall.getFrom().getY() > max){
                max = wall.getFrom().getY();
            }
            if (wall.getTo().getY() > max){
                max = wall.getTo().getY();
            }
        }
        return max;
    }

    public static double getMinX(List<Wall> walls){
        double min = Double.MAX_VALUE;
        for (Wall wall : walls){
            if (wall.getFrom().getX() < min){
                min = wall.getFrom().getX();
            }
            if (wall.getTo().getX() < min){
                min = wall.getTo().getX();
            }
        }
        return min;
    }

    public static double getMaxX(List<Wall> walls){
        double max = Double.MIN_VALUE;
        for (Wall wall : walls){
            if (wall.getFrom().getX() > max){
                max = wall.getFrom().getX();
            }
            if (wall.getTo().getX() > max){
                max = wall.getTo().getX();
            }
        }
        return max;
    }

}
