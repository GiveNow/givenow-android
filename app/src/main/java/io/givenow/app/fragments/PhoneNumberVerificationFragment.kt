package io.givenow.app.fragments

/**
 * Created by aphex on 11/26/15.
 */

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.givenow.app.R
import io.givenow.app.helpers.CustomAnimations
import io.givenow.app.models.ParseInstallationHelper
import io.givenow.app.models.ParseUserHelper
import rx.android.schedulers.AndroidSchedulers
import rx.parse.ParseObservable

/**
 * Created by aphex on 11/23/15.
 *
 */
class PhoneNumberVerificationFragment : DialogFragment() {

    var messageResource = R.string.phone_number_disclaimer

    @BindView(R.id.llContainer)
    lateinit var llContainer: LinearLayout
    @BindView(R.id.title)
    lateinit var tvTitle: TextView
    @BindView(R.id.tsDescription)
    lateinit var tsDescription: TextSwitcher
    @BindView(R.id.etPhoneNumber)
    lateinit var etPhoneNumber: EditText
    @BindView(R.id.etSMSCode)
    lateinit var etSMSCode: EditText
    @BindView(R.id.back)
    lateinit var ibBack: ImageButton
    @BindView(R.id.done)
    lateinit var ibDone: ImageButton
    @BindView(R.id.vsPhoneSMS)
    lateinit var vsPhoneSMS: ViewSwitcher
    @BindView(R.id.progressIndicator)
    lateinit var progressIndicator: ProgressBar

