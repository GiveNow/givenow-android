package io.givenow.app.fragments


import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.google.android.gms.analytics.HitBuilders
import com.parse.ParseUser
import io.givenow.app.GiveNowApplication
import io.givenow.app.R
import io.givenow.app.activities.MainActivity
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.withArguments

/**
 * Created by aphex on 11/23/15.
 *
 */
class PhoneNumberOnboardingFragment : Fragment(), PhoneNumberVerificationFragment.OnUserLoginCompleteListener {
    @BindView(R.id.title)
    lateinit var tvTitle: TextView
    @BindView(R.id.main)
    lateinit var llMain: LinearLayout
    private var color: Int = 0
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = arguments?.getString("title") ?: title
        color = arguments?.getInt("color") ?: color

    }

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_onboarding_phone_number, container, false)
        unbinder = ButterKnife.bind(this, v)

        tvTitle.text = title
        llMain.setBackgroundColor(color)

        val phoneNumberVerificationFragment = PhoneNumberVerificationFragment.newInstance()
        childFragmentManager.beginTransaction()
                .add(R.id.phoneNumberFragmentContainer,
                        phoneNumberVerificationFragment,
                        "phoneNumberVerificationFragment")
                .commit()
        return v
    }

    @OnClick(R.id.btnAddPhoneNumberLater)
    fun onAddPhoneNumberLater(btnAddPhoneNumberLater: Button) {
        (activity.application as GiveNowApplication).defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("OnBoarding")
                        .setAction("AddPhoneNumberLaterClicked")
                        .setValue(1)
                        .build())

        onUserLoginComplete()
    }

    override fun onUserLoginComplete() {
        //set first time var
        val editor = PreferenceManager.getDefaultSharedPreferences(activity).edit()
        editor.putBoolean("RanBefore", true)
        editor.apply()

        (activity.application as GiveNowApplication).defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("OnBoarding")
                        .setAction("UserLoginComplete")
                        .setLabel(ParseUser.getCurrentUser().objectId)
                        .build())

        Log.d("Onboarding", "Starting MainActivity")
        startActivity<MainActivity>()
        activity.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbinder.unbind()
    }

    companion object {

        fun newInstance(title: String, color: Int): PhoneNumberOnboardingFragment {
            return PhoneNumberOnboardingFragment().withArguments("title" to title, "color" to color)
        }
    }
}
