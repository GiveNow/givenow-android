package io.givenow.app.fragments.main.common;

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

import butterknife.Bind;
import butterknife.ButterKnife;
import io.givenow.app.R;


public class PhoneNumberDialogFragment extends DialogFragment implements TextWatcher {
    @Bind(R.id.etPhone)
    EditText etPhone;
    @Bind(R.id.tvDisclaimer)
    TextView tvDisclaimer;

    AlertDialog dialog;
    DialogInterface.OnDismissListener mDismissListener = dialog -> {
    };

    public PhoneNumberDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public static PhoneNumberDialogFragment newInstance(String title, String phoneNumber, CharSequence disclaimer) {
        PhoneNumberDialogFragment frag = new PhoneNumberDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("phoneNumber", phoneNumber);
        args.putCharSequence("disclaimer", disclaimer);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()); // R.style.Theme_Onewarmcoat_Dialog); //??
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_confirm_request_dialog, null);
        ButterKnife.bind(this, view);

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setTitle(title);
//        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
        alertDialogBuilder.setPositiveButton(R.string.dialog_phoneNumber_positiveButton, (dialog_param, which) -> {// on success
            // listener will be implemented by either RequestPickupFragment or PickupRequestDetailFragment
            PhoneNumberDialogListener listener = (PhoneNumberDialogListener) getParentFragment();
            listener.onFinishPhoneNumberDialog(etPhone.getText().toString());
        });
        alertDialogBuilder.setNegativeButton(R.string.dialog_phoneNumber_negativeButton, null);

//        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#246d9e'>" + title + "</font>"));
        alertDialogBuilder.setTitle(title);
        dialog = alertDialogBuilder.create();
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        etPhone.setText(getArguments().getString("phoneNumber"));
        tvDisclaimer.setText(getArguments().getCharSequence("disclaimer"));

        etPhone.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        etPhone.addTextChangedListener(this);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener dismissListener) {
        this.mDismissListener = dismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // this is very temperamental here, so in the interests of demo stability,
        // if i see this crashing again, maybe move this to an earlier point
        Dialog d = getDialog();
        if (d != null) {
            d.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        mDismissListener.onDismiss(dialog);
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

    public interface PhoneNumberDialogListener {
        void onFinishPhoneNumberDialog(String phoneNumber);
    }

}