package org.onewarmcoat.onewarmcoat.app.fragments.main;

import android.app.DialogFragment;
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
        View view = inflater.inflate(R.layout.fragment_confirm_pickup_dialog, container);
        ButterKnife.inject(this, view);
        String title = getArguments().getString("title", "Confirm pickup");
        getDialog().setTitle(title);
        // Show soft keyboard automatically
        etName.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return view;
    }
}