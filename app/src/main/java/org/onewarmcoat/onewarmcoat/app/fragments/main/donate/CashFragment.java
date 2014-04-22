package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import org.onewarmcoat.onewarmcoat.app.R;

import java.util.HashMap;

public class CashFragment extends Fragment implements OnClickListener {

    private OnFragmentInteractionListener mListener;
    private EditText etDonateAmount;

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

        Button b = (Button) v.findViewById(R.id.btn_donate);
        b.setOnClickListener(this);

        etDonateAmount = (EditText) v.findViewById(R.id.et_donate_amount);

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_donate:
                if(validateInput()) {
                    //check if user has a Stripe account
                    //pop Card.io dialog
                    setupStripe();
                }
                break;
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

    public void setupStripe() {
        Card card = new Card("4242-4242-4242-4242", 12, 2014, "123");

        try {
            Stripe stripe = new Stripe("pk_test_T2v8tseWb9m0K2Qa9tCrJUE5");

            stripe.createToken(
                    card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server

                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("token", token.getId());
                            params.put("amount", etDonateAmount.getText().toString());
                            ParseCloud.callFunctionInBackground("stripe_charge_customer", params, new FunctionCallback<String>() {
                                @Override
                                public void done(String result, ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } catch (AuthenticationException e1) {
            e1.printStackTrace();
        }

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