    private var mPhoneNumberFieldShowing = true
    private lateinit var unbinder: Unbinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_phone_verification, container, false)
        unbinder = ButterKnife.bind(this, v)

        messageResource = arguments?.getInt("messageResource", messageResource) ?: messageResource // or leave it unchanged from the default

        etPhoneNumber.addTextChangedListener(android.telephony.PhoneNumberFormattingTextWatcher()) //new PhoneNumberFormattingTextWatcher(Locale.getDefault().getCountry()));
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) { //TODO or if valid phone number
                    if (ibDone.visibility != View.VISIBLE) {
                        CustomAnimations.circularReveal(ibDone).start()
                    }
                    progressIndicator.visibility = View.VISIBLE
                } else {
                    if (ibDone.visibility == View.VISIBLE) {
                        CustomAnimations.circularHide(ibDone).start()
                    }
                    progressIndicator.visibility = View.INVISIBLE
                }
            }
        })

        etSMSCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable) {
                if (s.length == 4) {
                    if (ibDone.visibility != View.VISIBLE) {
                        CustomAnimations.circularReveal(ibDone).start()
                    }
                } else {
                    if (ibDone.visibility == View.VISIBLE) {
                        CustomAnimations.circularHide(ibDone).start()
                    }
                }
            }
        })

        val locale = resources.configuration.locale.country
        Log.d("phfrag", "locale is " + locale)
        etPhoneNumber.setText("+" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(locale).toString())
        etPhoneNumber.setSelection(etPhoneNumber.text.length)

        if (showsDialog) {
            //If we're being displayed in a dialog, modify a few views.
            tvTitle.visibility = View.VISIBLE
            tvTitle.setText(R.string.phone_number_verification_title)
            progressIndicator.indeterminateDrawable.setColorFilter(resources.getColor(R.color.colorPrimaryLight), android.graphics.PorterDuff.Mode.SRC_ATOP)
            val pad = resources.getDimensionPixelSize(R.dimen.dialog_container_padding)
            llContainer.setPadding(pad, pad, pad, pad)
            llContainer.requestLayout()

            tsDescription.setFactory {
                //Dialogs show their text left-justified rather than centered.
                tvFactory().apply {
                    gravity = Gravity.START
                    textSize = 18f
                }
            }
        } else {
            tsDescription.setFactory { this.tvFactory() }
        }

        tsDescription.setText(getString(messageResource))
        return v
    }

    private fun tvFactory(): TextView {
        return LayoutInflater.from(activity).inflate(R.layout.textview_phone_verify_description, tsDescription, false) as TextView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //seems to happen before onCreateView
        val dialog = super.onCreateDialog(savedInstanceState)
        //no title for this dialog please (a title bar appears on API<21)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    val phoneNumber: String
        get() = etPhoneNumber.text.toString()

    @OnClick(R.id.done)
    fun onDonePressed(ibDone: ImageButton) {
        ibDone.isClickable = false
        if (mPhoneNumberFieldShowing)
            sendCode()
        else
            doLogin()
    }

    @OnClick(R.id.back)
    fun onIbBackPressed(ibBack: ImageButton) {
        uiPhoneNumber()
    }

    private fun uiPhoneNumber(@StringRes messageResource: Int = this.messageResource) {
        vsPhoneSMS.displayedChild = 0
        mPhoneNumberFieldShowing = true
        CustomAnimations.circularHide(ibBack).start()
        if (phoneNumber.isNotEmpty()) {
            CustomAnimations.circularReveal(ibDone).start()
        }
        tsDescription.setText(getString(messageResource))
        ibDone.isClickable = true
    }

    private fun uiSMSCode(phoneNumber: String) {
        vsPhoneSMS.displayedChild = 1
        mPhoneNumberFieldShowing = false
        tsDescription.setText(getString(R.string.validate_sms_code, phoneNumber))
        CustomAnimations.circularReveal(ibBack).start()
        ibDone.isClickable = true
    }

    private fun sendCode() {
        val phoneNumber = phoneNumber
        val phoneUtil = PhoneNumberUtil.getInstance()
        if (phoneNumber.isNotEmpty()) {
            if (phoneNumber.contains("+")) {
                //validate phone number
                try {
                    val pn = phoneUtil.parse(phoneNumber, null)
                    if (phoneUtil.isValidNumber(pn)) {
                        //phone number is valid
                        //change done button to spinner
                        CustomAnimations.circularHide(ibDone).start()
                        //request a code
                        ParseUserHelper.sendCode(phoneNumber, getString(R.string.sms_body_javascript)).subscribe(
                                { response ->
                                    Log.d("Cloud Response", response.toString())
                                    //switch to sendSMS edittext
                                    uiSMSCode(phoneNumber)
                                },
                                { error ->
                                    Log.d("Cloud Response", "Error received from sendCode cloud function: ", error)
                                    uiPhoneNumber()
                                })
                    } else {
                        tsDescription.setText(getString(R.string.phone_number_verification_error_number_invalid))
                        ibDone.isClickable = true
                    }
                } catch (e: NumberParseException) {
                    e.printStackTrace()
                    tsDescription.setText(getString(R.string.phone_number_verification_error_number_failed_to_parse))
                    ibDone.isClickable = true
                }

            } else {
                tsDescription.setText(getString(R.string.phone_number_verification_error_no_country_code))
                ibDone.isClickable = true
            }
        } else { //Or just hide ibdone
            val shake = AnimationUtils.loadAnimation(activity, R.anim.shake)
            ibDone.startAnimation(shake)
            ibDone.isClickable = true
        }
    }

    private fun doLogin() {
        if (etSMSCode.text.toString().length == 4) {
            CustomAnimations.circularHide(ibDone).start()

            val phoneNumber = phoneNumber
            val code = Integer.parseInt(etSMSCode.text.toString())
            ParseUserHelper.logIn(phoneNumber, code).subscribe(
                    { sessionToken ->
                        ParseObservable.become(sessionToken.toString())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { becameUser ->
                                    Log.d("PhoneVerification", "Became user " + becameUser.username)
                                    ParseInstallationHelper.associateUserWithDevice(becameUser)
                                    userLoginComplete()
                                }
                    },
                    { error ->
                        Log.d("Cloud Response", "Error received from logIn cloud function: ", error)
                        uiPhoneNumber(R.string.phone_number_verification_error_sms_code)
                    })
        }
    }

    private fun userLoginComplete() {
        //change done button to givenow smiley
        ibDone.setImageResource(R.mipmap.ic_launcher)
        val reveal = CustomAnimations.circularReveal(ibDone)
        reveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val parentFragment = parentFragment
                if (parentFragment is OnUserLoginCompleteListener) {
                    parentFragment.onUserLoginComplete()
                }
                if (dialog != null) {
                    dismiss()
                }
            }
        })
        reveal.start()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

        val parentFragment = parentFragment
        if (parentFragment is DialogInterface.OnDismissListener) {
            parentFragment.onDismiss(dialog)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    interface OnUserLoginCompleteListener {
        fun onUserLoginComplete()
    }

    companion object {

        fun newInstance(): PhoneNumberVerificationFragment {
            return PhoneNumberVerificationFragment()
        }
    }
}
