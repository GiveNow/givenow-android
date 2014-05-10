package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.onewarmcoat.onewarmcoat.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ConfirmRequestDialogFragment extends DialogFragment {

    @InjectView(R.id.etName)
    EditText etName;
    @InjectView(R.id.etPhone)
    EditText etPhone;


    public ConfirmRequestDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static ConfirmRequestDialogFragment newInstance(String title) {
        ConfirmRequestDialogFragment frag = new ConfirmRequestDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        etName.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        /*String parentFragname = getParentFragment().getTag();
        if(parentFragname.equals("PickupRequestDetailFragment")) {
            tvDisclaimer.setText("You will only be called if there is a problem with the donation you are picking up");
        }*/
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // this is very temperamental here, so in the interests of demo stability,
        // if i see this crashing again, maybe move this to an earlier point
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onDismiss(dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_confirm_request_dialog, null);
        ButterKnife.inject(this, view);

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                Fragment parentFragment = getParentFragment();
                String parentFragName = parentFragment.getTag();
                /* listener will be implemented by either RequestPickupDetailFragment or
                   PickupRequestDetailFragment
                 */
                ConfirmPickupDialogListener listener = (ConfirmPickupDialogListener) parentFragment;
                listener.onFinishConfirmPickupDialog(etName.getText().toString(), etPhone.getText().toString());

//                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
                Toast.makeText(getActivity(), "Clicked Cancel!", Toast.LENGTH_LONG);
            }
        });

        /*AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();*/

        /*if (etName.getText() == null || etPhone.getText() == null)
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);*/

        return alertDialogBuilder.create();
        //return dialog;
    }

    public interface ConfirmPickupDialogListener {
        void onFinishConfirmPickupDialog(String name, String phoneNumber);
    }

}