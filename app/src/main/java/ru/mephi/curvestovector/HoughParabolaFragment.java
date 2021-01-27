package ru.mephi.curvestovector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HoughParabolaFragment extends Fragment {

    private AppCompatSeekBar mSeekBar;
    private TextView mTextView;
    public FloatingActionButton mButton;
    public DrawImageView mDrawImageView;
    private PhotoHoughParabolaHandler mHandler;
    private int num;
    private int current = 1;
    private boolean hasNum;
    private static final String DIALOG_NUM = "DialogNum";
    private static final int REQUEST_NUM = 0;

    public HoughParabolaFragment() {
        // Required empty public constructor
    }

    public static HoughParabolaFragment newInstance(String param1, String param2) {
        HoughParabolaFragment fragment = new HoughParabolaFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hough_parabola, container, false);
        mTextView = v.findViewById(R.id.text);
        mTextView.setText(getString(R.string.parabola_draw, 1));
        mButton = v.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                mHandler = new PhotoHoughParabolaHandler();
                mHandler.link(HoughParabolaFragment.this);
                mDrawImageView.setActive(false);
                mDrawImageView.getSquarePoint();
                Bitmap b = mDrawImageView.getBitmap();
                mHandler.execute(b.copy(b.getConfig(), true));
                mButton.setVisibility(View.INVISIBLE);
            }
        });
        mSeekBar = v.findViewById(R.id.seekbar);
        /*mSeekBar.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDrawImageView.setBrushSize(seekBar.getProgress());
            }
        });*/
        mDrawImageView = v.findViewById(R.id.image);
        mDrawImageView.link(this);
        PlotPhoto mPhoto = PlotPhoto.getPlotPhoto(getActivity());
        //bitmap = BitmapFactory.decodeFile(mPhoto.makePhotoBinarFile().getAbsolutePath());
        final ViewTreeObserver viewTreeObserver = mDrawImageView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mDrawImageView.setBitmap(PlotPhoto.getScaledBitmapFullSize(mPhoto.makePhotoBinarFile().getAbsolutePath(),
                            mDrawImageView.getWidth(), mDrawImageView.getHeight()));
                }
            });
        }
        //Bitmap bitmap = PlotPhoto.getScaledBitmap(mPhoto.makePhotoBinarFile().getAbsolutePath(),
        //mDrawImageView.getWidth(),mDrawImageView.getHeight());
        //mDrawImageView.setBitmap(bitmap);
        return v;
    }

    public Pair<Point, Point> getPoints() {
        return mDrawImageView.getSquarePoint();
    }

    public void update() {
        if (current == num) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("pair1",mDrawImageView.getBitmap().getHeight());
            bundle.putSerializable("pair2",mDrawImageView.getBitmap().getWidth());
            Navigation.findNavController(getActivity(),R.id.fragment).navigate(R.id.toFinalFromParabola,bundle);
        } else {
            current++;
            //mDrawImageView.setBitmap(bitmap);
            mTextView.setText(getString(R.string.parabola_draw, current));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        hasNum = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasNum) {
            FragmentManager fragmentManager = getFragmentManager();
            DialogParabolaFragment dialog = DialogParabolaFragment.newInstance();
            dialog.setTargetFragment(HoughParabolaFragment.this, REQUEST_NUM);
            dialog.show(fragmentManager, DIALOG_NUM);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_NUM) {
            num = data.getIntExtra(DialogParabolaFragment.EXTRA_NUM, 0);
            hasNum = true;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDrawImageView.unlink();
    }
}