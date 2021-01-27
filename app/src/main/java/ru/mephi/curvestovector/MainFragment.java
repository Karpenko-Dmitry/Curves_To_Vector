package ru.mephi.curvestovector;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class MainFragment extends Fragment {

    private ImageButton mPhotoButton;
    private Spinner mSpinner;
    private Spinner mMode;
    private Spinner mTime;
    private EditText mTextY;
    private EditText mTextX;
    private EditText mTextMinWidth;
    private EditText mTextMaxWidth;
    private EditText mTextMinLenght;
    private EditText mTextMaxLenght;
    private File mPhotoFile;
    private static final int REQUEST_PHOTO= 0;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlotPhoto photo = PlotPhoto.getPlotPhoto(getActivity());
        mPhotoFile = photo.getPhotoFile();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main,container,false);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getActivity().getPackageManager();
        mPhotoButton = v.findViewById(R.id.photo_button);
        mPhotoButton.setOnClickListener(view -> {
            try {
                saveParametrs();
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "ru.mephi.curvestovector.photo",
                        mPhotoFile);
                SegmentStore.getSegmentStore(getActivity()).erase();
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            } catch (NumberFormatException ex) {
                Toast.makeText(getActivity(),"Введены неверные данные",Toast.LENGTH_LONG).show();
            }

        });
        mTextX = v.findViewById(R.id.x_axes_text);
        mTextX.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mTextX.setText(Float.toString(Preference.getXValue(getActivity())));
        mTextY = v.findViewById(R.id.y_axes_text);
        mTextY.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mTextY.setText(Float.toString(Preference.getYValue(getActivity())/ 1000));
        mTextMinWidth = v.findViewById(R.id.min_width);
        mTextMinWidth.setText(Integer.toString(Preference.getMinWidth(getActivity())));
        mTextMaxWidth = v.findViewById(R.id.max_width);
        mTextMaxWidth.setText(Integer.toString(Preference.getMaxWidth(getActivity())));
        mTextMinLenght = v.findViewById(R.id.min_lenght);
        mTextMinLenght.setText(Integer.toString(Preference.getMinLenght(getActivity())));
        mTextMaxLenght = v.findViewById(R.id.max_lenght);
        mTextMaxLenght.setText(Integer.toString(Preference.getMaxLenght(getActivity())));
        mSpinner = v.findViewById(R.id.list);
        mMode = v.findViewById(R.id.mode);
        mTime = v.findViewById(R.id.time);
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "ru.mephi.curvestovector.photo",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // boolean canTakePhoto = mPhotoFile != null &&
            //captureImage.resolveActivity(packageManager) != null;
            //mButton.setEnabled(canTakePhoto);
            Navigation.findNavController(getActivity(),R.id.fragment).navigate(R.id.toBinar);
            //Intent photoIntent = new Intent(getActivity(), PhotoBinarActivity.class);
            //startActivity(photoIntent);
        }
    }

    private void saveParametrs(){
        String number = mTextX.getText().toString();
        float num = Float.parseFloat(number);
        Preference.setXValue(getActivity(),num);

        number = mTextY.getText().toString();
        num = Float.parseFloat(number);
        Preference.setYValue(getActivity(),num * 1000);

        if (String.valueOf(mSpinner.getSelectedItem()).equals("Светлый") ) {
            Preference.setColorPixel(getActivity(), Color.WHITE);
        } else {
            Preference.setColorPixel(getActivity(), Color.BLACK);
        }
        if (String.valueOf(mTime.getSelectedItem()).equals("с") ) {
            Preference.setTime(getActivity(), 1000000);
        } else {
            Preference.setTime(getActivity(), 1000);
        }
        Preference.setIsLine(getActivity(), (String.valueOf(mMode.getSelectedItem()).equals("Линии")));
        int iNum = Integer.parseInt(mTextMinWidth.getText().toString());
        Preference.setMinWidth(getActivity(),iNum);

        iNum = Integer.parseInt(mTextMaxWidth.getText().toString());
        Preference.setMaxWidth(getActivity(),iNum);

        iNum = Integer.parseInt(mTextMaxLenght.getText().toString());
        Preference.setMaxLenght(getActivity(),iNum);

        iNum = Integer.parseInt(mTextMinLenght.getText().toString());
        Preference.setMinLenght(getActivity(),iNum);

    }
}