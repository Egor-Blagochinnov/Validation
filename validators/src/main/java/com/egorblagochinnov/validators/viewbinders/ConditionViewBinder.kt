package com.egorblagochinnov.validators.viewbinders

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.egorblagochinnov.validators.Condition
import com.egorblagochinnov.validators.LiveDataValidator
import com.egorblagochinnov.validators.ValidationResult
import java.lang.ref.WeakReference

/**
 * Binds the [condition] validator to the view
 *
 * @param viewRef - Weak reference to <V>, which should react to the validator
 * @param condition - The condition that checks the data <D>
 * **/
abstract class ConditionViewBinder<V : View, D>(
    private val viewRef: WeakReference<V>,
    private val condition: Condition<D?>
) : LifecycleObserver {
    protected val view: V?; get() = viewRef.get()

    /**
     * Triggers validation
     * **/
    fun check() {
        check(getValidationData(view))
    }

    protected fun check(data: D?) {
        onValidationResult(view, validate(data))
    }

    /**
     * Triggers validation and return result as Boolean (valid or invalid)
     * **/
    fun isValid(): Boolean {
        return validate().isValid
    }

    /**
     * Triggers validation with current data in view and return [ValidationResult]
     * **/
    fun validate(): ValidationResult {
        return validate(getValidationData(view))
    }

    /**
     * Triggers validation with [data] and return [ValidationResult]
     * **/
    fun validate(data: D?): ValidationResult {
        return condition.validate(data)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        attach()
    }

    /**
     * Indicates that [ConditionViewBinder] is attached to lifecycle
     *
     * **/
    open fun attach() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected fun onDestroy() {
        detach()
    }

    @CallSuper
    open fun detach() {
        viewRef.clear()
    }

    /**
     * Сообщает о результате проверки поля
     * Просто вызывает метод onValidationResult(view: V?, result: ValidationResult?)
     * и подставляет туда view
     *
     * @param result - результат проверки
     * **/
    protected fun onValidationResult(result: ValidationResult?) {
        onValidationResult(view, result)
    }

    /**
     * Возвразает из view данные которые нужно проверить по валидатору
     *
     * @return D - Данные для проверки валидатором
     * **/
    abstract fun getValidationData(view: V?): D?

    /**
     * Сообщает о результате проверки поля
     *
     * @param view - поле, котрое проверяется
     * @param result - результат проверки
     * **/
    protected abstract fun onValidationResult(view: V?, result: ValidationResult?)
}