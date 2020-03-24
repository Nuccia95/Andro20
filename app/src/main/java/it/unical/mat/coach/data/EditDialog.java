package it.unical.mat.coach.data;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import it.unical.mat.coach.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class EditDialog extends AppCompatDialogFragment {

    private EditText editWeight;
    private EditText editHeight;
    private EditText editGender;
    private EditDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_dialog, null);

        editWeight = view.findViewById(R.id.edit_weight);
        editHeight = view.findViewById(R.id.edit_height);
        editGender = view.findViewById(R.id.edit_gender);

        builder.setView(view)
                .setTitle("Edit")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String weight = editWeight.getText().toString();
                        String height = editHeight.getText().toString();
                        String gender = editGender.getText().toString();
                        listener.applyTexts(weight, height, gender);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (EditDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement EditDialogListener");
        }
    }

    public interface EditDialogListener {
        void applyTexts(String weight, String height, String gender);
    }

}
