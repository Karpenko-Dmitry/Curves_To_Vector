package ru.mephi.curvestovector;

import android.content.Context;

import java.util.ArrayList;

public class SegmentStore {

    private static SegmentStore mStore;
    private Context mContext;
    private ArrayList<Segment> mSegments;


    public static SegmentStore getSegmentStore (Context context) {
        if (mStore == null) {
            mStore = new SegmentStore(context);
        }
        return mStore;
    }

    private SegmentStore(Context context) {
        mContext = context.getApplicationContext();
        //mSegments = new ArrayList<>();
    }

    public ArrayList<Segment> getSegments(){
        return mSegments;
    }

    public void erase(){
        mSegments = new ArrayList<>();
    }

    public int getSize(){
        return  mSegments.size();
    }

    public void add(Segment segment) {
       mSegments.add(segment);
    }

    public Segment get(int index){
        return mSegments.get(index);
    }
}
