package ru.mephi.curvestovector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

public class DrawImageView extends View {

    private Paint mFingerTouch;
    private Bitmap bitmap;
    private Canvas mCanvas;
    private int w;
    private int h;
    private boolean drawing;
    private Square mSquare;
    private boolean active;

    private HoughParabolaFragment link;

    public DrawImageView(Context context) {
        this(context, null);
    }

    public DrawImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        active = true;
        mFingerTouch = new Paint();
        mFingerTouch.setColor(Color.BLUE);
        mFingerTouch.setStyle(Paint.Style.STROKE);
        mFingerTouch.setStrokeWidth(5);
    }

    public void link(HoughParabolaFragment link) {
        this.link = link;
    }

    public void unlink() {
        this.link = null;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setBitmap(Bitmap b) {
        bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas = new Canvas(bitmap);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF p = new PointF(event.getX(), event.getY());
        Log.i("Touch",p.toString());
        if (active) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //TODO link.mButton.setVisibility(View.INVISIBLE);
                    mSquare = new Square(p);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mSquare != null) {
                        mSquare.setEnd(p);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //TODO link.mButton.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    //mSquare = null;
                    break;
            }
            invalidate();
            return true;
        }
        return false;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Pair<Point, Point> getSquarePoint() {
        return mSquare.getPairPoint();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.h = h;
        this.w = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null) {
            canvas.drawBitmap(Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888), 0, 0, new Paint());
        } else {
            canvas.drawBitmap(bitmap, 0, 0, null);
            if (active && mSquare != null) {
                float left = Math.min(mSquare.getStart().x, mSquare.mEnd.x);
                float right = Math.max(mSquare.getStart().x, mSquare.mEnd.x);
                float top = Math.min(mSquare.getStart().y, mSquare.mEnd.y);
                float bottom = Math.max(mSquare.getStart().y, mSquare.mEnd.y);
                canvas.drawRect(left, top, right, bottom, mFingerTouch);
            }
        }
    }


    private class Square {
        private PointF mStart;
        private PointF mEnd;

        public Square(PointF start) {
            mStart = start;
            mEnd = start;
        }

        public PointF getEnd() {
            return mEnd;
        }

        public void setEnd(PointF end) {
            mEnd = end;
        }

        public PointF getStart() {
            return mStart;
        }

        public Pair<Point, Point> getPairPoint() {
            float minX = mStart.x;
            float minY = mStart.y;
            float maxX = mStart.x;
            float maxY = mStart.y;
            if (mEnd.x > maxX) {
                maxX = mEnd.x;
            }
            if (mEnd.x < minX) {
                minX = mEnd.x;
            }
            if (mEnd.y > maxY) {
                maxY = mEnd.y;
            }
            if (mEnd.y < minY) {
                minY = mEnd.y;
            }
            return new Pair<>(new Point((int) minX, (int) minY), new Point((int) maxX, (int) maxY));
        }
    }
}
