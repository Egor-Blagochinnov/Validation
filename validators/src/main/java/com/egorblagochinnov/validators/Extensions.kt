package com.egorblagochinnov.validators

import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.egorblagochinnov.validators.viewbinders.LiveDataValidatorViewBinder
import com.egorblagochinnov.validators.viewbinders.TextConditionViewBinder
import com.egorblagochinnov.validators.viewbinders.TextViewLiveDataValidatorBinder
import java.lang.ref.WeakReference

fun TextView.textChanges(): LiveData<String?> {
    val result = MutableLiveData<String?>()

    this.doAfterTextChanged {
        result.value = it?.toString()
    }

    return result
}

/**
 * Подключает валидатор [TextViewLiveDataValidatorBinder] к [TextView]
 * Подписывается на валидатор (активирует его)
 * Следит за фокусом [TextView]
 *
 * @param lifecycleOwner - [LifecycleOwner] Нужен для подписки на валидатор [LiveDataValidator]
 * @param validator - Валидатор, по которому проверяется поле
 *
 * @return готовый [TextViewLiveDataValidatorBinder]
 * **/
fun <D> TextView.validateBy(
        lifecycleOwner: LifecycleOwner,
        validator: LiveDataValidator<D?>
): TextViewLiveDataValidatorBinder<D> {
    val viewValidator = TextViewLiveDataValidatorBinder(WeakReference(this), validator)
    viewValidator.attach(lifecycleOwner)
    viewValidator.setAsFocusListener()
    return viewValidator
}

/**
 * Создаёт условие проверки и подключает его к [TextView]
 *
 * @return готовый [TextConditionViewBinder]
 * **/
fun TextView.validateBy(
    errorMessage: String,
    isValueValid: (value: CharSequence?) -> Boolean
): TextConditionViewBinder<TextView> {
    return validateBy(Condition.create(errorMessage, isValueValid))
}

/**
 * Подключает условие валидности к [TextView]
 *
 * @return готовый [TextConditionViewBinder]
 * **/
fun TextView.validateBy(
    condition: Condition<CharSequence?>
): TextConditionViewBinder<TextView> {
    val viewValidator = TextConditionViewBinder(WeakReference(this), condition)
    viewValidator.activate()
    return viewValidator
}

fun <V : View, D> V.validateBy(
    validator: LiveDataValidator<D?>,
    onValidationResult: (view: V?, result: ValidationResult?) -> Unit
): LiveDataValidatorViewBinder<V, D> {
    return LiveDataValidatorViewBinder.create(this, validator, onValidationResult)
}