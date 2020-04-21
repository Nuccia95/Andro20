package it.unical.mat.coach.data;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import it.unical.mat.coach.R;

public class DaysDialog extends AppCompatDialogFragment {

    private DaysDialog.EditDialogListener listener;
    String[] days;
    boolean[] checkedDays;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        days = getResources().getStringArray(R.array.days);
        checkedDays = new boolean[days.length];

        LayoutInflater inflater = getActivity().getLayoutInflater();
        //View view = inflater.inflate(R.layout.edit_workout_days_dialog, null);

        builder.setTitle("Edit Workout Days")
                .setMultiChoiceItems(days, checkedDays, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        if(isChecked)
                            checkedDays[position] = true;
                        else
                            checkedDays[position] = false;
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.setDays(checkedDays);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DaysDialog.EditDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement DaysDialogListener");
        }
    }

    public interface EditDialogListener {
        void setDays(boolean[] checkedDays);
    }

}
