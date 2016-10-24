package pl.edu.agh.capo.logic.scheduler.divider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.capo.common.Location;
import pl.edu.agh.capo.logic.Agent;
import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.logic.fitness.AbstractFitnessEstimator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class AbstractTimeDivider {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTimeDivider.class);
    private final long intervalTime;

    private final List<Room> rooms;
    private final List<Agent> agents = new CopyOnWriteArrayList<>();
    private final List<AgentFactorInfo> agentFactorInfos = new LinkedList<>();
    private final Class<? extends AbstractFitnessEstimator> estimatorClass;
    protected int agentCount = 0;
    protected double intervalFactorSum;
    // Current interval
    private long[] currentIntervalTimes;
    private boolean newInterval;
    private AgentFactorInfo best;

    public AbstractTimeDivider(List<Room> rooms, Class<? extends AbstractFitnessEstimator> estimator, int intervalTimeInMillis) {
        this.rooms = rooms;
        this.intervalTime = TimeUnit.MILLISECONDS.toNanos(intervalTimeInMillis);
        this.estimatorClass = estimator;
        this.rooms.forEach(this::buildAgent);
        reinitializeCurrentIntervalTimes();
        updateTheBest();
    }

    private void buildAgent(Room room) {
        addAgent(new Agent(estimatorClass, room));
    }

    private void reinitializeCurrentIntervalTimes() {
        currentIntervalTimes = new long[agentCount];
    }

    public void reset() {
        newInterval = true;
    }

    protected abstract AgentFactorInfo createAgentInfo(int index, Agent agent);

    private void addAgent(Agent agent) {
        agents.add(agent);
        AgentFactorInfo agentFactorInfo = createAgentInfo(agentFactorInfos.size(), agent);
        agentFactorInfos.add(agentFactorInfo);
        agentCount++;
    }

    private void updateFactor(AgentFactorInfo info) {
        info.updateFactor();
        intervalFactorSum += info.getFactor();
    }

    public List<AgentFactorInfo> getAgentFactorInfos() {
        return agentFactorInfos;
    }

    public void updateAgents() {
        agentFactorInfos.forEach(this::updateFactor);
        updateTheBest();
        addSearchedAgents();
        removeExcessAgents();
        addAgentInEmptyRooms();
        updateIndexes();
        intervalFactorSum = 0.0;
        newInterval = false;
    }

    protected void addAgentInEmptyRooms() {
        List<Room> filledRooms = agentFactorInfos.stream().map(agentFactorInfo -> agentFactorInfo.getAgent().getRoom()).collect(Collectors.toList());
        rooms.stream().filter(room -> !filledRooms.contains(room)).forEach(this::buildAgent);
        reinitializeCurrentIntervalTimes();
    }

    private void updateIndexes() {
        for (int i = 0; i < agentCount; i++) {
            agentFactorInfos.get(i).index = i;
        }
    }

    private void addSearchedAgents() {
        Map<Room, List<AgentFactorInfo>> agentsByRoom = agentFactorInfos.stream().collect(
                Collectors.groupingBy(agentFactorInfo -> agentFactorInfo.getAgent().getRoom()));
        agentsByRoom.forEach((room, list) -> addAgentsIfNeeded(list));
    }

    private void addAgentsIfNeeded(List<AgentFactorInfo> agentFactorInfos) {
        double maxFitness = agentFactorInfos.stream().max((a1, a2) -> Double.compare(a1.getAgent().getFitness(), a2.getAgent().getFitness())).get().getAgent().getFitness();
        AgentFactorInfo searcher = null;
        for (AgentFactorInfo agentFactorInfo : agentFactorInfos) {
            if (agentFactorInfo.getAgent().isBetterLocationInRoom(maxFitness) && noneFollowsSameHypothesis(agentFactorInfos, agentFactorInfo)) {
                maxFitness = agentFactorInfo.getAgent().getBestLocationFitness();
                searcher = agentFactorInfo;
            }
        }
        addOrReplaceAgentIfNeeded(agentFactorInfos, searcher);
    }

    private boolean noneFollowsSameHypothesis(List<AgentFactorInfo> agentFactorInfos, AgentFactorInfo searcher) {
        Location location = searcher.getAgent().getBestLocationInRoom();
        double fitness = searcher.getAgent().getBestLocationFitness();
        for (AgentFactorInfo info : agentFactorInfos) {
            Agent agent = info.getAgent();
            if (agent.getLocation().inNeighbourhoodOf(location)) {
                agent.setLocation(location, fitness);
                return false;
            }
        }
        return true;
    }

    private void addOrReplaceAgentIfNeeded(List<AgentFactorInfo> agentFactorInfos, AgentFactorInfo searcher) {
        if (searcher != null) {
            AgentFactorInfo max = agentFactorInfos.stream().max((a1, a2) -> Double.compare(a1.getFactor(), a2.getFactor())).get();
            addBetterAgent(searcher, max.getFactor());
        }
    }

    private void addBetterAgent(AgentFactorInfo searcher, double maxFactor) {
        Location location = searcher.getAgent().getBestLocationInRoom();
        addAgent(createBestFitAgentInRoom(searcher.getAgent().getRoom(), location, maxFactor));
    }

    private Agent createBestFitAgentInRoom(Room room, Location bestLocationInRoom, double energy) {
        return new Agent(estimatorClass, room, bestLocationInRoom, energy * 0.51);
    }


    private void removeExcessAgents() {
        Map<Room, List<AgentFactorInfo>> agentsByRoom = agentFactorInfos.stream().collect(
                Collectors.groupingBy(agentFactorInfo -> agentFactorInfo.getAgent().getRoom()));
        agentsByRoom.forEach((room, list) -> removeAgentsIfNeeded(list));
    }

    private void removeAgentsIfNeeded(List<AgentFactorInfo> agentFactorInfos) {
        if (agentFactorInfos.size() > 1) {
            AgentFactorInfo maxFactor = agentFactorInfos.stream().max((a1, a2) -> Double.compare(a1.getFactor(), a2.getFactor())).get();
            agentFactorInfos.stream().filter(info -> !info.equals(maxFactor)).filter(info -> info.getFactor() <= maxFactor.getFactor() * 0.5 || maxFactor.followsSameHyphotesis(info)).forEach(this::removeAgent);
        }
    }

    private void removeAgent(AgentFactorInfo agentFactorInfoToRemove) {
        agents.remove(agentFactorInfoToRemove.getAgent());
        agentFactorInfos.remove(agentFactorInfoToRemove);
        agentCount--;
    }

    public long[] getTimes() {
        return currentIntervalTimes;
    }

    public void recalculate() {
        if (intervalFactorSum > 0.0) {
            final long timeToDivide = intervalTime - distributedTimeToStarvingAgents();
            agentFactorInfos.stream().filter(a -> a.getFactor() > 0).forEach(i -> setTime(i, i.calculateTime(timeToDivide)));
        } else {
            agentFactorInfos.forEach(i -> setTime(i, intervalTime / agentCount));
        }
    }

    private int distributedTimeToStarvingAgents() {
        int distributedTime = 0;
        for (AgentFactorInfo agentInfo : agentFactorInfos) {
            if (agentInfo.isStarved()) {
                long time = intervalTime / agentCount;
                setTime(agentInfo, time);
                distributedTime += time;
            }
        }
        return distributedTime;
    }

    private void setTime(AgentFactorInfo agentFactorInfo, long time) {
        if (time > 0) {
            agentFactorInfo.sleptIterations = 0;
        }
        currentIntervalTimes[agentFactorInfo.index] = time;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public AgentFactorInfo getBest() {
        return best;
    }

    private void updateTheBest() {
        AgentFactorInfo best = agentFactorInfos.stream()
                .max((a1, a2) -> Double.compare(a1.getFactor(), a2.getFactor()))
                .get();
        if (best == null) {
            best = agentFactorInfos.stream().findAny().get();
        }
        agents.forEach(agent -> agent.setIsTheBest(false));
        best.agent.setIsTheBest(true);
        this.best = best;
    }

    public abstract double getFactor(Agent agent);

    public abstract class AgentFactorInfo {
        private static final int MAX_SLEPT_ITERATIONS = 5;
        private final Agent agent;
        protected double factor;
        private int index;
        private int sleptIterations = MAX_SLEPT_ITERATIONS;

        public AgentFactorInfo(int index, Agent agent) {
            this.index = index;
            this.agent = agent;
        }

        public Agent getAgent() {
            return agent;
        }

        public int getIndex() {
            return index;
        }

        public double getFactor() {
            return factor;
        }

        private boolean isStarved() {
            return ++sleptIterations > AgentFactorInfo.MAX_SLEPT_ITERATIONS;
        }

        private int calculateTime(long timeToDivide) {
            return (int) (timeToDivide * factor / intervalFactorSum);
        }

        private void updateFactor() {
            if (newInterval) {
                resetFactor();
            }
            this.factor = estimatedFactor();
        }

        protected abstract void resetFactor();

        protected abstract double estimatedFactor();

        public boolean followsSameHyphotesis(AgentFactorInfo agentFactorInfo) {
            return getAgent().getLocation().inNeighbourhoodOf(agentFactorInfo.getAgent().getLocation());
        }

        @Override
        public String toString() {
            return "AgentFactorInfo{" +
                    "agent=" + agent +
                    ", factor=" + factor +
                    ", index=" + index +
                    ", sleptIterations=" + sleptIterations +
                    '}';
        }
    }
}
