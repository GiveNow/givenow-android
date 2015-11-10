package io.givenow.app.helpers;

import android.view.View;
import android.widget.EditText;

/**
 * Used to format the amount views.
 */
public class AmountOnFocusChangeListener implements View.OnFocusChangeListener {
    @Override
    public void onFocusChange(View p_view, boolean p_hasFocus) {
        // This listener will be attached to any view containing amounts.
        EditText v_amountView = (EditText) p_view;
        if (p_hasFocus) {
            // v_value is using a currency mask - transfor over to cents.
            String v_value = v_amountView.getText().toString();
            int v_cents = Utils.parseAmountToCents(v_value);
            // Now, format cents to an amount (without currency mask)
            v_value = Utils.formatCentsToAmount(v_cents);
            v_amountView.setText(v_value);
            // Select all so the user can overwrite the entire amount in one shot.
            v_amountView.selectAll();
        } else {
            // v_value is not using a currency mask - transfor over to cents.
            String v_value = v_amountView.getText().toString();
            int v_cents = Utils.parseAmountToCents(v_value);
            // Now, format cents to an amount (with currency mask)
            v_value = Utils.formatCentsToCurrency(v_cents);
            v_amountView.setText(v_value);
        }
    }
}