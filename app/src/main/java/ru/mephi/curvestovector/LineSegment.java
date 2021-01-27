package ru.mephi.curvestovector;

import android.graphics.Point;

public class LineSegment extends Segment {
    private double k;
    private double b;
    /*private Point start;
    private Point end;*/

    public LineSegment(double k, double b, Point p1, Point p2) {
        super(p1,p2);
        this.k = k;
        this.b = b;
        if (p1.x < p2.x) {
            start = p1;
            end = p2;
        } else if (p1.x > p2.x) {
            start = p2;
            end = p1;
        } else {
            if (p1.y < p2.y) {
                start = p1;
                end = p2;
            } else {
                start = p2;
                end = p1;
            }
        }
    }

    public LineSegment(LineSegment segment, int duration, boolean flag) {
        super();
        k = segment.k;
        b = segment.b;
        if (flag) {
           start = segment.start;
           Point p = new Point();
           p.x = start.x + duration;
           p.y =(int) Math.round(k * p.x + b);
           end = p;
        } else {
            end = segment.end;
            Point p = new Point();
            p.x = end.x - duration;
            p.y =(int) Math.round(k * p.x + b);
            start = p;
        }
    }



    public double getK() {
        return k;
    }

    public double getB() {
        return b;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    /*@Override
    public int getDuration() {
        return end.x - start.x;
    }

    @Override
    public int getIncrease() {
        return end.y - start.y;
    }*/


    public double getIk(int lenght) {
        if (lenght > getDuration()) {
            throw new IllegalArgumentException("lenght > duration : " + lenght);
        }
        int xk = lenght + start.x;
        return  k * xk + b;
    }

    public double getIncrease(int height,double max) {
        return (end.y - start.y) * max / height;
    }

    @Override
    public double getY(int x) {
        return k * x + b;
    }

    @Override
    public double getX(int y) {
        return (y - b)/k;
    }

    @Override
    public void correct() {
        k = ((double)(end.y-start.y)) / (end.x - start.x);
        b = start.y - k *start.x;
    }
}
