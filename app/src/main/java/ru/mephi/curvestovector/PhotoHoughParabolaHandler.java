package ru.mephi.curvestovector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

public class PhotoHoughParabolaHandler extends AsyncTask<Bitmap, Integer, Bitmap> {

    private HoughParabolaFragment link;
    private Bitmap photo;
    private ArrayList<FeaturePoint> points;
    private Point start;
    private Point end;
    private int width=0;
    private int height=0;
    private double maxX;
    private double maxY;
    private SegmentStore store;
    private int [][][][] accum;
    private int [] kvants = {200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000,
            200, 100, 50};


    public void link(HoughParabolaFragment link) {
        this.link =  link;
    }

    public void unlink() {
        link = null;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        photo = bitmaps[0];
        width = photo.getWidth();
        height = photo.getHeight();
        points = new ArrayList<>();
        maxX = Preference.getXValue(link.getActivity());
        maxY = Preference.getYValue(link.getActivity());
        accum = new int [64][64][64][64];
        androidx.core.util.Pair<Point,Point> controlPoints = link.getPoints();
        start = controlPoints.first;
        end = controlPoints.second;
        int minWidth = Preference.getMinWidth(link.getActivity());
        int maxWidth = Preference.getMaxWidth(link.getActivity());
        TimingLogger logger = new TimingLogger("timermethod","hough parabola");
        Hough hough = new Hough(photo,start.x,start.y, end.y,end.x,height,maxWidth,minWidth,false);
        points = hough.getFeaturePoint();
        logger.addSplit("Feature point");
        //colorRectangle(controlPoints);
        vote();
        logger.addSplit("vote");
        findMax();
        logger.addSplit("max");
        logger.dumpToLog();
        colorRectangle();
        return photo;
    }

    private void colorBitmap(ArrayList<FeaturePoint> points) {
        for (FeaturePoint point : points) {
            photo.setPixel(point.x, point.y, Color.RED);
        }
    }

    private double getRealX(int x) {
        return maxX * x / width;
    }

    private double getRealY(int y) {
        return maxY * y / height;
    }

    private void colorRectangle() {
        /*for (int i = 0;i < width;i++) {
            photo.setPixel(i, start.y,Color.BLUE);
            photo.setPixel(i,end.y,Color.BLUE);
        }
        for (int i = 0;i < height;i++) {
            photo.setPixel(start.x,i,Color.BLUE);
            photo.setPixel(end.x,i,Color.BLUE);
        }*/
        for (int i = start.x; i < end.x;i++) {
            for (int j = start.y; j < end.y;j++ ) {
                photo.setPixel(i,j,Color.BLACK);
            }
        }
    }

    private void vote() {
        int size = points.size();
        int min = Collections.min(points,(c1,c2) -> c1.x - c2.x).x;
        int max = Collections.max(points,(c1,c2) -> c1.x - c2.x).x;
        int lenght = (max - min) *1000;
        int n = 0;
        boolean isMilli;
        int kvant = 0;
        for(int i = kvants.length-1;i>= 0;i-- ) {
            n = lenght / kvants[i];
            if (n < 255) {
                kvant = kvants[i];
                break;
            }
        }
        if (kvant / 1000 != 0) {
            isMilli = true;
            kvant = kvant /1000;
        } else {
            isMilli = false;
        }
        //int i =0;
        Collections.sort(points, (featurePoint, t1) -> featurePoint.x - t1.x);
        for (int i= 0;i < n;i++) {
            for (int b = 0;b < 64;b++) {
                for (int c = 0;c < 64;c++) {
                    for (int d = 0;d< 64;d++) {
                        double y = getRealY(points.get(i*kvant).y) * 249/(maxY);
                        int d0 = -127 + d *4;
                        double b0 = -1 + b * 0.0316;
                        double c0 = -1 + c * 0.0316;
                        double a0 = (y -  b0 * (i - d0) - c0 * Math.pow(i - d0,2) / 256)/2;
                        int a = (int) ((a0 + 127 )* 64 /249);
                        if (a < 0 || a >=64) {
                            continue;
                        }
                        accum[a][b][c][d]++;
                    }
                }
            }
        }
        /*for (FeaturePoint point : points) {
            Log.i("parabola","size: " + size + "; i = " + i);
            for (int b = 0;b < 64;b++) {
                for (int c = 0;c < 64;c++) {
                    for (int d = 0;d< 64;d++) {
                        double x = getRealX(point.x)*249/(maxX);
                        double y = getRealY(point.y) * 249/(maxY);
                        //double y = getRealY(point.y)/4;
                        int d0 = -127 + d *4;
                        double b0 = -1 + b * 0.0316;
                        double c0 = -1 + c * 0.0316;
                        int a = (int) (y -  b0 * (x - d0) - c * Math.pow(x - d0,2) / 256)/2;
                        int a0 = a / 4;
                        if (a0 < 0) {
                            a0 = 0;
                        }
                        accum[a0][b][c][d]++;
                    }
                }
            }
            i++;
        }*/
    }

    private void findMax() {
        int max = -1;
        int a0=-1,b0=-1,c0=-1,d0=-1;
        for (int a = 0;a < 64 ;a++) {
            for (int b = 0;b < 64;b++) {
                for (int c = 0;c < 64;c++) {
                    for (int d = 0;d< 64;d++) {
                        if (accum[a][b][c][d] > max) {
                            max = accum[a][b][c][d];
                            a0 = a;
                            b0 = b;
                            c0 = c;
                            d0 = d;
                        }
                    }
                }
            }
        }
        if (max != -1) {
            SegmentStore store = SegmentStore.getSegmentStore(link.getActivity());
            ParabolaSegment pSegment = new ParabolaSegment(a0 * 4 - 127,-1 + b0 * 0.0316,
                    -1 + c0 * 0.0316,d0 * 4 - 127, start,end);
            store.add(pSegment);
        }

    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        //colorBitmap(points);
        link.mDrawImageView.setActive(true);
        link.mDrawImageView.setBitmap(bitmap);
        link.mButton.setVisibility(View.VISIBLE);
        link.update();
    }
}
