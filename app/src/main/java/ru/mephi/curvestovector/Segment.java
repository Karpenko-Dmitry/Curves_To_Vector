package ru.mephi.curvestovector;

import android.graphics.Point;

public abstract class Segment {

    protected Point start;
    protected Point end;

    public Segment() {
    }

    public Segment(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public int getDuration() {
        return end.x - start.x;
    }

    public int getIncrease() {
        return end.y - start.y;
    }

    public abstract double getY(int x);

    public abstract double getX(int y);

    public abstract void correct();

    public void setStart(Point start) {
        this.start = start;
    }


}
