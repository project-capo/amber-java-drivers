package pl.edu.agh.capo.logic.scheduler.divider;

import pl.edu.agh.capo.logic.Agent;
import pl.edu.agh.capo.logic.Room;
import pl.edu.agh.capo.logic.fitness.AbstractFitnessEstimator;

import java.util.List;

public class EnergyTimeDivider extends AbstractTimeDivider {
    public EnergyTimeDivider(List<Room> rooms, Class<? extends AbstractFitnessEstimator> estimator, int intervalTime) {
        super(rooms, estimator, intervalTime);
    }

    @Override
    protected AgentFactorInfo createAgentInfo(int index, Agent agent) {
        return new AgentFactorInfo(index, agent) {
            @Override
            protected void resetFactor() {
                agent.resetEnergy();
            }

            @Override
            protected double estimatedFactor() {
                agent.recalculateEnergy();
                return agent.getEnergy();
            }
        };
    }

    @Override
    public double getFactor(Agent agent) {
        return agent.getEnergy();
    }
}
