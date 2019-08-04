package com.timgortworst.roomy.ui.splash.presenter

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.timgortworst.roomy.domain.SetupInteractor
import com.timgortworst.roomy.ui.splash.ui.SplashView
import com.timgortworst.roomy.utils.CoroutineLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SplashPresenter @Inject constructor(
        private val view: SplashView,
        private val setupInteractor: SetupInteractor
) : DefaultLifecycleObserver {

    private val scope = CoroutineLifecycleScope(Dispatchers.Main)

    init {
        if (view is LifecycleOwner) {
            view.lifecycle.addObserver(scope)
        }
    }

    fun initializeUser(referredHouseholdId: String = "") = scope.launch {
        // Google sign in
        if (FirebaseAuth.getInstance().currentUser == null) {
            view.goToGoogleSignInActivity()
            return@launch
        }

        // continue to main activity
        if (setupInteractor.getHouseholdIdForUser().isNotBlank()) {
            view.goToMainActivity()
            return@launch
        }

        // setup new or referred user
        if (referredHouseholdId.isNotBlank()) {
            view.goToSetupActivityReferred(referredHouseholdId)
        } else {
            view.goToSetupActivity()
        }
    }
}
