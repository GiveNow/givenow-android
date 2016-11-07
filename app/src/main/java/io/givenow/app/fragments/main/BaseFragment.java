package io.givenow.app.fragments.main;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;

import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

/**
 * Created by aphex on 11/29/15.
 */
@FragmentWithArgs
public class BaseFragment extends Fragment {

    @CallSuper
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArgs.inject(this); // read @Arg fields
    }

//    @CallSuper
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        ButterKnife.bind(this, view);
//        return view;
//    }

//    @CallSuper
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        unbinder.unbind()
//    }
}