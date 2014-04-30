package org.onewarmcoat.onewarmcoat.app.fragments.main.volunteer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.models.PickupRequest;

import butterknife.ButterKnife;
import butterknife.InjectView;

// ARC: Not used currently

public class AcceptPickupDialogFragment extends DialogFragment {

    @InjectView(R.id.dnameTV)
    TextView donorNameTextView;
    @InjectView(R.id.daddrTV)
    TextView donorAddressTextView;

    // try this out too
    PickupRequest currentPickupReq;

    public AcceptPickupDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static AcceptPickupDialogFragment newInstance(PickupRequest pr) {
        AcceptPickupDialogFragment frag = new AcceptPickupDialogFragment();
        Bundle args = new Bundle();
        String name = pr.getName();
        String addr = pr.getAddresss();

        args.putString("donorName", name);
        args.putString("donorAddress", addr);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //this crashes all of the things
//        getDialog().getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
        super.onDismiss(dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //String title = getArguments().getString("title");
        String title = "Pick This Donation Up?";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();


        View view = inflater.inflate(R.layout.fragment_accept_pickup_dialog, null);
        ButterKnife.inject(this, view);

        String name = getArguments().getString("donorName");
        String address = getArguments().getString("donorAddress");

        donorNameTextView.setText(name);
        donorAddressTextView.setText(address);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);


        alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // on success
                AcceptPickupDialogListener listener = (AcceptPickupDialogListener) getParentFragment();
                listener.onConfirmAcceptDialog();
                dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();


        //return alertDialogBuilder.create();
        return dialog;
    }

    public interface AcceptPickupDialogListener {
        // go to PickupRequestsFragment to get access to vars for Donation persistence
        void onConfirmAcceptDialog();
    }

}