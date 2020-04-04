package com.timgortworst.roomy.presentation.features.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.timgortworst.roomy.R
import com.timgortworst.roomy.domain.model.firestore.User
import com.timgortworst.roomy.domain.model.response.Response
import com.timgortworst.roomy.domain.model.ui.EasterEgg
import com.timgortworst.roomy.domain.usecase.SettingsUseCase

class SettingsViewModel(
    private val settingsUseCase: SettingsUseCase
) : ViewModel() {

    private val _htmlText = MutableLiveData<String>("")
    val htmlText: LiveData<String>
        get() = _htmlText

    private val _easterEgg = MutableLiveData<EasterEgg?>()
    val easterEgg: LiveData<EasterEgg?>
        get() = _easterEgg

    val currentUser = settingsUseCase.getCurrentUser()

    fun onAppVersionClick(count: Int) {
        _easterEgg.value = settingsUseCase.onAppVersionClick(count)
    }

    fun setDisclaimerText(htmlText: String) {
        _htmlText.value = htmlText
    }
}