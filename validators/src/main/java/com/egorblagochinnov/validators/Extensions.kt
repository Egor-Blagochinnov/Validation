package com.egorblagochinnov.validators

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import com.egorblagochinnov.validators.viewbinders.LiveDataValidatorViewBinder
import com.egorblagochinnov.validators.viewbinders.TextConditionViewBinder
import com.egorblagochinnov.validators.viewbinders.TextViewLiveDataValidatorBinder
import java.lang.ref.WeakReference

/**
 * Bind the validator [TextViewLiveDataValidatorBinder] to [TextView]
 * Subscribes to the validator (activates it)
 * Keeps track of focus [TextView]
 *
 * @param lifecycleOwner - [LifecycleOwner] Needed to subscribe to the [LiveDataValidator]
 * @param validator - The validator by which the field is validated
 *
 * @return prepared [TextViewLiveDataValidatorBinder]
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
 * Creates a test condition and connects it to [TextView]
 *
 * @return ready [TextConditionViewBinder]
 * **/
fun TextView.validateBy(
    errorMessage: String,
    isValueValid: (value: CharSequence?) -> Boolean
): TextConditionViewBinder<TextView> {
    return validateBy(Condition.create(errorMessage, isValueValid))
}

/**
 * Bind a [Condition] to [TextView]
 *
 * @return prepared [TextConditionViewBinder]
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