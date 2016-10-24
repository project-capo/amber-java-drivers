package pl.edu.agh.capo.robot;

public interface IMeasureReader {
    Measure read();

    boolean isFinished();

    boolean isIdle();
}
