package com.hse.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hse.core.viewmodels.BaseViewModel
import com.hse.core.viewmodels.ViewModelParams
import dagger.MapKey
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

//@Singleton
open class BaseViewModelFactory @Inject constructor(
    private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {

    var paramsInjector: (() -> ViewModelParams)? = null

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = viewModels[modelClass]?.get() as T
        paramsInjector?.let {
            if (viewModel is BaseViewModel) viewModel.params = it.invoke()
        }
        return viewModel
    }
}