package com.timgortworst.roomy.presentation.features.splash.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.timgortworst.roomy.R
import com.timgortworst.roomy.data.utils.Constants
import com.timgortworst.roomy.domain.utils.showToast
import com.timgortworst.roomy.presentation.features.googlesignin.view.GoogleSignInActivity
import com.timgortworst.roomy.presentation.features.main.view.MainActivity
import com.timgortworst.roomy.presentation.features.setup.view.SetupActivity
import com.timgortworst.roomy.presentation.features.splash.presenter.SplashPresenter
import dagger.android.AndroidInjection
import javax.inject.Inject

class SplashActivity : AppCompatActivity(), SplashView {

    @Inject
    lateinit var presenter: SplashPresenter

    companion object {
        private const val TAG = "SplashActivity"
        private const val RESULT_CODE = 1234

        fun start(context: Context) {
            val intent = Intent(context, SetupActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        setTheme(R.style.AppTheme_Launcher)
        super.onCreate(savedInstanceState)

        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnCompleteListener {
            val referredHouseholdId = it.result?.link?.getQueryParameter(Constants.QUERY_PARAM_HOUSEHOLD).orEmpty()
            presenter.initializeUser(referredHouseholdId)
        }.addOnFailureListener {
            Log.e(TAG, it.localizedMessage.orEmpty())
            showToast(R.string.error_generic)
        }
    }

    override fun goToGoogleSignInActivity() {
        GoogleSignInActivity.startForResult(this, RESULT_CODE)
    }

    override fun goToSetupActivityReferred(referredHouseholdId: String) {
        SetupActivity.start(this, referredHouseholdId)
        finish()
    }

    override fun goToSetupActivity() {
        SetupActivity.start(this)
        finish()
    }

    override fun goToMainActivity() {
        MainActivity.start(this)
        finish()
    }

    override fun userInvalid() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE && resultCode == Activity.RESULT_OK) {
            presenter.initializeUser()
        } else {
            recreate()
            showToast(R.string.error_connection)
        }
    }
}
