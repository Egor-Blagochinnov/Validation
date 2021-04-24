package com.egorblagochinnov.validators

import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.lang.ref.WeakReference

fun <T> Condition<T>.isValid(value: T): Boolean {
    return this.validate(value).isValid
}

fun TextView.textChanges(): LiveData<String?> {
    val result = MutableLiveData<String?>()

    this.doAfterTextChanged {
        result.value = it?.toString()
    }

    return result
}

/**
 * Подключает валидатор [TextViewValidatorObserver] к [TextView]
 * Подписывается на валидатор (активирует его)
 * Следит за фокусом [TextView]
 *
 * @param lifecycleOwner - [LifecycleOwner] Нужен для подписки на валидатор [LiveDataValidator]
 * @param validator - Валидатор, по которому проверяется поле
 *
 * @return готовый [TextViewValidatorObserver]
 * **/
fun <D> TextView.validateBy(
        lifecycleOwner: LifecycleOwner,
        validator: LiveDataValidator<D>
): TextViewValidatorObserver<D> {
    val viewValidator = TextViewValidatorObserver(WeakReference(this), validator)
    viewValidator.activate(lifecycleOwner)
    viewValidator.setAsFocusListener()
    return viewValidator
}

/**
 * Создаёт условие проверки и подключает его к [TextView]
 *
 * @return готовый [TextViewValidator]
 * **/
fun TextView.validateBy(
    errorMessage: String,
    isValueValid: (value: CharSequence?) -> Boolean
): TextViewValidator<TextView> {
    return validateBy(Condition.create(errorMessage, isValueValid))
}

/**
 * Подключает условие валидности к [TextView]
 *
 * @return готовый [TextViewValidator]
 * **/
fun TextView.validateBy(
    condition: Condition<CharSequence?>
): TextViewValidator<TextView> {
    val viewValidator = TextViewValidator(WeakReference(this), condition)
    viewValidator.activate()
    return viewValidator
}