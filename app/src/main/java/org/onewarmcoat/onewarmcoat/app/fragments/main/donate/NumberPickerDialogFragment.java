package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import org.onewarmcoat.onewarmcoat.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class NumberPickerDialogFragment extends DialogFragment {

    @InjectView(R.id.numberPicker)
    NumberPicker numberPicker;

    public NumberPickerDialogFragment() {
        // Empty constructor required for NumberPickerDialogFragment
    }

    public static NumberPickerDialogFragment newInstance(String title, int initialNum) {
        NumberPickerDialogFragment frag = new NumberPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("initialNum", initialNum);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        etName.setOnEditorActionListener(this);
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_number_picker_dialog, null);
        ButterKnife.inject(this, view);

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                NumberPickerDialogListener listener = (NumberPickerDialogListener) getParentFragment();
                listener.onFinishNumberPickerDialog(numberPicker.getValue());
                dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        int initialNum = getArguments().getInt("initialNum");
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(100);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(initialNum);

        return alertDialogBuilder.create();
    }

    public interface NumberPickerDialogListener {
        void onFinishNumberPickerDialog(int value);
    }

//    @Override
//    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//        if (EditorInfo.IME_ACTION_DONE == actionId) {
//            // Return input text to activity
//            ConfirmPickupDialogListener listener = (ConfirmPickupDialogListener) getParentFragment();
//            listener.onFinishConfirmPickupDialog(etName.getText().toString(), etPhone.getText().toString());
//            dismiss();
//            return true;
//        }
//        return false;
//
//    }
}