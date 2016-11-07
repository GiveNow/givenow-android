package io.givenow.app.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

/**
 * Created by aphex on 11/23/15.
 *
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetIntent: Intent = when {
            isFirstTime -> Intent(this, OnboardingActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }

        // So push notification data gets passed on:
        intent?.extras?.let { extras -> targetIntent.putExtras(extras) }
//        if (intent != null) {
//            if (intent.extras != null) {
//                targetIntent.putExtras(intent.extras)
//            }
//        }

        startActivity(targetIntent)
        finish()
    }

    /***
     * Checks that application runs first time and write flag at SharedPreferences

     * @return true if 1st time
     */
    private val isFirstTime: Boolean
        get() {
            val ranBefore = PreferenceManager.getDefaultSharedPreferences(baseContext).getBoolean("RanBefore", false)
            return !ranBefore
        }
}
