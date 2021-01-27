package ru.mephi.curvestovector;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;

public class Hough {

    private ArrayList<FeaturePoint> mPoints;
    private Bitmap photo;
    private int minX;
    private int minY;
    private int maxY;
    private int maxX;
    private int height;
    private int maxWid;
    private int minWid;
    private boolean isLine;

    public Hough (Bitmap photo,int minX,int minY,int maxY,int maxX,int height, int maxWid, int minWid, boolean isLine) {
        this.photo = photo;
        this.minX = minX;
        this.minY = minY;
        this.maxY = maxY;
        this.maxX = maxX;
        this.height = height;
        this.maxWid = maxWid;
        this.minWid = minWid;
        this.isLine = isLine;
    }

    public ArrayList<FeaturePoint> getFeaturePoint() {
        mPoints = new ArrayList<>();
        for (int j = minY; j < maxY; j+=1) {
            getFeaturePointsLine(j);
        }

        for (int i = minX; i < maxX; i+=1) {
            getFeaturePointsColumn(i);
        }
        return mPoints;
    }

    private void getFeaturePointsLine(int line) {
        int startX = 0;
        boolean whiteSeries = false;
        int countBlack = 0;
        for (int i = minX; i < maxX; i++) {
            if (isWhite(photo.getPixel(i, getY(line,height,isLine)))) {
                if (!whiteSeries) {
                    startX = i;
                    whiteSeries = true;
                    countBlack = 0;
                }
            } else {
                if (whiteSeries) {
                    countBlack++;
                    if (countBlack == 3) {
                        whiteSeries = false;
                        int length = i - startX;
                        if (length >= minWid && length <= maxWid) {
                            FeaturePoint point = new FeaturePoint(startX + length / 2, line, 1, length);
                            mPoints.add(point);
                        }
                    }
                }
            }
        }
    }

    private void getFeaturePointsColumn(int column) {
        int startY = 0;
        boolean whiteSeria = false;
        int countBlack = 0;
        for (int i = minY; i < maxY; i++) {
            if (isWhite(photo.getPixel(column, getY(i,height,isLine)))) {
                if (!whiteSeria) {
                    startY = i;
                    whiteSeria = true;
                    countBlack = 0;
                }
            } else {
                if (whiteSeria) {
                    countBlack ++;
                    if (countBlack == 3) {
                        whiteSeria = false;
                        int length = i - startY;
                        if (length >= minWid && length <= maxWid) {
                            int horLenght = getHorizontalLenghtPoint(column,i);
                            if (horLenght > maxWid || horLenght < minWid) {
                                FeaturePoint point = new FeaturePoint(column, startY + length / 2, 2, length);
                                mPoints.add(point);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isWhite(int pixel) {
        return pixel == Color.WHITE;
    }

    private int getHorizontalLenghtPoint(int x0, int y0) {
        int rightLen = 0;
        int leftLen = 0;
        for (int x = x0; x < maxX; x++) {
            if (!isWhite(photo.getPixel(x, getY(y0,height,isLine)))) {
                rightLen = x - x0;
                break;
            }
        }
        for (int x = x0; x >= minX; x--) {
            if (!isWhite(photo.getPixel(x, getY(y0,height,isLine)))) {
                leftLen = x0 - x;
                break;
            }
        }
        return leftLen + rightLen;
    }

    private static int getY (int y, int height, boolean isLine) {
        return isLine ? height -1 - y : y;
    }
}
