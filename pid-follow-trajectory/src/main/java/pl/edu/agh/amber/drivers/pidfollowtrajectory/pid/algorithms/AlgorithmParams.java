package pl.edu.agh.amber.drivers.pidfollowtrajectory.pid.algorithms;

/**
 * Created by kubicz10 on 3/11/15.
 */
public class AlgorithmParams {
    private double lookahead; //maksymalna suma dlugosci rozwazanych odcinkow
    private double iAlpha; //waga odleglosci katowej,razem z iTrack normalizowane do 1.0 przed zaaplikowaniem do wzoru na promien skretu
    private double iTrack; //waga odleglosci liniowej, razem z iAlpha normalizowane do 1.0 przed zaaplikowaniem do wzoru na promien skretu
    private double centreAcceleration; //maksymalne dopuszczalne przyspieszenie odsrodkowe, umozliwia pokonywanie zakretow z bezpieczna predkoscia
    private double maxLinearVelocity; //maksymalna predkosc liniowa
    private int bezierSegmentsNumber; //liczba odcinkow na ktore dzielony jest pojedynczy odcinek trajektoii wejsciowej przy wygladzaniu
    private double scale; //todo: learn WTF IS THIS
    private int loopSleepTime; //czas co ile ma sie wykonywac glowna petla

    public AlgorithmParams(){}

    public AlgorithmParams(double lookahead, double iAlpha, double iTrack, double centreAcceleration, double maxLinearVelocity, int bezierSegmentsNumber, boolean pointsWithTime, double scale, int loopSleepTime) {
        this.lookahead = lookahead;
        this.iAlpha = iAlpha;
        this.iTrack = iTrack;
        this.centreAcceleration = centreAcceleration;
        this.maxLinearVelocity = maxLinearVelocity;
        this.bezierSegmentsNumber = bezierSegmentsNumber;
        this.pointsWithTime = pointsWithTime;
        this.scale = scale;
        this.loopSleepTime = loopSleepTime;
    }

    private boolean pointsWithTime; //okresla tryb algorytmu, jesli true to predkosc jest zmienna, dla kazdego punktu jest podany czas

    public double getLookahead() {
        return lookahead;
    }

    public void setLookahead(double lookahead) {
        this.lookahead = lookahead;
    }

    public double getiAlpha() {
        return iAlpha;
    }

    public void setiAlpha(double iAlpha) {
        this.iAlpha = iAlpha;
    }

    public double getiTrack() {
        return iTrack;
    }

    public void setiTrack(double iTrack) {
        this.iTrack = iTrack;
    }

    public double getCentreAcceleration() {
        return centreAcceleration;
    }

    public void setCentreAcceleration(double centreAcceleration) {
        this.centreAcceleration = centreAcceleration;
    }

    public double getMaxLinearVelocity() {
        return maxLinearVelocity;
    }

    public void setMaxLinearVelocity(double maxLinearVelocity) {
        this.maxLinearVelocity = maxLinearVelocity;
    }

    public int getBezierSegmentsNumber() {
        return bezierSegmentsNumber;
    }

    public void setBezierSegmentsNumber(int bezierSegmentsNumber) {
        this.bezierSegmentsNumber = bezierSegmentsNumber;
    }

    public boolean isPointsWithTime() {
        return pointsWithTime;
    }

    public void setPointsWithTime(boolean pointsWithTime) {
        this.pointsWithTime = pointsWithTime;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public int getLoopSleepTime() {
        return loopSleepTime;
    }

    public void setLoopSleepTime(int loopSleepTime) {
        this.loopSleepTime = loopSleepTime;
    }

    @Override
    public String toString() {
        return "AlgorithmParams{" +
                "lookahead=" + lookahead +
                ", iAlpha=" + iAlpha +
                ", iTrack=" + iTrack +
                ", centreAcceleration=" + centreAcceleration +
                ", maxLinearVelocity=" + maxLinearVelocity +
                ", bezierSegmentsNumber=" + bezierSegmentsNumber +
                ", scale=" + scale +
                ", loopSleepTime=" + loopSleepTime +
                ", pointsWithTime=" + pointsWithTime +
                '}';
    }

}
