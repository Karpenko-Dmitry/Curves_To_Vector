package ru.mephi.curvestovector;

import android.graphics.Point;
import android.util.Log;

import androidx.core.util.Pair;

import java.util.Iterator;

public class BrezenhemOld implements Iterator<Point> {

    private int x;
    private int y;
    private int x_Dd=0, y_Dd=0;
    private int d;
    private int incX=0, incY=0;
    private int dx;
    private int dy;
    private int i = 0;
    private int width = -1;
    private int height = -1;

    public BrezenhemOld(Point start, Point end) {
        init(start, end);
    }

    private void init(Point start, Point end){
        dx=end.x-start.x;
        dy=end.y-start.y;
        if (dx > 0) {
            incX = 1;
        } else if (dx != 0) {
            incX=-1;
        }
        if (dy > 0) {
            incY=1;
        } else if (dy != 0){
            incY=-1;
        }
        dx=Math.abs(dx);
        dy=Math.abs(dy);
        d = dx > dy ? dx : dy;
        x=start.x;
        y=start.y;
    }

    public BrezenhemOld(Point point, Pair<Double,Double> equation, boolean posDirection, int w, int h)  {
        width = w -1;
        height = h - 1;
        Pair<Double,Double> eqNormal = getPerpendicular(point,equation);
        Point p1 = getIntersectionPoint(height-1,eqNormal);
        Point p2 = getIntersectionPoint(0,eqNormal);
        Point p3 = getIntersectionPoint(eqNormal, 0);
        Point p4 = getIntersectionPoint(eqNormal,  width-1);
        if (posDirection) {
            if (cheakBound(p1)) {
                init(point,p1);
            } else if (cheakBound(p3)) {
                init(point,p3);
            } else if (cheakBound(p4)) {
                init(point,p4);
            } else {
                init(point,p2);
            }
        } else {
            if (cheakBound(p2)) {
                init(point,p2);
            } else if (cheakBound(p4)) {
                init(point,p4);
            } else if (cheakBound(p3)){
                init(point,p3);
            } else {
                init(point,p1);
            }
        }
    }

    private boolean cheakBound(Point p) {
        return p.x >=0 && p.x <= width && p.y >= 0 && p.y <= height-1;
    }

    private Point getIntersectionPoint(int y,Pair<Double,Double> line1) {
        int x0 = (int) ((y - line1.second)/line1.first);
        return new Point(x0,y);
    }

    private static Point getIntersectionPoint(Pair<Double,Double> line1, int x) {
        int y0 =(int) (line1.first * x + line1.second);
        return new Point(x,y0);
    }

    private Pair<Double,Double> getPerpendicular (Point point,Pair<Double,Double> equation){
        Double k = - 1/equation.first;
        Double b = point.y - k * point.x;
        return Pair.create(k,b);
    }

    @Override
    public boolean hasNext() {
        return i < d;
    }

    public boolean isLast() {
        return i == d;
    }

    @Override
    public Point next() {
        if (i == 0) {
            i++;
            return new Point(x,y);
        } else if (i <= d) {
            i++;
            x_Dd+=dx;
            y_Dd+=dy;
            if (x_Dd>=d) {
                x+=incX;
                x_Dd-=d;
            }
            if (y_Dd>=d) {
                y+=incY;
                y_Dd-=d;
            }
            return new Point(x,y);
        }
        return null;
    }

    public static Point getRealPoint(Point p, double xMax,double yMax,int width,int height,int xScale) {
        return new Point((int) (p.x * xMax * xScale / width),(int) (p.y * yMax / height));
    }
}
