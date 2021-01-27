package ru.mephi.curvestovector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TimingLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class BinarizationLoader extends AsyncTaskLoader<Bitmap> {

    public static final String filePath = "FILE_PATH";
    public static final String THRESHOLD = "THRESHOLD";
    private Context mContext;
    private Bitmap photo;
    private int usefulPixel;
    private int uselessPixel;
    private int threshold;
    private boolean result;


    public BinarizationLoader(@NonNull Context context, Bundle args) {
        super(context);
        mContext = context.getApplicationContext();
        photo = BitmapFactory.decodeFile(args.getString(filePath)).copy(Bitmap.Config.ARGB_8888,true);
        threshold = args.getInt(THRESHOLD);
        if (Preference.getColorPixel(mContext) == Color.WHITE) {
            usefulPixel = Color.WHITE;
            uselessPixel = Color.BLACK;
        } else {
            usefulPixel = Color.BLACK;
            uselessPixel = Color.WHITE;
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (isResult()) {
            deliverResult(getPhoto());
        } else {
            forceLoad();
        }
    }

    @Nullable
    @Override
    public Bitmap loadInBackground() {
        int width = photo.getWidth();
        int height = photo.getHeight();
        Log.i("size_bitmap","width:" + width + "; height:" + height);
        TimingLogger logger = new TimingLogger("timermethod","binarization");
        for (int j = 0; j < height;j++) {
            for (int i = 0; i < width;i++) {
                int newColor = binarization(photo.getPixel(i,j));
                photo.setPixel(i,j,newColor);
            }
        }
        logger.addSplit("binarization");
        logger.dumpToLog();
        result = true;
        return photo;
    }

    private int binarization(int pixel) {
        float luminance = getLuminance(pixel);
        pixel = luminance > threshold ? usefulPixel : uselessPixel;
        return pixel;
    }

    private float getLuminance(int pixel) {
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);
        return ( (  66 * r + 129 * g +  25 * b + 128) >> 8) +  16;
    }

    public boolean isResult() {
        return result;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    @Override
    public void deliverResult(@Nullable Bitmap data) {
        photo = data;
        super.deliverResult(data);
    }
}
