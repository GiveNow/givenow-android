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

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
                setupStripe(v);
                break;
        }
    }

    public void setupStripe(View view) {
        Card card = new Card("4242-4242-4242-4242", 12, 2014, "123");

        try {
            Stripe stripe = new Stripe("pk_test_T2v8tseWb9m0K2Qa9tCrJUE5");

            stripe.createToken(
                    card,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            // Send token to your server
                            Toast.makeText(getActivity(), token.toString(), Toast.LENGTH_LONG).show();

                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("token", token.getId());
                            ParseCloud.callFunctionInBackground("hello", params, new FunctionCallback<String>() {
                                @Override
                                public void done(String result, ParseException e) {
                                    if (e == null) {
                                        // result is "Hello world!"
                                        Toast.makeText(getActivity(), "What does the server say?  " + result, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        public void onError(Exception error) {
                            // Show localized error message
                            Toast.makeText(getActivity(),
                                    error.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
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