package ru.mephi.curvestovector;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FinalFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SegmentAdapter mAdapter;
    private SegmentStore mStore;
    private ImageButton mSend;
    private ImageButton mRotate;
    private int height;
    private int width;
    private double maxY;
    private double maxX;
    private double angle=0;
    public static final int REQUEST =0;

    public FinalFragment() {
        // Required empty public constructor
    }

    public static FinalFragment newInstance() {
        FinalFragment fragment = new FinalFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_final,container,false);
        mRecyclerView = v.findViewById(R.id.recycler_view);
        height = getArguments().getInt("pair1");
        width = getArguments().getInt("pair2");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mStore = SegmentStore.getSegmentStore(getActivity());
        mAdapter = new SegmentAdapter(mStore.getSegments());
        mRecyclerView.setAdapter(mAdapter);
        maxY = Preference.getYValue(getActivity());
        maxX = Preference.getXValue(getActivity());
        ArrayList<Segment> segments = mStore.getSegments();
        correctPoints(segments);
        mSend = v.findViewById(R.id.send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConvertVector vectorConvert = new ConvertVector(maxY,height,maxX,width);
                ArrayList<String> hex = vectorConvert.getVectors(segments);
                PlotPhoto.getPlotPhoto(getActivity()).writeCSVfile(hex);
                Uri path = FileProvider.getUriForFile(getContext().getApplicationContext(),
                        "ru.mephi.curvestovector.photo", PlotPhoto.getPlotPhoto(getActivity()).makeCSVfile());
                Intent i = new Intent(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, "Vectors");
                i.putExtra(Intent.EXTRA_STREAM, path);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.setType("plain/*");
                /*List<ResolveInfo> sendActivities = getActivity()
                        .getPackageManager().queryIntentActivities(i,
                                PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo activity : sendActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,path,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }*/
                startActivity(i);
            }
        });
        mRotate = v.findViewById(R.id.rotate);
        mRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivityForResult(RotateFixActivity.newIntent(getActivity(),angle),REQUEST);
            }
        });
        return v;
    }

    private  void correctPoints(ArrayList<Segment> segments) {
        Collections.sort(segments, (segment, t1) -> segment.start.x - t1.start.x);
        for (int i = 0; i < segments.size();i++) {
            Segment segment = segments.get(i);
            Point newPoint = null;
            if (i == 0) {
                int y = (int) Math.round(segment.getY(0));
                int x = (int) Math.round(segment.getX(0));
                if (checkY(y)) {
                    segment.setStart(new Point(0,y));
                    segment.correct();
                } else if (checkX(x)) {
                    segment.setStart(new Point(x,0));
                    segment.correct();
                }
            } else {
                segment.setStart(segments.get(i-1).end);
                segment.correct();
            }
        }
    }

    private boolean checkY(int y) {
        return y >= 0 && y <= maxY;
    }

    private boolean checkX(int x) {
        return x >= 0 && x <= maxX;
    }

    private class SegmentHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;
        private Segment mSegment;

        public SegmentHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_element, parent, false));
            mTextView = itemView.findViewById(R.id.segment_text);
        }

        public void bind(Segment segment) {
            mSegment = segment;
            if (Preference.isLine(getActivity())) {
                LineSegment line = (LineSegment) mSegment;
                String k = "" + line.getK();
                mTextView.setText("y = " + formatDouble(line.getK()) + " * x + " + formatDouble(line.getB()) +
                        " [ {" + line.getStart().x + ";" + line.getStart().y + "} {" +
                        line.getEnd().x + ";" + line.getEnd().y + "} ]");
            } else{
                ParabolaSegment parabola = (ParabolaSegment) mSegment;
                mTextView.setText("a = " + parabola.getA() + "; b = " + parabola.getB() +
                        "; c = " + parabola.getC() + "; d = " + parabola.getD());
            }
        }

        private String formatDouble(Double d) {
            return String.format("%.6f",d);
        }
    }

    private class SegmentAdapter extends RecyclerView.Adapter<SegmentHolder> {

        private List<Segment> mSegments;

        public SegmentAdapter(List<Segment> segments) {
            mSegments = segments;
        }

        @NonNull
        @Override
        public SegmentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new SegmentHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull SegmentHolder holder, int position) {
            holder.bind(mSegments.get(position));
        }

        @Override
        public int getItemCount() {
            return mSegments.size();
        }
    }

    public void setAngle(double angle) {
        this.angle = angle;
        corectSegments();
        mAdapter.notifyDataSetChanged();
    }

    private void corectSegments() {
        ArrayList<Segment> segments = mStore.getSegments();
        Point p;
        if (segments.isEmpty()) {
            return;
        }
        LineSegment ls = (LineSegment) segments.get(0);
        ls.start = new Point(getNewX(ls.start.x,ls.start.y),getNewY(ls.start.x,ls.start.y));
        p = new Point(getNewX(ls.end.x,ls.end.y),getNewY(ls.end.x,ls.end.y));
        ls.end = p;
        ls.correct();
        for(int i = 1;i < segments.size();i++) {
            LineSegment s = (LineSegment) segments.get(i);
            s.start = p;
            p = new Point(getNewX(s.end.x,s.end.y),getNewY(s.end.x,s.end.y));
            s.end = p;
            s.correct();
        }
    }

    private int getNewX(double x, double y) {
        return (int) (x * Math.cos(Math.toRadians(angle)) + y * Math.sin(Math.toRadians(angle)));
    }

    private int getNewY(double x, double y) {
        return (int) (-x * Math.sin(Math.toRadians(angle)) + y * Math.cos(Math.toRadians(angle)));
    }
}