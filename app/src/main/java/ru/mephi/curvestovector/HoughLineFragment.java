package ru.mephi.curvestovector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

public class HoughLineFragment extends Fragment implements LoaderManager.LoaderCallbacks<Bitmap> {

    private ImageView mPhotoView;
    private FloatingActionButton mButton;
    private ProgressBar mProgressBar;
    private TextView mText;
    private PlotPhoto mPhoto;
    private File mPhotoFile;
    //private AsyncTask<Bitmap, Integer, Bitmap> mHandler;
    private Bitmap binarBitmap;
    private static final int LOADER_ID = 1;

    public HoughLineFragment() {
        // Required empty public constructor
    }

    public static HoughLineFragment newInstance() {
        HoughLineFragment fragment = new HoughLineFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mPhoto = PlotPhoto.getPlotPhoto(getActivity());
        String pathBinarPhoto = mPhoto.makePhotoBinarFile().getAbsolutePath();
        binarBitmap = BitmapFactory.decodeFile(pathBinarPhoto);
        Bundle bndl = new Bundle();
        bndl.putString(HoughLineLoader.filePath, pathBinarPhoto);
        getLoaderManager().initLoader(LOADER_ID,bndl,this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hough_line,container,false);
        mPhotoView = v.findViewById(R.id.photo);
        mPhotoView.setImageBitmap(binarBitmap);
        mProgressBar = v.findViewById(R.id.progressbar);
        mText = v.findViewById(R.id.text);
        mButton = v.findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("pair1",binarBitmap.getHeight());
                bundle.putSerializable("pair2",binarBitmap.getWidth());
                Navigation.findNavController(getActivity(),R.id.fragment).navigate(R.id.toFinalFromLine,bundle);
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        /*if (mHandler != null ) {
            mHandler.cancel(true);
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("Lines","line");
        /*mHandler = new PhotoHoughLineHandler();
        ((PhotoHoughLineHandler) mHandler).link(HoughLineFragment.this);
        mHandler.execute(binarBitmap.copy(binarBitmap.getConfig(),true));*/
        //mProgressBar.setVisibility(View.VISIBLE);
        getLoaderManager().getLoader(LOADER_ID).forceLoad();
    }

    @NonNull
    @Override
    public Loader<Bitmap> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Bitmap> loader = null;
        if (id == LOADER_ID) {
            loader = new HoughLineLoader(getActivity(), args);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Bitmap> loader, Bitmap data) {
        mPhotoView.setImageBitmap(data);
        mButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Bitmap> loader) {

    }
}