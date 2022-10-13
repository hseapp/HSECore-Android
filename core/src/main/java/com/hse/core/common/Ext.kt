/*
 * Copyright (c) 2020 National Research University Higher School of Economics
 * All Rights Reserved.
 */

package com.hse.core.common

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.hse.core.BaseApplication
import com.hse.core.R
import com.hse.core.ui.BaseActivity
import com.hse.core.ui.BaseFragment
import com.hse.core.utils.ClickListener
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt


fun BaseFragment<*>.activity() = activity as? BaseActivity

inline fun <reified E> BaseFragment<*>.observeStateChanges(crossinline f: (state: E) -> Unit) =
    viewModel.loadingState?.observe(this, Observer { if (it is E) f(it) })

operator fun <T> MutableLiveData<ArrayList<T>>.plusAssign(values: List<T>) {
    val value = this.value ?: arrayListOf()
    value.addAll(values)
    this.value = value
}

fun showToast(@StringRes text: Int, long: Boolean = false) = showToast(string(text), long)
fun showToast(text: String, long: Boolean = false) {
    Toast.makeText(
        BaseApplication.appContext,
        text,
        if (long) Toast.LENGTH_LONG else Toast.LENGTH_LONG
    ).show()
}


fun View.fadeIn(duration: Long = 150, delay: Long = 0, doOnComplete: (() -> Unit)? = null) {
    if (visibility == View.VISIBLE && alpha == 1f) return
    clearAnimation()
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setStartDelay(delay)
        .setDuration(duration)
        .withEndAction {
            doOnComplete?.invoke()
        }.start()
}

fun View.fadeOut(duration: Long = 150, delay: Long = 0, doOnComplete: (() -> Unit)? = null) {
    if (visibility == View.GONE) return
    clearAnimation()
    animate()
        .alpha(0f)
        .setStartDelay(delay)
        .setDuration(duration)
        .withEndAction {
            visibility = View.GONE
            doOnComplete?.invoke()
        }.start()
}

fun View.setGone() {
    visibility = View.GONE
}

fun View.setVisible() {
    visibility = View.VISIBLE
}

fun View.setInvisible() {
    visibility = View.INVISIBLE
}

fun View?.onClick(onClick: ((View?) -> Unit)?) {
    if (onClick == null) this?.setOnClickListener(null)
    else this?.setOnClickListener(ClickListener(onClick))
}

private var density = 0f
private fun getDensity(context: Context): Float {
    if (density == 0f) density = context.resources.displayMetrics.density
    return density
}

fun dp(dp: Float): Float {
    val density = getDensity(BaseApplication.appContext)
    return if (dp == 0f) 0f
    else ceil(density * dp)
}

fun dip(dp: Float): Int = dp(dp).toInt()

fun getSelectableItemBackgroundBorderless(): Int {
    val outValue = TypedValue()
    BaseApplication.appContext.theme
        .resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true)
    return outValue.resourceId
}

fun getSelectableItemBackground(): Int {
    val outValue = TypedValue()
    BaseApplication.appContext.theme
        .resolveAttribute(R.attr.selectableItemBackground, outValue, true)
    return outValue.resourceId
}

fun View.hideKeyboard(clearFocus: Boolean = false) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val focusedView = (context as Activity).currentFocus
    if (focusedView != null) {
        imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
        if (clearFocus) focusedView.clearFocus()
    }
}

fun EditText.showKeyboard() {
    requestFocus()
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.animateTranslationZ(
    value: Float,
    duration: Long = 250,
    doOnComplete: (() -> Unit)? = null
) {
    animate().translationZ(value).setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                doOnComplete?.invoke()
            }
        })
        .start()
}

fun color(@ColorRes res: Int) = ContextCompat.getColor(BaseApplication.appContext, res)
fun string(@StringRes res: Int) = BaseApplication.appContext.getString(res)
fun drawable(@DrawableRes res: Int) = if (res == 0) null else BaseApplication.appContext.getDrawable(res)

