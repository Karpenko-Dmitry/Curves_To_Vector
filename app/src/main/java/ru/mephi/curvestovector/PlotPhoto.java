package ru.mephi.curvestovector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class PlotPhoto {

    private static PlotPhoto mPhoto;
    private Context mContext;
    private File mPhotoFile;

    public static PlotPhoto getPlotPhoto (Context context) {
        if (mPhoto == null) {
            mPhoto = new PlotPhoto(context);
        }
        return mPhoto;
    }

    private PlotPhoto(Context context) {
        mContext = context.getApplicationContext();
        mPhotoFile = makePhotoFile();
    }


    public static String getPhotoFilename() {
        Date date = new Date();
        String str = "IMG_" + date.getTime() + ".jpg";
        Log.d("Photo",str);
        return str;
    }

    public static String getTempPhotoFilename() {
        return "IMG_TEMP.jpg";
    }

    public static String getTempPhotoBinarFilename() {
        return  "IMG_BINAR.jpg";

    }

    public File makePhotoFile() {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, getTempPhotoFilename());
    }

    public File makePhotoBinarFile() {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, getTempPhotoBinarFilename());
    }

    public File makeCSVfile() {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, getTempCSVFilename());
    }

    private static String getTempCSVFilename() {
        return "coefficient.txt";
    }

    public File getPhotoFile() {
        return mPhotoFile;
    }

    public void setPhotoFile(File photoFile) {
        mPhotoFile = photoFile;
    }

    public void createPhotoBitmapFile(Bitmap bitmap) {
        File file = makePhotoBinarFile();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(bitmapdata);
            } finally {
                fos.flush();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void writeCSVfile(ArrayList<String> strings) {
        File file = makeCSVfile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < strings.size();i++) {
                writer.write(strings.get(i) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;
            inSampleSize = Math.round(heightScale > widthScale ? heightScale :
                    widthScale);
        }
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmapFullSize(String path, int destWidth, int destHeight) {
        Bitmap b = BitmapFactory.decodeFile(path);
        return Bitmap.createScaledBitmap(b,destWidth,destHeight,true);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay()
                .getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

}
