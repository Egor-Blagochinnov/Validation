package com.egorblagochinnov.validators.viewbinders

import android.view.View
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.egorblagochinnov.validators.Condition
import com.egorblagochinnov.validators.ValidationResult
import java.lang.ref.WeakReference

/**
 * Для валидации любых данных <D> в любых view <V>
 *
 * @param viewRef - Слабая ссылка на <V> (любой класс наследованный от View), который надо валидировать
 * @param condition - Условие, которое и проверяет данные <D>
 * **/
abstract class ConditionViewBinder<V : View, D>(
    private val viewRef: WeakReference<V>,
    private val condition: Condition<D?>
) : LifecycleObserver {
    protected val view: V?; get() = viewRef.get()

    fun check() {
        check(getValidationData(view))
    }

    /** Проверяет данные и устанавливает результат валидации на View **/
    protected fun check(data: D?) {
        onValidationResult(view, validate(data))
    }

    /** Проверяет поле **/
    fun isValid(): Boolean {
        return validate().isValid
    }

    fun validate(): ValidationResult {
        return validate(getValidationData(view))
    }

    fun validate(data: D?): ValidationResult {
        return condition.validate(data)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected fun onStart() {
        attach()
    }

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