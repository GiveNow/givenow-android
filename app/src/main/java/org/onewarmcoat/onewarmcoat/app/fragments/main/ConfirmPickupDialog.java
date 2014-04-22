package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import org.onewarmcoat.onewarmcoat.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ConfirmPickupDialog extends DialogFragment {

    @InjectView(R.id.etName)
    EditText etName;
    @InjectView(R.id.etPhone)
    EditText etPhone;

    public ConfirmPickupDialog() {
        // Empty constructor required for DialogFragment
    }

    public static ConfirmPickupDialog newInstance(String title) {
        ConfirmPickupDialog frag = new ConfirmPickupDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Show soft keyboard automatically
        etName.requestFocus();
//        etName.setOnEditorActionListener(this);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
//        View view = inflater.inflate(R.layout.fragment_confirm_pickup_dialog, container);
//        ButterKnife.inject(this, view);


//        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_confirm_pickup_dialog, null);
        ButterKnife.inject(this, view);

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                ConfirmPickupDialogListener listener = (ConfirmPickupDialogListener) getParentFragment();
                listener.onFinishConfirmPickupDialog(etName.getText().toString(), etPhone.getText().toString());
                dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }

    public interface ConfirmPickupDialogListener {
        void onFinishConfirmPickupDialog(String name, String phoneNumber);
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