package com.egorblagochinnov.validators.viewbinders

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.egorblagochinnov.validators.*
import java.lang.ref.WeakReference

/**
 * [ConditionViewBinder] for text validation in the TextView (and its descendants)
 * After each change of the text in the text field, checks the text against the validator
 * Depending on the result of validation - sets an error on the field
 *
 * @param textViewRef - Weak reference to the TextView to be validated
 * @param validator - Validator that validates the text
 * **/
class TextConditionViewBinder<V : TextView>(
    textViewRef: WeakReference<V>,
    private val validator: Condition<CharSequence?>
) : ConditionViewBinder<V, CharSequence?>(textViewRef, validator), TextWatcher {
    private val textView = textViewRef.get()

    var successIcon: Drawable? = textView?.context?.let {
        ContextCompat.getDrawable(it, R.drawable.ic_baseline_check_circle_24)
    }

    var successColor: Int? = textView?.context?.let {
        ContextCompat.getColor(it, R.color.state_success)
    }

    var errorIcon: Drawable? = textView?.context?.let {
        ContextCompat.getDrawable(it, R.drawable.ic_baseline_error_24)
    }

    var errorColor: Int? = textView?.context?.let {
        ContextCompat.getColor(it, R.color.design_default_color_error)
    }

    fun activate() {
        textView?.addTextChangedListener(this)
    }

    fun deactivate() {
        textView?.removeTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) {
        check(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun getValidationData(view: V?): CharSequence? {
        return view?.text
    }

    override fun onValidationResult(view: V?, result: ValidationResult?) {
        if (result?.isValid == true) {
            view?.removeError()
            view?.showSuccessIcon()
            return
        } else {
            view?.showError(result?.errorMessage ?: "Error")
        }
    }
}