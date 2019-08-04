package com.timgortworst.roomy.ui.main.view

interface PageStateListener {
    fun setErrorView(isVisible: Boolean, title: Int? = null, text: Int? = null)
    fun setLoadingView(isLoading: Boolean)
}