fun openBrowser(context: Context, url: String?, internal: Boolean = false): Boolean {
    try {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        if (internal) i.`package` = context.packageName
        context.startActivity(i)
        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

@ColorInt
fun alphaColor(@ColorInt color: Int, factor: Float): Int {
    val alpha = (Color.alpha(color) * factor).roundToInt()
    val red: Int = Color.red(color)
    val green: Int = Color.green(color)
    val blue: Int = Color.blue(color)
    return Color.argb(alpha, red, green, blue)
}

fun isBitSet(value: Int, bit: Int) = value and bit != 0

fun isBitSetOrEmpty(value: Int, bit: Int) = value == 0 || value and bit != 0

fun getFileSizeString(size: Long?): String? {
    if (size == null) return null
    val df = DecimalFormat("0.00")
    val sizeKb = 1024.0f
    val sizeMb = sizeKb * sizeKb
    val sizeGb = sizeMb * sizeKb
    val sizeTerra = sizeGb * sizeKb
    return when {
        size < sizeMb -> df.format(size / sizeKb).toString() + " Kb"
        size < sizeGb -> df.format(
            size / sizeMb
        ).toString() + " Mb"
        size < sizeTerra -> df.format(size / sizeGb).toString() + " Gb"
        else -> null
    }
}

fun <T> List<T>.lastSafe(): T? {
    if (isEmpty()) return null
    return get(size - 1)
}

fun isAppVisible() = BaseApplication.visibleActivitiesCount > 0

fun Any.doOnAppResumed(action: () -> Unit) {
    if (isAppVisible()) action()
    else {
        val hash = this.hashCode()
        BaseApplication.addLifecycleObserver(hash, object : BaseApplication.Companion.LifecycleObserver {
            override fun onAppResumed() {
                action()
                BaseApplication.removeLifecycleObserver(hash)
            }

            override fun onAppPaused() {
            }
        })
    }
}

fun Any.doOnAppPaused(action: () -> Unit) {
    if (!isAppVisible()) action()
    else {
        val hash = this.hashCode()
        BaseApplication.addLifecycleObserver(hash, object : BaseApplication.Companion.LifecycleObserver {
            override fun onAppResumed() {
            }

            override fun onAppPaused() {
                action()
                BaseApplication.removeLifecycleObserver(hash)
            }
        })
    }
}

fun DateFormat.parseOrDefault(src: String?): Date? {
    return try {
        parse(src)
    } catch (e: java.lang.Exception) {
        Date()
    }
}

fun downloadFile(url: String, fileName: String? = UUID.randomUUID().toString(), title: String? = fileName, description: String? = fileName) {
    try {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setDescription(description)
        request.setTitle(title)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        (BaseApplication.appContext.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager)?.enqueue(request)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun <T> List<T>.lastSafe(predicate: (T) -> Boolean): T? {
    return try {
        last(predicate)
    } catch (e: Exception) {
        null
    }
}

fun Int?.safe(default: Int = 0) : Int = this ?: default
fun Long?.safe(default: Long = 0) : Long = this ?: default
fun Float?.safe(default: Float = 0f) : Float = this ?: default
fun Double?.safe(default: Double = 0.0) : Double = this ?: default
fun String?.safe(default: String = "") : String = this ?: default

fun TextView.bind(text: CharSequence?) {
    if (text == null) {
        this.setGone()
    } else {
        this.text = text
        this.setVisible()
    }
}

inline var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

infix fun <T1, T2, T3> Pair<T1, T2>.to(third: T3) = Triple(first, second, third)

fun Context.downloadFile(url: String, fileName: String? = "File") {
    if (this !is Activity) return
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
    } else {
        com.hse.core.common.downloadFile(url, fileName)
    }
}

fun downloadFile(url: String, fileName: String? = "File") {
    try {
        val dm = BaseApplication.appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(fileName)
        val title = fileName?.replace("\\W*", "") ?: ""
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
        dm.enqueue(request)
    } catch (e: Throwable) {
        showToast(R.string.error_occurred)
    }
}

inline fun <T> T.applyIf(predicate: Boolean, action: T.() -> Unit): T {
    if (predicate) action()
    return this
}

fun RecyclerView.hideKeyboardOnScroll() {
    val hideKeyboardScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) recyclerView.hideKeyboard(true)
        }
    }
    addOnScrollListener(hideKeyboardScrollListener)
}

fun Fragment.isNotAvailable(): Boolean {
    val activity = activity
    return activity == null || !isAdded || isRemoving || isDetached || activity.isFinishing || activity.isDestroyed
}