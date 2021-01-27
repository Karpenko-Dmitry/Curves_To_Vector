package ru.mephi.curvestovector;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;


public class DialogParabolaFragment extends DialogFragment {

    public static final String EXTRA_NUM =
            "ru.mephi.parabola";
    private EditText mEditText;

    public static DialogParabolaFragment newInstance() {
        return new DialogParabolaFragment();
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.parabola_amount,null);
        mEditText = v.findViewById(R.id.number);
        return new AlertDialog.Builder(getActivity()).
                setView(v).
                setTitle("Количество кривых на изображении").
                setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int num = Integer.parseInt(mEditText.getText().toString());
                        sendResult(Activity.RESULT_OK,num);
                    }
                }).create();
    }

    protected void sendResult(int resultCode, int amount) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NUM,amount);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }
}
