package pl.edu.agh.capo.robot;


import pl.edu.agh.capo.common.Line;
import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.common.Section;
import pl.edu.agh.capo.common.Vision;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Measure {
    private static final int VISION_JUMP = 24;     //24 for 30 readings

    private final double leftVelocity;
    private final double rightVelocity;
    private final Date datetime;
    private final List<Vision> visions;
    private List<Line> lines;
    private List<Section> sections;
    private List<Double> angles;

    public Measure(Date datetime, double rightVelocity, double leftVelocity, List<Vision> visions) {
        this.leftVelocity = milistoMeters(leftVelocity);
        this.rightVelocity = milistoMeters(rightVelocity);
        this.datetime = datetime;
        this.visions = visions;
    }

    private double milistoMeters(double velocity) {
        return velocity / 1000;
    }

    public double getLeftVelocity() {
        return leftVelocity;
    }

    public double getRightVelocity() {
        return rightVelocity;
    }

    public List<Vision> getVisions() {
        return new CopyOnWriteArrayList<>(visions);
    }

    public List<Vision> getVisionsProbe() {
        List<Vision> filteredVisions = new ArrayList<>();
        for (int i = 0; i < visions.size(); i += VISION_JUMP) {
            filteredVisions.add(visions.get(i));
        }
        return filteredVisions;
    }

    public long getDatetime() {
        return datetime.getTime();
    }

    @Override
    public String toString() {
        return "Measure{" +
                "leftVelocity=" + leftVelocity +
                ", rightVelocity=" + rightVelocity +
                ", datetime=" + datetime +
                ", visions=" + visions +
                '}';
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
        this.angles = new LinkedList<>();
        lines.forEach(this::prepareAngles);
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public void prepareAngles(Line line) {
        double theta = line.getTheta();
        angles.add(Location.normalizeAlpha(90 - theta));
        angles.add(Location.normalizeAlpha(180 - theta));
        angles.add(Location.normalizeAlpha(270 - theta));
        angles.add(Location.normalizeAlpha(-theta));
    }

    public List<Double> getAngles() {
        return angles;
    }
}
