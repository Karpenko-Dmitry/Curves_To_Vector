package ru.mephi.curvestovector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class PhotoBinarHandler extends AsyncTask<Bitmap,Integer,Bitmap> {

    private boolean isRunning = false;
    private BinarizationFragment link;
    private int usefulPixel;
    private int uselessPixel;

    public void link(BinarizationFragment link){
        this.link = (BinarizationFragment) link;
    }

    public void unlink(){
        link = null;
    }

    public boolean isRunning() {
        return isRunning;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        isRunning = true;
        if (Preference.getColorPixel(link.getActivity()) == Color.WHITE) {
            usefulPixel = Color.WHITE;
            uselessPixel = Color.BLACK;
        } else {
            usefulPixel = Color.BLACK;
            uselessPixel = Color.WHITE;
        }
    }

    @Override
    protected Bitmap doInBackground(Bitmap... bitmaps) {
        Log.d("binar","start background");
        Bitmap photo = bitmaps[0];
        int width = photo.getWidth();
        int height = photo.getHeight();
        for (int j = 0; j < height;j++) {
            for (int i = 0; i < width;i++) {
                if (isCancelled()) {
                    return null;
                }
                int newColor = binarization(photo.getPixel(i,j));
                photo.setPixel(i,j,newColor);
            }
            if (j % 10 == 0) {
                publishProgress(getScale(j, height));
            }
        }
        return photo;
    }

    private int getScale(int y, int height) {
        return (100 * y) / height;
    }

    private int binarization(int pixel) {
        float luminance = getLuminance(pixel);
        //pixel = luminance > link.getThreshold() ? usefulPixel : uselessPixel;
        return pixel;
    }

    private float getLuminance(int pixel) {
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);
        return (0.29f * r + 0.58f * g  + 0.11f * b);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        link.getProgressBar().setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        /*link.getPhotoView().setImageBitmap(bitmap);
        link.setBinPhoto(bitmap);
        isRunning = false;
        link.getProgressBar().setVisibility(View.INVISIBLE);
        link.getButton().setVisibility(View.VISIBLE);*/
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}