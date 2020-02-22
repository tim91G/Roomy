package com.timgortworst.roomy.domain.utils

import android.content.Context
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.util.Log.e
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.core.content.ContextCompat.getSystemService
import android.app.Activity
import kotlinx.android.synthetic.main.layout_recurrence_picker.*


fun Context.showToast(stringResource: Int, length: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, getString(stringResource), length).show()
}

fun Activity.clearFocus(view: View) {
    view.clearFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
}

fun View.runBeforeDraw(onFinished: (view: View) -> Unit) {
    val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            Runnable { onFinished.invoke(this@runBeforeDraw) }.run()
            viewTreeObserver.removeOnPreDrawListener(this)
            return true
        }
    }
    viewTreeObserver.addOnPreDrawListener(preDrawListener)
}

@Suppress("DEPRECATION")
fun String.fromHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}
