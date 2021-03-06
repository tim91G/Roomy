package com.timgortworst.tasqs.infrastructure.extension

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

fun Context.toast(stringResource: Int, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, getString(stringResource), length).show()
}

fun Activity.clearFocus(view: View) {
    view.clearFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
}

fun View.snackbar(
    message: String = "",
    actionMessage: String = "",
    anchorView: View? = null,
    length: Int = Snackbar.LENGTH_LONG,
    action: (() -> Unit)? = null
): Snackbar {
    val snackbar = Snackbar.make(this, message, length)
    if (action != null) snackbar.setAction(actionMessage) { action.invoke() }
    if (anchorView != null) snackbar.anchorView = anchorView
    snackbar.show()
    return snackbar
}