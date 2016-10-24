package pl.edu.agh.capo.hough.jni;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.capo.common.Line;
import pl.edu.agh.capo.common.Section;
import pl.edu.agh.capo.common.Vision;
import pl.edu.agh.capo.hough.HoughTransform;
import pl.edu.agh.capo.robot.CapoRobotConstants;
import pl.edu.agh.capo.robot.Measure;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class KernelBasedHoughTransform implements HoughTransform {

    private static final Logger logger = LoggerFactory.getLogger(JniKernelHough.class);

    private KhtResult result;

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public void run(Measure measure, int threshold, int max) {
        VisionImage visionImage = new VisionImage(feasibleVisions(measure.getVisions()));
        try {
            result = new JniKernelHough().kht(visionImage.toByteArray(),
                    visionImage.getSize(),
                    visionImage.getSize(),
                    max,
                    CapoRobotConstants.KHT_CLUSTER_MIN_SIZE,
                    CapoRobotConstants.KHT_CLUSTER_MIN_DEVIATION,
                    CapoRobotConstants.KHT_DELTA,
                    CapoRobotConstants.KHT_KERNEL_MIN_HEIGHT,
                    CapoRobotConstants.KHT_N_SIGMAS);


            //visionImage.writeToFileWithLines("vision-with-lines.bmp", getLines());
            //visionImage.writeToFile("vision.bmp");

            visionImage.translateLines(result.getLines());

            //if (lines.get(0).getRawRho() >0 && lines.get(0).getRawTheta() > 90) {
            //System.exit(1);
            //}
        } catch (Throwable e) {
            logger.error("Could not extraxt lines", e);
        }
    }

    private List<Vision> feasibleVisions(List<Vision> visions) {
        return visions.stream()
                .filter(vision -> vision.getDistance() < CapoRobotConstants.MAX_VISION_DISTANCE)
                .collect(Collectors.toList());

    }

    @Override
    public List<Line> getLines() {
        return result.getLines();
    }

    @Override
    public List<Section> getSections() {
        return result.getSections();
    }
}
