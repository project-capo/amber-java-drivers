package pl.edu.agh.capo.logic.estimator;

import pl.edu.agh.capo.common.Line;
import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.robot.Measure;

import java.util.*;
import java.util.stream.Stream;

public class PerpendicularLinesLocationEstimator {
    private final Room room;
    private CountItemsList<Location> locations = new CountItemsList<>();

    public PerpendicularLinesLocationEstimator(Room room, Measure measure) {
        this.room = room;
        int index = 0;
        List<Line> lines = measure.getLines();
        for (Line next : lines) {
            if (index == lines.size() - 1) {
                locations = locations.sortByValue();
                return;
            }
            for (int i = index; i < lines.size(); i++) {
                Line line = lines.get(i);
                if (line.isPerpendicularTo(next)) {
                    if (line.getTheta() < next.getTheta()) {
                        addCornerLocations(next, line);
                    } else {
                        addCornerLocations(line, next);
                    }
                }
            }
            index++;
        }
    }

    private void addCornerLocations(Line left, Line right) {
        double theta = right.getTheta();
        //int size = locations.size();
        addCornerLocations(left, right, 90 - theta);
        //System.out.println("THETA: " + (locations.size() - size));

        //size = locations.size();
        addCornerLocations(left, right, 180 - theta);
/*        if(locations.size() - size > 0) {
            System.out.println(left.getTheta() - right.getTheta());
        }*/
        //System.out.println("THETA + 90: " + (locations.size() - size));

        //size = locations.size();
        addCornerLocations(left, right, 270 - theta);
        //System.out.println("THETA + 180: " + (locations.size() - size));

        //size = locations.size();
        addCornerLocations(left, right, -theta);
/*        if(locations.size() - size > 0) {
            System.out.println(left.getTheta() - right.getTheta());
        }*/
        //System.out.println("THETA + -90: " + (locations.size() -size));
    }

    private void addCornerLocations(Line left, Line right, double angle) {
        addLocation(new Location(room.getMaxX() - right.getRho(), room.getMinY() + left.getRho(), angle));
        addLocation(new Location(room.getMaxX() - left.getRho(), room.getMaxY() - right.getRho(), angle));
        addLocation(new Location(room.getMinX() + right.getRho(), room.getMaxY() - left.getRho(), angle));
        addLocation(new Location(room.getMinX() + left.getRho(), room.getMinY() + right.getRho(), angle));
    }

    private void addLocation(Location location) {
        if (room.coordinatesMatches(location.positionX, location.positionY)) {
            locations.add(location);
        }
    }

    public int size() {
        return locations.size();
    }

    public Location pop() {
        Location location = locations.stream().findAny().get();
        locations.remove(location);
        return location;
    }

    public class CountItemsList<E> extends ArrayList<E> {

        private final Map<E, Integer> count = new HashMap<>();

        public boolean add(E element) {
            if (!count.containsKey(element)) {
                count.put(element, 1);
                return super.add(element);
            } else {
                count.put(element, count.get(element) + 1);
            }
            return false;
        }

        @Override
        public void clear() {
            super.clear();
            count.clear();
        }

        public CountItemsList<E> sortByValue() {
            CountItemsList<E> result = new CountItemsList<>();
            Stream<Map.Entry<E, Integer>> st = count.entrySet().stream().filter(e -> e.getValue() > 1);

            st.sorted(Comparator.comparing(Map.Entry::getValue)).forEachOrdered(e -> result.add(0, e.getKey()));

            return result;
        }
    }
}
