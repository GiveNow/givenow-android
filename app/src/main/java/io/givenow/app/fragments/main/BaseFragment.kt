package io.givenow.app.fragments.main

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment

import com.hannesdorfmann.fragmentargs.FragmentArgs
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs

/**
 * Created by aphex on 11/29/15.
 */
@FragmentWithArgs
open class BaseFragment : Fragment() {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this) // read @Arg fields
    }
}