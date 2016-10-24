package pl.edu.agh.capo.hough;

import pl.edu.agh.capo.common.Line;
import pl.edu.agh.capo.common.Section;
import pl.edu.agh.capo.robot.Measure;

import java.util.List;

public interface HoughTransform {
    void run(Measure measure, int threshold, int max);

    List<Line> getLines();

    List<Section> getSections();
}
