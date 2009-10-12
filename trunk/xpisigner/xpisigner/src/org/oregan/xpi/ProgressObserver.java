package org.oregan.xpi;

public interface ProgressObserver
{
    public void setRange(int min, int max);
    public void setValue(int value);
    public void printMessage(String message);
}
