package org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer;

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

// ARC: Not used currently

public class AcceptPickupDialogFragment extends DialogFragment {

    @InjectView(R.id.vnameET)
    EditText volunteerNameEditText;
    @InjectView(R.id.vphoneET)
    EditText volunteerPhoneEditText;


    public AcceptPickupDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static AcceptPickupDialogFragment newInstance(String title) {
        AcceptPickupDialogFragment frag = new AcceptPickupDialogFragment();
        Bundle args = new Bundle();
        args.putString("dialog_title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        volunteerNameEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("dialog_title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_accept_pickup_dialog, null);
        ButterKnife.inject(this, view);

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Fragment parentFragment = getParentFragment();
                String parentFragName = parentFragment.getTag();

                /*String volunteerName = volunteerNameEditText.getText().toString();
                String volunteerPhone = volunteerAddressEditText.getText().toString();*/

                AcceptPickupDialogListener listener = (AcceptPickupDialogListener) parentFragment;
                listener.onConfirmAcceptDialog(volunteerNameEditText.getText().toString(),
                        volunteerPhoneEditText.getText().toString());
                //dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();
                Toast.makeText(getActivity(), "Clicked Cancel!", Toast.LENGTH_LONG);
            }
        });

        // create the alert dialog and show it
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();


        //return alertDialogBuilder.create();
        return dialog;
    }

    public interface AcceptPickupDialogListener {
        // go to PickupRequestsFragment to get access to vars for Donation persistence
        void onConfirmAcceptDialog(String name, String phoneNum);
    }

}