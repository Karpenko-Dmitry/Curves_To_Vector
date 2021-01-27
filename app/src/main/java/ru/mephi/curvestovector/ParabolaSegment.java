package ru.mephi.curvestovector;

import android.graphics.Point;

public class ParabolaSegment extends Segment {

    private int a;
    private double b;
    private double c;
    private int d;
    private int step;
    private boolean isMilli;

    public int getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    public int getD() {
        return d;
    }

    public ParabolaSegment(int a, double b, double c, int d, Point start, Point end) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.start = start;
        this.end = end;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getIncrease() {
        return 0;
    }

    @Override
    public double getY(int x) {
        return 0;
    }

    @Override
    public double getX(int y) {
        return 0;
    }

    @Override
    public void correct() {

    }

}
