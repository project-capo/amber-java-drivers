package pl.edu.agh.capo.hough.jni;

import pl.edu.agh.capo.common.Line;
import pl.edu.agh.capo.common.Section;

import java.util.List;

public class KhtResult {
    private final List<Line> lines;
    private final List<Section> sections;

    public KhtResult(List<Line> lines, List<Section> sections) {
        this.lines = lines;
        this.sections = sections;
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<Section> getSections() {
        return sections;
    }
}
