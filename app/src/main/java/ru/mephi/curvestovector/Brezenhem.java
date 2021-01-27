package ru.mephi.curvestovector;

import android.graphics.Point;

import java.util.ArrayList;

public class Brezenhem {

    private ArrayList<Point> points;

    public Brezenhem(Point start, Point end) {
        points = new ArrayList<>();
        int x_Dd=0, y_Dd=0;
        int dx=end.x-start.x;
        int dy=end.y-start.y;
        int d;
        int incX=0;
        int incY=0;
        if (dx>0) {
            incX = 1;
        } else if (dx!=0) {
            incX=-1;
        }
        if (dy>0) {
            incY = 1;
        }else if (dy!=0) {
            incY=-1;
        }
        dx=Math.abs(dx);
        dy=Math.abs(dy);
        d = dx>dy? dx: dy;
        int x=start.x, y=start.y;
        points.add(new Point(x,y));
        for (int i=1; i<=d; i++) {
            x_Dd+=dx; y_Dd+=dy;
            if (x_Dd>=d) {
                x+=incX;
                x_Dd-=d;
            }
            if (y_Dd>=d) {
                y+=incY;
                y_Dd-=d;
            }
            points.add(new Point(x,y));
        }
    }

    public Point getRealPoint(int pos, double xMax,double yMax,int width,int height,int xScale) {
        Point p = points.get(pos);
        return new Point((int) (p.x * xMax * xScale / width),(int) (p.y * yMax / height));
    }

    public Point getPoint(int pos) {
        return points.get(pos);
    }

    public int getSize() {
        return points.size();
    }
}
