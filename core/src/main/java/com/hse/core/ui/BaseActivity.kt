package com.hse.core.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hse.core.BaseApplication
import com.hse.core.common.BaseViewModelFactory
import com.hse.core.navigation.Navigation
import com.hse.core.utils.KeyboardInterceptor
import com.hse.core.utils.KeyboardListener
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {
    var navigation: Navigation? = null

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory
    @Inject
    lateinit var keyboardInterceptor: KeyboardInterceptor
    private lateinit var keyboardListener: KeyboardListener

    init {
        BaseApplication.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keyboardListener = KeyboardListener(this, keyboardInterceptor)
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardListener.clean()
    }

    override fun onBackPressed() {
        if (navigation?.onBackPressed() == false) super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        navigation?.getCurrentTopFragment()?.onActivityResult(requestCode, resultCode, data)
    }

}