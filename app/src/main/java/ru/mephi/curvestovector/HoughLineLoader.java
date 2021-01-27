package ru.mephi.curvestovector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.loader.content.AsyncTaskLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class HoughLineLoader extends AsyncTaskLoader<Bitmap> {

    public static final String filePath = "FILE_PATH";
    private Bitmap photo;
    private ArrayList<FeaturePoint> points;
    private Context mContext;
    private int rRange;
    private int width=0;
    private int height=0;
    private Accumulator accum;
    private SegmentStore store;
    private ArrayList<Peak> listPeak;
    private int max=-1;
    private int[] mColors = {Color.BLUE, Color.YELLOW, Color.GREEN, Color.MAGENTA, Color.RED, Color.CYAN, Color.GRAY};

    public HoughLineLoader(@NonNull Context context, Bundle args) {
        super(context);
        mContext = context.getApplicationContext();
        photo = BitmapFactory.decodeFile(args.getString(filePath)).copy(Bitmap.Config.ARGB_8888,true);
    }

    @Nullable
    @Override
    public Bitmap loadInBackground() {
        width = photo.getWidth();
        height = photo.getHeight();
        points = new ArrayList<>();
        Log.i("size_bitmap","width:" + width + "; height:" + height);
        store = SegmentStore.getSegmentStore(mContext);
        initAcc();
        listPeak = new ArrayList<>();
        int minWidth = Preference.getMinWidth(mContext);
        int maxWidth = Preference.getMaxWidth(mContext);
        TimingLogger logger = new TimingLogger("timermethod","hough line");
        Hough hough = new Hough(photo,0,0,height,width,height,maxWidth,minWidth,true);
        points = hough.getFeaturePoint();
        getWeight();
        logger.addSplit("Feature point");
        vote();
        logger.addSplit("vote");
        setMax();
        logger.addSplit("max");
        //setOneMax();
        Collections.sort(listPeak, (c1, c2) -> -Integer.compare(accum.weight[c1.r + rRange/2][c1.f],accum.weight[c2.r + rRange/2][c2.f]));
        verification();
        logger.addSplit("verification");
        logger.dumpToLog();
        return photo;
    }

    private void verification() {
        int i = 0;
        for (Peak p : listPeak) {
            androidx.core.util.Pair<Point,Point> points = getBound(p.r, p.f);
            verification(points.first, points.second,p,mColors[i]);
            i = (i + 1) % 7;
        }
    }

    private void verification(Point start, Point end, Peak peak, int color) {
        int gap_count = 0, start_pos = 1;
        int lenMin = Preference.getMinLenght(mContext);
        int widMin = Preference.getMinWidth(mContext);
        double maxY = Preference.getYValue(mContext);
        double maxX = Preference.getXValue(mContext);
        int xScale = Preference.getTime(mContext);
        if (start.x >= width || end.x >= width ) {
            throw new RuntimeException("Upper bound x");
        }
        if (start.y >= height || end.y >= height) {
            throw new RuntimeException("Upper bound y");
        }
        if (start.x < 0 || end.x < 0|| start.y < 0 || end.y < 0) {
            throw new RuntimeException("Down bound");
        }
        BrezenhemOld brezenhem = new BrezenhemOld(start,end);
        int i = 1;
        Pair<Double,Double> pair = getPair(peak.r,peak.f);
        Pair<Double,Double> realPair = getRealPair(peak.r,peak.f);
        Point startPoint = null;
        Point endPoint = null;
        while (brezenhem.hasNext()) {
            Point point = brezenhem.next();
            if (startPoint == null) {
                startPoint = point;
            }
            //photo.setPixel(point.x,height - 1 -point.y,color);
            if (photo.getPixel(point.x, height - 1 - point.y) == Color.WHITE) {
                gap_count = 0;
                if (start_pos == 0) {
                    start_pos = i;
                    startPoint = point;
                }
                if (!brezenhem.hasNext()) {
                    if (i - start_pos >= lenMin  && isSegment(startPoint,point,pair)) {
                        store.add(new LineSegment(realPair.first,realPair.second,
                                brezenhem.getRealPoint(startPoint,maxX,maxY,width,height,xScale),
                                brezenhem.getRealPoint(point,maxX,maxY,width,height,xScale)));
                        /*mSegments.add(new LineSegment(realPair.first,realPair.second,brezenhem.getPoint(start_pos),
                                brezenhem.getPoint(i)));*/
                        eraseSegment(startPoint,point,pair,color);
                    }
                }
            } else {
                gap_count += 1;
                endPoint = point;
                if (gap_count == 50 || !brezenhem.hasNext()) {
                    if (i - gap_count - start_pos >= lenMin  && isSegment(startPoint,endPoint,pair)) {
                        store.add(new LineSegment(realPair.first,realPair.second,brezenhem.getRealPoint(startPoint,maxX,maxY,width,height,xScale),
                                brezenhem.getRealPoint(endPoint,maxX,maxY,width,height,xScale)));
                        /*mSegments.add(new LineSegment(realPair.first,realPair.second,brezenhem.getPoint(start_pos),
                                brezenhem.getPoint(i - gap_count)));*/
                        eraseSegment(startPoint,endPoint,pair,color);
                    }
                    start_pos = 0;
                    startPoint = brezenhem.next();
                }
            }
            i++;
        }
    }

    private boolean isSegment(Point first, Point second, androidx.core.util.Pair<Double,Double> pair) {
        BrezenhemOld line = new BrezenhemOld(first,second);
        int i = 1;
        int line_count = 0;
        int lenMin = Preference.getMinLenght(mContext);
        int widMin = Preference.getMinWidth(mContext);
        int widMax = Preference.getMaxWidth(mContext);
        int kmin, kmax;
        while (line.hasNext()) {
            Point pi = line.next();
            if (photo.getPixel(getX(pi.x),getY(height - 1 - pi.y)) == Color.WHITE ) {
                line_count = 0;
            } else {
                line_count++;
            }
            if (line_count >= 50) {
                //throw new RuntimeException("line erase is segment > 5 minwidth");
                return false;
            }
            kmin = 0;
            kmax = 0;
            BrezenhemOld widthLine = new BrezenhemOld(pi,pair,true,width,height);
            int count = 0;
            widthLine.next();
            while(widthLine.hasNext()) {
                Point pw = widthLine.next();
                try {
                    if (photo.getPixel(getX(pw.x),getY(height - 1 - pw.y)) == Color.WHITE ) {
                        count = 0;
                        kmin++;
                    } else {
                        count++;
                    }
                    if (count == widMin) {
                        break;
                    }
                } catch (Exception e) {
                    throw  e;
                }
            }
            widthLine = new BrezenhemOld(pi,pair,false,width,height);
            widthLine.next();
            count = 0;
            while(widthLine.hasNext()) {
                Point pw = widthLine.next();
                try {
                    if (photo.getPixel(getX(pw.x),getY(height - 1 - pw.y)) == Color.WHITE ) {
                        count = 0;
                        kmax++;
                    } else {
                        count++;
                    }
                    if (count == widMin) {
                        break;
                    }
                } catch (Exception e) {
                    throw  e;
                }
            }
            kmax = kmin + kmax + 1;
            if (kmax < widMin) {
                return false;
            }
            i++;
        }
        return true;
    }

    private Point getLineEndPoint(Peak peak, Point start, int offset){
        if (peak.f > 45 && peak.f < 135) {
            return getPointByX(start.x + offset, getPair(peak.r,peak.f));
        } else {
            if (peak.f == 0 && peak.f == 180 ) {
                return new Point(start.x, start.y + offset);
            }
            return getPointByY(start.y + offset,getPair(peak.r,peak.f));
        }
    }

    private void eraseSegment(Point first, Point second, androidx.core.util.Pair<Double,Double> pair, int color) {
        int minW = Preference.getMinWidth(mContext);
        BrezenhemOld line = new BrezenhemOld(first,second);
        int i = 1;
        int gap_count = 0, start_pos = 1;
        int lenMin = Preference.getMinLenght(mContext);
        int widMin = Preference.getMinWidth(mContext);
        int widMax = Preference.getMaxWidth(mContext) / 2;
        while (line.hasNext()) {
            Point pi = line.next();
            if (photo.getPixel(getX(pi.x),getY(height - 1 - pi.y)) == Color.WHITE ) {
                photo.setPixel(getX(pi.x),getY(height - 1 - pi.y), color);
            }
            int k = 0;
            BrezenhemOld widthLine = new BrezenhemOld(pi,pair,true,width,height);
            int count = 0;
            while(widthLine.hasNext()) {
                Point pw = widthLine.next();
                //photo.setPixel(getX(pw.x),getY(height - 1 - pw.y), color);
                try {
                    if (photo.getPixel(getX(pw.x),getY(height - 1 - pw.y)) == Color.WHITE ) {
                        photo.setPixel(getX(pw.x),getY(height - 1 - pw.y), color);
                        k++;
                        count = 0;
                    } else {
                        count++;
                    }
                    if (k > widMax) {
                        break;
                    }
                    if (count == minW) {
                        break;
                    }
                } catch (Exception e) {
                    throw  e;
                }
            }
            k = 0;
            widthLine = new BrezenhemOld(pi,pair,false,width,height);
            count = 0;
            while(widthLine.hasNext()) {
                Point pw = widthLine.next();
                //photo.setPixel(getX(pw.x),getY(height - 1 - pw.y), color);
                try {
                    if (photo.getPixel(getX(pw.x),getY(height - 1 - pw.y)) == Color.WHITE ) {
                        photo.setPixel(getX(pw.x),getY(height - 1 - pw.y),color);
                        k++;
                        count = 0;
                    } else {
                        count++;
                    }
                    if (count == minW) {
                        break;
                    }
                    if (k > widMax) {
                        break;
                    }
                } catch (Exception e) {
                    throw  e;
                }
            }
            i++;
        }
    }

    private int getX (int x) {
        if (x < 0) {
            x = 0;
        } else if (x > width - 1) {
            x = width - 1;
        }
        return x;
    }

    private int getY (int y) {
        if (y < 0) {
            y = 0;
        } else if (y > height - 1) {
            y = height - 1;
        }
        return y;
    }

    private Point getPointByX(int x, androidx.core.util.Pair<Double,Double> pair) {
        return new Point(x,(int) (pair.first * x + pair.second));
    }

    private Point getPointByY(int y, androidx.core.util.Pair<Double,Double> pair) {
        return new Point((int) ((y - pair.second)/pair.first),y);
    }


    private void setMax() {
        int minLenght = Preference.getMinLenght(mContext);
        int minWidth = Preference.getMinWidth(mContext);
        int peak = minLenght*minWidth/60;
        int dw = 5;
        int df = 1;
        for (int i = 0; i < rRange; i++) {
            outer:
            for (int j = 0; j < 180; j++) {
                int lowBound = accum.lowBounder[i][j];
                int upBound = accum.upperBounder[i][j];
                boolean flag = inXDiapasone(j);
                if (accum.weight[i][j] > max) {
                    max = accum.weight[i][j];
                }
                if (accum.weight[i][j] > peak) {
                    int il = (i - dw) > 0 ? i - dw : 0 ;
                    int jl = (j - df) > 0 ? j - df : 0;
                    int iu = (i + dw) < rRange ? i + dw : rRange - 1 ;
                    int ju = (j + df) < 180 ? j + df : 180 - 1;
                    int j0 = jl;
                    int i0 = il;
                    while (i0 <= iu && j0 <= ju) {
                        if (i0 == i && j0 == j) {
                            j0++;
                            if (j0 > ju) {
                                j0 = jl;
                                i0++;
                            }
                            continue;
                        }
                        if (inXDiapasone(j0) == flag && accum.upperBounder[i0][j0] > upBound) {
                            upBound = accum.upperBounder[i0][j0];
                        }
                        if (inXDiapasone(j0) == flag && accum.lowBounder[i0][j0] < lowBound) {
                            lowBound = accum.lowBounder[i0][j0];
                        }
                        if (accum.weight[i0][j0] > accum.weight[i][j]) {
                            continue outer;
                        }
                        if (accum.weight[i0][j0] == accum.weight[i][j]) {
                            accum.weight[i0][j0] = 0;
                        }
                        j0++;
                        if (j0 > ju) {
                            j0 = jl;
                            i0++;
                        }
                    }
                    accum.lowBounder[i][j] = lowBound;
                    accum.upperBounder[i][j] = upBound;
                    listPeak.add(new Peak(i - rRange/2, j));
                }
            }
        }
    }

    private boolean inXDiapasone(int f) {
        return f > 45 && f < 135;
    }

    private void setOneMax() {
        int minLenght = Preference.getMinLenght(mContext);
        int minWidth = Preference.getMinWidth(mContext);
        int peak = 0;
        int max = -1;
        int i0 = -1,j0 = -1;
        for (int i = 0; i < rRange; i++) {
            for (int j = 0; j < 180; j++) {
                if (accum.weight[i][j] > peak && accum.weight[i][j] > max) {
                    max = accum.weight[i][j];
                    i0 = i;
                    j0 = j;
                }
            }
        }
        if (max != -1) {
            listPeak.add(new Peak(i0 - rRange/2 , j0));
        }
    }

    private void vote() {
        int r1;
        for (FeaturePoint point : points) {
            if (point.type == 1) {
                for (int f = 60; f <= 120; f++) {
                    r1 = (int) (point.x * Math.cos(Math.toRadians(f)) + (point.y) * Math.sin(Math.toRadians(f)));
                    accum.weight[r1 + rRange / 2][f] += point.weight;
                    accum.upperBounder[r1 + rRange / 2][f] = Math.max(accum.upperBounder[r1 + rRange / 2][f], point.x);
                    accum.lowBounder[r1 + rRange / 2][f] = Math.min(accum.lowBounder[r1 + rRange / 2][f], point.x);
                }
            } else if (point.type == 2) {
                for (int f = 0; f <= 30; f++) {
                    r1 = (int) (point.x * Math.cos(Math.toRadians(f)) + (point.y) * Math.sin(Math.toRadians(f)));
                    accum.weight[r1 + rRange / 2][f] += point.weight;
                    accum.upperBounder[r1 + rRange / 2][f] = Math.max(accum.upperBounder[r1 + rRange / 2][f], point.y);
                    accum.lowBounder[r1 + rRange / 2][f] = Math.min(accum.lowBounder[r1 + rRange / 2][f], point.y);
                }
                for (int f = 150; f < 180; f++) {
                    r1 = (int) (point.x * Math.cos(Math.toRadians(f)) + (point.y) * Math.sin(Math.toRadians(f)));
                    accum.weight[r1 + rRange / 2][f] += point.weight;
                    accum.upperBounder[r1 + rRange / 2][f] = Math.max(accum.upperBounder[r1 + rRange / 2][f], point.y);
                    accum.lowBounder[r1 + rRange / 2][f] = Math.min(accum.lowBounder[r1 + rRange / 2][f], point.y);
                }
            } else if (point.type == 4) {
                for (int f = 15; f <= 75; f++) {
                    r1 = (int) (point.x * Math.cos(Math.toRadians(f)) + (point.y) * Math.sin(Math.toRadians(f)));
                    accum.weight[r1 + rRange / 2][f] += point.weight;
                    if (f > 45) {
                        accum.upperBounder[r1 + rRange / 2][f] = Math.max(accum.upperBounder[r1 + rRange / 2][f], point.x);
                        accum.lowBounder[r1 + rRange / 2][f] = Math.min(accum.lowBounder[r1 + rRange / 2][f], point.x);
                    } else {
                        accum.upperBounder[r1 + rRange / 2][f] = Math.max(accum.upperBounder[r1 + rRange / 2][f], point.y);
                        accum.lowBounder[r1 + rRange / 2][f] = Math.min(accum.lowBounder[r1 + rRange / 2][f], point.y);
                    }
                }
            } else {
                for (int f = 105; f <= 165; f++) {
                    r1 = (int) (point.x * Math.cos(Math.toRadians(f)) + (point.y) * Math.sin(Math.toRadians(f)));
                    accum.weight[r1 + rRange / 2][f] += point.weight;
                    if (f < 135) {
                        accum.upperBounder[r1 + rRange / 2][f] = Math.max(accum.upperBounder[r1 + rRange / 2][f], point.x);
                        accum.lowBounder[r1 + rRange / 2][f] = Math.min(accum.lowBounder[r1 + rRange / 2][f], point.x);
                    } else {
                        accum.upperBounder[r1 + rRange / 2][f] = Math.max(accum.upperBounder[r1 + rRange / 2][f], point.y);
                        accum.lowBounder[r1 + rRange / 2][f] = Math.min(accum.lowBounder[r1 + rRange / 2][f], point.y);
                    }
                }
            }
        }
    }

    private void initAcc() {
        rRange = 2 * (int) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
        accum = new Accumulator(180,rRange);
        int maxBound = Math.max(height, width);
        for (int f = 0; f < 180; f++) {
            for (int r = 0; r < rRange; r++) {
                accum.lowBounder[r][f] = maxBound;
            }
        }
    }

    private void getFeaturePointsLine(int line, int hmin, int hmax) {

        int startX = 0;
        boolean whiteSeries = false;
        int countBlack = 0;
        for (int i = 0; i < width; i++) {
            if (isWhite(photo.getPixel(i, height - 1 -line))) {
                if (!whiteSeries) {
                    startX = i;
                    whiteSeries = true;
                    countBlack = 0;
                }
            } else {
                if (whiteSeries) {
                    countBlack ++;
                    if (countBlack == 3) {
                        whiteSeries = false;
                        int length = i - startX;
                        if (length >= hmin && length <= hmax) {
                            FeaturePoint point = new FeaturePoint(startX + length / 2, line, 1, length);
                            points.add(point);
                        }
                    }
                }
            }
        }
    }

    private void getFeaturePointsColumn(int column, int hmin, int hmax) {
        int startY = 0;
        boolean whiteSeria = false;
        int countBlack = 0;
        for (int i = 0; i < height; i++) {
            if (isWhite(photo.getPixel(column, height - 1 -i))) {
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
                        if (length >= hmin && length <= hmax) {
                            int horLenght = getHorizontalLenghtPoint(column, i);
                            if (horLenght > hmax || horLenght < hmin) {
                                FeaturePoint point = new FeaturePoint(column, startY + length / 2, 2, length);
                                points.add(point);
                            }
                        }
                    }
                }
            }
        }
    }

    private int getHorizontalLenghtPoint(int x0, int y0) {
        int rightLen = 0;
        int leftLen = 0;
        for (int x = x0; x < width; x++) {
            if (!isWhite(photo.getPixel(x, height - 1 - y0))) {
                rightLen = x - x0;
                break;
            }
        }
        for (int x = x0; x >= 0; x--) {
            if (!isWhite(photo.getPixel(x, height - 1 - y0))) {
                leftLen = x0 - x;
                break;
            }
        }
        return leftLen + rightLen;
    }

    private int getVerticalLenghtPoint(int x0, int y0) {
        int upLen = 0;
        int downLen = 0;
        for (int y = y0; y < height; y++) {
            if (!isWhite(photo.getPixel(x0, height - 1 - y))) {
                upLen = y - y0;
                break;
            }
        }
        for (int y = y0; y >= 0; y--) {
            if (!isWhite(photo.getPixel(x0, height - 1 - y))) {
                downLen = y0 - y;
                break;
            }
        }
        return upLen + downLen;
    }

    private int getRightDiagonalLenghtPoint(int x0, int y0) {
        int upRightDiag = 0;
        int downRightDiag = 0;
        for (int y = y0, x = x0; y < height && x < width; y++, x++) {
            if (!isWhite(photo.getPixel(x, height - 1 - y))) {
                upRightDiag = (int) Math.sqrt(Math.pow((y - y0), 2) + Math.pow((x - x0), 2));
                break;
            }
        }
        for (int y = y0, x = x0; y > -1 && x >= 0; y--, x--) {
            if (!isWhite(photo.getPixel(x, height - 1 - y))) {
                downRightDiag = (int) Math.sqrt(Math.pow((y - y0), 2) + Math.pow((x - x0), 2));
                break;
            }
        }
        return downRightDiag + upRightDiag;
    }

    private int getLeftDiagonalLenghtPoint(int x0, int y0) {
        int upLeftDiag = 0;
        int downLeftDiag = 0;
        for (int y = y0, x = x0; y < height && x >= 0; y++, x--) {
            if (!isWhite(photo.getPixel(x, height - 1 - y))) {
                upLeftDiag = (int) Math.sqrt(Math.pow((y - y0), 2) + Math.pow((x - x0), 2));
                break;
            }
        }
        for (int y = y0, x = x0; y > -1 && x < width; y--, x++) {
            if (!isWhite(photo.getPixel(x, height - 1 - y))) {
                downLeftDiag = (int) Math.sqrt(Math.pow((y - y0), 2) + Math.pow((x - x0), 2));
                break;
            }
        }
        return downLeftDiag + upLeftDiag;
    }

    private void getWeight() {
        for (FeaturePoint point : points) {
            int h_len = 0, v_len = 0, rd_len = 0, ld_len = 0;
            if (point.type == 1) {
                h_len = point.weight;
                v_len = getVerticalLenghtPoint(point.x, point.y);
            } else {
                h_len = getHorizontalLenghtPoint(point.x, point.y);
                v_len = point.weight;
            }
            rd_len = getRightDiagonalLenghtPoint(point.x, point.y);
            ld_len = getLeftDiagonalLenghtPoint(point.x, point.y);
            switch (max(h_len,v_len,rd_len,ld_len)) {
                case  (0):
                    point.type = 1;
                    point.weight = v_len;
                    break;
                case (1):
                    point.type = 2;
                    point.weight = h_len;
                    break;
                case (2):
                    point.type = 3;
                    point.weight = ld_len;
                    break;
                case (3):
                    point.type = 4;
                    point.weight = rd_len;
                    break;
                default:
                    throw new RuntimeException("incorect max func");

            }
        }
    }

    private int max(int ... val ) {
        int index = 0;
        int max = val[0];
        for (int i = 1;i < val.length;i++) {
            if (val[i] > max) {
                index = i;
                max = val[i];
            }
        }
        return index;
    }

    private int min(int ... val ) {
        int index = 0;
        int min = val[0];
        for (int i = 1;i < val.length;i++) {
            if (val[i] < min) {
                index = i;
                min = val[i];
            }
        }
        return index;
    }

    private void colorBitmap(ArrayList<FeaturePoint> points) {
        for (FeaturePoint point : points) {
            photo.setPixel(point.x, height - 1 - point.y, Color.RED);
            photo.setPixel(point.x-1, height - 1 - point.y, Color.RED);
            photo.setPixel(point.x+1, height - 1 - point.y, Color.RED);
        }
    }

    private boolean isWhite(int pixel) {
        return pixel == Color.WHITE;
    }


    private int getScale(int y, int height) {
        return (50 * y) / height;
    }


    private androidx.core.util.Pair<Double,Double> getPair(int r, int f) {
        double k = - Math.cos(Math.toRadians(f)) / Math.sin(Math.toRadians(f));
        double b = r / Math.sin(Math.toRadians(f));
        return new androidx.core.util.Pair<>(k, b);
    }

    private void createAccumFile () {
        /*Bitmap bitmap = Bitmap.createBitmap(1800,rRange, Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888,true);
        for (int f = 0; f < 180; f++) {
            for (int r = 0; r < rRange; r++) {
                if (accum[r][f].weight >   max/2) {
                    for (int i = 0;i < 10;i++) {
                        bitmap.setPixel(f*10 + i,r,Color.WHITE);
                    }
                } else if (accum[r][f].weight > max/5) {
                    for (int i = 0;i < 10;i++) {
                        bitmap.setPixel(f*10 + i,r,Color.argb(255,242,242,242));
                    }
                } else if (accum[r][f].weight > max/30) {
                    for (int i = 0;i < 10;i++) {
                        bitmap.setPixel(f*10 + i,r,Color.argb(255,200,200,200));
                    }
                } else if (accum[r][f].weight > max/100) {
                    for (int i = 0;i < 10;i++) {
                        bitmap.setPixel(f*10 + i,r,Color.argb(255,170,170,170));
                    }
                } else if (accum[r][f].weight >  max/300) {
                    for (int i = 0;i < 10;i++) {
                        bitmap.setPixel(f*10 + i,r,Color.argb(255,140,140,140));
                    }
                } else if (accum[r][f].weight > max/1000) {
                    for (int i = 0;i < 10;i++) {
                        bitmap.setPixel(f*10 + i,r,Color.argb(255,100,100,100));
                    }
                }else {
                    for (int i = 0;i < 10;i++) {
                        bitmap.setPixel(f*10 + i,r,Color.BLACK);
                    }
                }
            }
        }
        File filePath = mContext.getFilesDir();
        File file = new File(filePath,"test2.png");
        try(FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private androidx.core.util.Pair<Double,Double> getRealPair(int r, int f) {
        float y = Preference.getYValue(mContext);
        float x = Preference.getXValue(mContext);
        int xScale = Preference.getTime(mContext);
        x *= xScale;
        double k = - Math.cos(Math.toRadians(f)) / Math.sin(Math.toRadians(f));
        double b = r / Math.sin(Math.toRadians(f));
        b = b  * y / (height-1);
        k = k * y * width / (x * (height -1));
        return new androidx.core.util.Pair<>(k, b);
    }

    private androidx.core.util.Pair<Point,Point> getBound(int r, int f) {
        int y1,x1,y2,x2;
        if (45 < f && f < 135) {
            x1 = accum.lowBounder[r + rRange/2][f ];
            x2 = accum.upperBounder[r + rRange/2 ][f ];
            y1 = (int) (r / Math.sin(Math.toRadians(f)) - x1 *
                    Math.cos(Math.toRadians(f)) / Math.sin(Math.toRadians(f)));
            y2 = (int) (r / Math.sin(Math.toRadians(f)) - x2 *
                    Math.cos(Math.toRadians(f)) / Math.sin(Math.toRadians(f)));
        } else {
            y1 = accum.lowBounder[r + rRange/2 ][f ];
            y2 = accum.upperBounder[r + rRange/2][f ];
            x1 = (int) (r / Math.cos(Math.toRadians(f)) - y1 *
                    Math.sin(Math.toRadians(f)) / Math.cos(Math.toRadians(f)));
            x2 = (int) (r / Math.cos(Math.toRadians(f)) - y2 *
                    Math.sin(Math.toRadians(f)) / Math.cos(Math.toRadians(f)));
        }
        if (x1 >= width) {
            x1 = width-1;
        }
        if (x2 >= width) {
            x2 = width-1;
        }
        if (y1 >= height) {
            y1 = height-1;
        }
        if (y2 >= height) {
            y2 = height-1;
        }
        if (x1 < 0) {
            x1 = 0;
        }
        if (x2 < 0) {
            x2 = 0;
        }
        if (y1 < 0) {
            y1 = 0;
        }
        if (y2 < 0) {
            y2 = 0;
        }
        return new Pair<>(new Point(x1,y1),new Point(x2,y2));
    }

    class Accumulator {
        private int [][] weight;
        private int [][] upperBounder;
        private int [][] lowBounder;

        public Accumulator(int width, int height) {
            weight = new int[height][width];
            upperBounder = new int[height][width];
            lowBounder = new int[height][width];
        }
    }

    class Peak {
        private int r;
        private int f;

        public Peak(int r, int f) {
            this.r = r;
            this.f = f;
        }
    }
}
