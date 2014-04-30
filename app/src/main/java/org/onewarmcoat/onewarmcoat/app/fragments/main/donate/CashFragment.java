package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.models.CharityUserHelper;
import org.onewarmcoat.onewarmcoat.app.models.Donation;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class CashFragment extends Fragment {

    public static final int MY_SCAN_REQUEST_CODE = 01234;
    private static final String MY_CARDIO_APP_TOKEN = "ccb24a9a0d9d4d529c2f7f27cedc926e";

    @InjectView(R.id.et_donate_amount)
    EditText etDonateAmount;

    @InjectView(R.id.btn_donate)
    Button btnDonate;

    public static CashFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        CashFragment f = new CashFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_cash, container, false);
        ButterKnife.inject(this, v);

        //to allow layout resize with keyboard
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*
 * Called when the Activity becomes visible.
 */
    @Override
    public void onStart() {
        super.onStart();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    @OnClick(R.id.btn_donate)
    public void onDonate(Button b) {
        if(validateInput()) {
            //check if user has a Stripe token - Parse query
            if(CharityUserHelper.hasStripeCustomerId()){
                chargeStripeCustomer();
            }else{
                //user doesn't have a stored Stripe token
                //pop Card.io dialog
                getCardDetails();
            }
        }
    }

    private boolean validateInput() {
        String enteredAmount = etDonateAmount.getText().toString();

        int amount;
        try {
            //parseInt returns an int, so this will be auto-rounded to whole dollars
            amount = Integer.parseInt(enteredAmount);
            if(amount > 0){
                //heh, Android keyboard handles the decimal/negative case by default.  Should have tested before writing the code
                return true;
            }
        } catch (NumberFormatException nfe){
            //sad day, no moniez
        }

        Toast.makeText(getActivity(), "$" + enteredAmount + " is not valid input. " +
                "Please enter a valid donation amount > 0", Toast.LENGTH_LONG).show();
        return false;
    }

    private void getCardDetails() {
        if(!CardIOActivity.canReadCardWithCamera()){
            Toast.makeText(getActivity(), "whyyyyyyyy??", Toast.LENGTH_SHORT).show();
        }

        Intent scanIntent = new Intent(getActivity(), CardIOActivity.class);

        // required for authentication with card.io
        scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, MY_CARDIO_APP_TOKEN);

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: true
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

        Toast.makeText(getActivity(), "Starting Card.io Intent wheeeee", Toast.LENGTH_SHORT).show();

        // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
        startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MY_SCAN_REQUEST_CODE){
            String resultDisplayStr;
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                resultDisplayStr = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";

                // Do something with the raw number, e.g.:
                // myService.setCardNumber( scanResult.cardNumber );

                if (scanResult.isExpiryValid()) {
                    resultDisplayStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
                }

                if (scanResult.cvv != null) {
                    // Never log or display a CVV
                    resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
                }

                if (scanResult.postalCode != null) {
                    resultDisplayStr += "Postal Code: " + scanResult.postalCode + "\n";
                }

                newStripeCard(scanResult);
            }
            else {
                resultDisplayStr = "Scan was canceled.";
            }


            //TODO: This data like last 4, expiration, etc should get stored too.
            // do something with resultDisplayStr, maybe display it in a textView
            Toast.makeText(getActivity(), resultDisplayStr, Toast.LENGTH_LONG).show();

        }
    }

    public void newStripeCard(CreditCard cc) {
        Card card = new Card(cc.cardNumber, cc.expiryMonth, cc.expiryYear, cc.cvv);

        if(card.validateCard()) {
            try {
                Stripe stripe = new Stripe("pk_test_T2v8tseWb9m0K2Qa9tCrJUE5");

                stripe.createToken(
                        card,
                        new TokenCallback() {
                            public void onSuccess(Token token) {
                                chargeStripeToken(token);
                            }

                            public void onError(Exception error) {
                                // Show localized error message
                                Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                );
            } catch (AuthenticationException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(getActivity(), "Credit card invalid :(", Toast.LENGTH_LONG).show();
        }

    }

    private void chargeStripeToken(Token token) {
        // Send token to server to charge
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("token", token.getId());
        final String amount = etDonateAmount.getText().toString();
        params.put("amount", amount);

        params.put("user_name", CharityUserHelper.getName());
        //we also ideally want a name - profile data, which has a default value

        //need to call a different function to create new stripe customer (if we have authority/info to do so)
        ParseCloud.callFunctionInBackground("stripe_charge", params, new FunctionCallback<String>() {
            @Override
            public void done(String stripeCustomerId, ParseException e) {
                if (e == null) {
//                    Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
                    CharityUserHelper.setStripeCustomerId(stripeCustomerId);

                    Toast.makeText(getActivity(), CharityUserHelper.getStripeCustomerId(), Toast.LENGTH_LONG).show();

                    storeDonation(amount);
                }
            }
        });
    }

    private void chargeStripeCustomer() {
        // Send customer ID to server to charge
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("stripeCustomer", CharityUserHelper.getStripeCustomerId());
        final String amount = etDonateAmount.getText().toString();
        params.put("amount", amount);

        //need to call a different function to create new stripe customer (if we have authority/info to do so)
        ParseCloud.callFunctionInBackground("stripe_charge_customer", params, new FunctionCallback<String>() {
            @Override
            public void done(String result, ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();

                    storeDonation(amount);
                }
            }
        });
    }

    private void storeDonation(String amount){
        etDonateAmount.setText("");
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(etDonateAmount.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        Donation donation = new Donation(ParseUser.getCurrentUser(), Donation.CASH, Integer.parseInt(amount));
        donation.saveInBackground();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}