package com.alerts;

public class PriorityAlertDecorator extends AlertDecorator {
    private int priorityLevel = 1;

    public PriorityAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    public void setPriority(int level) {
        this.priorityLevel = level;
    }

    @Override
    public String getCondition() {
        return "[Priority " + priorityLevel + "] " + decoratedAlert.getCondition();
    }
}