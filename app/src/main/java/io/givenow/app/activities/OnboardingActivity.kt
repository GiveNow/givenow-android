package io.givenow.app.activities

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.widget.ImageView
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.google.android.gms.analytics.HitBuilders
import io.givenow.app.GiveNowApplication
import io.givenow.app.R
import io.givenow.app.fragments.PhoneNumberOnboardingFragment
import io.givenow.app.fragments.PhoneNumberVerificationFragment

/**
 * Created by aphex on 11/16/15.
 *
 */
class OnboardingActivity : AppIntro2(),
        PhoneNumberVerificationFragment.OnUserLoginCompleteListener {

    private lateinit var phoneNumberOnboardingFragment: PhoneNumberOnboardingFragment
    private lateinit var ivDone: ImageView

    // Please DO NOT override onCreate. Use init
    override fun init(savedInstanceState: Bundle?) {
        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_1_title),
                getString(R.string.onboarding_1_description),
                R.drawable.ic_launcher_google_play,
                resources.getColor(R.color.onboarding_1_color)))
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_2_title),
                getString(R.string.onboarding_2_description),
                R.drawable.onboarding_2_image,
                resources.getColor(R.color.onboarding_2_color)))
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_3_title),
                getString(R.string.onboarding_3_description),
                R.drawable.onboarding_3_image,
                resources.getColor(R.color.onboarding_3_color)))
        addSlide(AppIntroFragment.newInstance(getString(R.string.onboarding_4_title),
                getString(R.string.onboarding_4_description),
                R.drawable.onboarding_4_image,
                resources.getColor(R.color.onboarding_4_color)))

        phoneNumberOnboardingFragment = PhoneNumberOnboardingFragment.newInstance(getString(R.string.phone_number_verification_title),
                resources.getColor(R.color.onboarding_5_color))
        addSlide(phoneNumberOnboardingFragment)

        // OPTIONAL METHODS
        // Override bar/separator color
        //        setBarColor(Color.parseColor("#3F51B5"));
        //        setSeparatorColor(Color.parseColor("#2196F3"));

        //TODO: possibly implement color fade effect between slides instead of this
        setFadeAnimation()
        // Hide Skip/Done button
        //        showSkipButton(false);
        showDoneButton(false)

        ivDone = findViewById(com.github.paolorotolo.appintro.R.id.done) as ImageView

        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                val tracker = (application as GiveNowApplication).defaultTracker
                tracker.setScreenName("Onboarding:" + position.toString())
                tracker.send(HitBuilders.ScreenViewBuilder().build())
            }
        })
    }

    override fun onDonePressed() = Unit

    override fun onUserLoginComplete() {
        phoneNumberOnboardingFragment.onUserLoginComplete()
    }

}