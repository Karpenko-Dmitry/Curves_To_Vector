package ru.mephi.curvestovector;

public class FeaturePoint {
    public int x;
    public int y;
    public int weight;
    public int type;

    public FeaturePoint(int x, int y, int type, int weight) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.weight = weight;
    }
}
