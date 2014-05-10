package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import org.onewarmcoat.onewarmcoat.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ConfirmRequestDialogFragment extends DialogFragment implements TextWatcher {

    @InjectView(R.id.etName)
    EditText etName;
    @InjectView(R.id.etPhone)
    EditText etPhone;
    @InjectView(R.id.tvDisclaimer)
    TextView tvDisclaimer;

    AlertDialog dialog;

    public ConfirmRequestDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static ConfirmRequestDialogFragment newInstance(String title, String name, String phoneNumber, CharSequence disclaimer) {
        ConfirmRequestDialogFragment frag = new ConfirmRequestDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("name", name);
        args.putString("phoneNumber", phoneNumber);
        args.putCharSequence("disclaimer", disclaimer);
        frag.setArguments(args);
        return frag;
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
                // listener will be implemented by either RequestPickupDetailFragment or PickupRequestDetailFragment
                ConfirmPickupDialogListener listener = (ConfirmPickupDialogListener) getParentFragment();
                listener.onFinishConfirmPickupDialog(etName.getText().toString(), etPhone.getText().toString());

            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });

        return alertDialogBuilder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        etName.setText(getArguments().getString("name"));
        etPhone.setText(getArguments().getString("phoneNumber"));
        tvDisclaimer.setText(getArguments().getCharSequence("disclaimer"));

        etName.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//         etName.addTextChangedListener(this);
        etPhone.addTextChangedListener(this);

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
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String phoneContents = etPhone.getText().toString();

        if (phoneContents.equals("")) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        } else {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }

    public interface ConfirmPickupDialogListener {
        void onFinishConfirmPickupDialog(String name, String phoneNumber);
    }

}