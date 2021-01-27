package ru.mephi.curvestovector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class BinarizationFragment extends Fragment implements LoaderManager.LoaderCallbacks<Bitmap>{

    private ImageView mPhotoView;
    private FloatingActionButton mButton;
    private AppCompatSeekBar mSeekBar;
    private ContentLoadingProgressBar mProgressBar;
    private PlotPhoto mPhoto;
    private Bitmap binPhoto;
    private File mPhotoFile;
    private PhotoBinarHandler mHandler;
    private int heightImage;
    private int widthImage;
    private static final int LOADER_ID = 2;
    private String pathBinarPhoto;

    public BinarizationFragment() {
        // Required empty public constructor
    }

    public static BinarizationFragment newInstance() {
        BinarizationFragment fragment = new BinarizationFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPhoto = PlotPhoto.getPlotPhoto(getActivity());
        pathBinarPhoto = mPhoto.getPhotoFile().getAbsolutePath();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_binarization,container,false);
        mPhotoView = v.findViewById(R.id.photo);
        mProgressBar = v.findViewById(R.id.progressbar);
        mButton = v.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPhoto.createPhotoBitmapFile(binPhoto);
                NavController nav = Navigation.findNavController(getActivity(),R.id.fragment);
                if (Preference.isLine(getActivity())) {
                    nav.navigate(R.id.toLineFragment);
                } else {
                    nav.navigate(R.id.toParabolaFragment);
                }
            }
        });
        mSeekBar = v.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mButton.setVisibility(View.INVISIBLE);
                /*Loader<Bitmap> loader = getLoaderManager().getLoader(LOADER_ID);
                if (loader != null) {
                    loader.cancelLoad();
                }*/
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int threshold = seekBar.getProgress();
                mProgressBar.setVisibility(View.VISIBLE);
                Bundle bndl = new Bundle();
                bndl.putString(BinarizationLoader.filePath, pathBinarPhoto);
                bndl.putInt(BinarizationLoader.THRESHOLD, threshold);
                Loader<Bitmap> loader = getLoaderManager().getLoader(LOADER_ID);
                if (loader == null) {
                    getLoaderManager().initLoader(LOADER_ID,bndl,BinarizationFragment.this);
                } else {
                    getLoaderManager().restartLoader(LOADER_ID,bndl,BinarizationFragment.this);
                }
            }
        });
        setPhotoViewDimension();
        mPhotoFile = mPhoto.getPhotoFile();
        updatePhotoView();
        return v;
    }

    public ContentLoadingProgressBar getProgressBar() {
        return mProgressBar;
    }

    private void setPhotoViewDimension() {
        ViewTreeObserver viewTreeObserver = mPhotoView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    heightImage = mPhotoView.getHeight();
                    widthImage = mPhotoView.getWidth();
                }
            });
        }
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            //mPhotoView.setImageDrawable(null);
            return;
        } else {
            Bitmap bitmap = null;
            if (heightImage != 0 && widthImage != 0) {
                bitmap = PlotPhoto.getScaledBitmap(mPhotoFile.getPath(),
                        widthImage,heightImage);
            } else {
                bitmap = PlotPhoto.getScaledBitmap(
                        mPhotoFile.getPath(), getActivity());
            }

            bitmap = compressedBitmap(bitmap);
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setEnabled(false);
        }
    }

    private Bitmap compressedBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bos = null;
        byte[] bytes = null;
        try{
            bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,bos);
            bytes = bos.toByteArray();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }

    @NonNull
    @Override
    public Loader<Bitmap> onCreateLoader(int id, @Nullable Bundle args) {
        BinarizationLoader loader = null;
        if (id == LOADER_ID) {
            loader = new BinarizationLoader(getActivity(), args);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Bitmap> loader, Bitmap data) {
        mPhotoView.setImageBitmap(data);
        mButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        binPhoto = data;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Bitmap> loader) {

    }

    @Override
    public void onResume() {
        super.onResume();
        SegmentStore store = SegmentStore.getSegmentStore(getActivity());
        store.erase();
    }
}