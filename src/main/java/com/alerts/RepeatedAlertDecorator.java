package com.alerts;

public class RepeatedAlertDecorator extends AlertDecorator {
    private int repeatCount = 0;
    private static final int MAX_REPEATS = 3;

    public RepeatedAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }    public void checkAndRepeat() {
        if (repeatCount < MAX_REPEATS) {
            System.out.println("Repeating Alert: " + getCondition());
            repeatCount++;
        }
    }
    
    @Override
    public String getCondition() {
        return "[Repeated " + repeatCount + " times] " + decoratedAlert.getCondition();
    }
}