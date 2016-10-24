package pl.edu.agh.capo.logic.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.capo.hough.HoughTransform;
import pl.edu.agh.capo.hough.jni.KernelBasedHoughTransform;
import pl.edu.agh.capo.logic.Agent;
import pl.edu.agh.capo.logic.scheduler.divider.AbstractTimeDivider;
import pl.edu.agh.capo.robot.CapoRobotConstants;
import pl.edu.agh.capo.robot.IMeasureReader;
import pl.edu.agh.capo.robot.Measure;

public class Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
    private static final HoughTransform houghTransform = new KernelBasedHoughTransform();
    private final AbstractTimeDivider divider;
    private final IMeasureReader measureReader;

    private UpdateMeasureListener listener;
    private Measure currentMeasure;
    private double millisSinceLastMeasure = 0.0;
    private OnFinishListener onFinishListener;

    public Scheduler(AbstractTimeDivider divider, IMeasureReader measureReader) {
        this.divider = divider;
        this.measureReader = measureReader;
    }

    private synchronized void startWorker(Worker worker) {
        Thread workerThread = new Thread(worker);
        workerThread.start();
        divider.recalculate();
        try {
            Thread startThread = new Thread(this::start);
            wait();
            startThread.start();
        } catch (InterruptedException e) {
            logger.error("Could not wait for worker", e);
        }
    }

    public void start() {
        if (!measureReader.isFinished()) {
            divider.updateAgents();
            if (measureReader.isIdle()) {
                startWorker(new Worker());
            } else {
                calculateMeasuresTimeDifference(measureReader.read());
                startWorker(new MeasureWorker());
            }
        } else if (onFinishListener != null) {
            onFinishListener.onFinish();
        }
    }

    private void calculateMeasuresTimeDifference(Measure measure) {
        if (this.currentMeasure != null) {
            millisSinceLastMeasure = measure.getDatetime() - this.currentMeasure.getDatetime();
        }
        this.currentMeasure = measure;
    }

    public void setListener(UpdateMeasureListener listener) {
        this.listener = listener;
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public interface UpdateMeasureListener {
        void onUpdate();
    }

    public interface OnFinishListener {
        void onFinish();
    }

    private class MeasureWorker extends Worker {

        @Override
        protected void estimatePosition(long currentTime) {
            currentAgent.setMeasure(currentMeasure, millisSinceLastMeasure);
            super.estimatePosition(currentTime);
        }

        @Override
        public void run() {
            // long time = System.currentTimeMillis();

            houghTransform.run(currentMeasure, CapoRobotConstants.HOUGH_THRESHOLD, CapoRobotConstants.HOUGH_MAX_LINES_COUNT);
            currentMeasure.setLines(houghTransform.getLines());
            currentMeasure.setSections(houghTransform.getSections());
            super.run();
            //long end = System.currentTimeMillis();
            // logger.debug("ovetime: " + (end - time - CapoRobotConstants.INTERVAL_TIME));
        }
    }

    private class Worker implements Runnable {
        private final long[] nanoTimes;

        protected Agent currentAgent;
        private long currentTime;
        private long currentStartTime;

        private Worker() {
            this.nanoTimes = divider.getTimes();
        }

        private void updateAgent(AbstractTimeDivider.AgentFactorInfo info) {
            this.currentAgent = info.getAgent();
            long currentTime = nanoTimes[info.getIndex()];
            if (currentTime > 0) {
                estimatePosition(currentTime);
            }
        }

        protected void estimatePosition(long currentTime) {
            //currentAgent.prepare();
            double countFactor = CapoRobotConstants.ENHANCEMENT_TIME_FACTOR_MIN +
                    CapoRobotConstants.ENHANCEMENT_TIME_FACTOR_RANGE_SIZE * currentAgent.getFitness();
            long countTime = (long) (countFactor * currentTime);
            resetTime(countTime);
            if (currentMeasure.getAngles().size() > 0) {
                calculateWithAnglesUntilTimeLeft();
                int timeLeft = (int) (currentTime - (System.nanoTime() - currentStartTime));
                resetTime(timeLeft);
                estimateRandomWithAnglesUntilTimeLeft();
            } else {
                calculateUntilTimeLeft();
                int timeLeft = (int) (currentTime - (System.nanoTime() - currentStartTime));
                resetTime(timeLeft);
                estimateRandomUntilTimeLeft();
            }
        }

        private void calculateWithAnglesUntilTimeLeft() {
            try {
                checkTime();
                while (true) {
                    currentAgent.estimateWithAnglesInNeighbourhood();
                    checkTime();
                }
            } catch (TimeoutException e) {
            }
        }

        private void calculateUntilTimeLeft() {
            try {
                checkTime();
                while (true) {
                    currentAgent.estimateInNeighbourhood();
                    checkTime();
                }
            } catch (TimeoutException e) {
            }
        }

        private void resetTime(long time) {
            this.currentTime = time;
            this.currentStartTime = System.nanoTime();
        }

        private void estimateRandomWithAnglesUntilTimeLeft() {
            try {
                currentAgent.prepareCalculations();
                while (currentAgent.calculate()) {
                    checkTime();
                }
                while (true) {
                    currentAgent.estimateRandomWithAngles();
                    checkTime();
                }
            } catch (TimeoutException e) {
            }
        }

        private void estimateRandomUntilTimeLeft() {
            try {
                currentAgent.prepareCalculations();
                while (currentAgent.calculate()) {
                    checkTime();
                }
                while (true) {
                    currentAgent.estimateRandom();
                    checkTime();
                }
            } catch (TimeoutException e) {
            }
        }

        private void checkTime() throws TimeoutException {
            long diff = System.nanoTime() - currentStartTime;
            if (diff >= currentTime) {
                throw new TimeoutException();
            }
        }

        @Override
        public void run() {
            divider.getAgentFactorInfos().forEach(this::updateAgent);
            synchronized (Scheduler.this) {
                Scheduler.this.notify();
        }
            if (listener != null) {
                listener.onUpdate();
        }
    }

        private class TimeoutException extends Exception {
        }
    }
}