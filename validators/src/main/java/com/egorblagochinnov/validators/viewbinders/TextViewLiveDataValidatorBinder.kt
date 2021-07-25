package com.egorblagochinnov.validators.viewbinders

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.egorblagochinnov.validators.*
import com.google.android.material.textfield.TextInputLayout
import java.lang.ref.WeakReference

/**
 * Validator listener for text fields
 * In this case, the validator can validate data of any type (double, for example)
 *
 * @param viewRef - Weak reference to TextView
 * @param validator - The [validator] against which the view is validated
 * **/
class TextViewLiveDataValidatorBinder<D>(
    viewRef: WeakReference<TextView>,
    validator: LiveDataValidator<D?>
) : LiveDataValidatorViewBinder<TextView, D>(viewRef, validator), View.OnFocusChangeListener {

    /**
     * The [TextInputLayout] container that contains the [EditText] (or TextView)
     * **/
    private val viewParent: TextInputLayout? by lazy { view?.getParentInputLayout() }

    /**
     * Saved tooltip text.
     * It is necessary so that after the error is displayed, it is possible to return to the place of the helper text.
     * **/
    private var helperText: CharSequence? = null

    private var maxLength: Int? = null

    /**
     * Interaction with the field.
     * Set to true the first time the field is focused.
     * Needed in order to hide errors until the user click on the field
     * **/
    private var isInteracted: Boolean = false

    private var defaultSuccessColorRes: Int = R.color.state_success
    private var defaultErrorColorRes: Int = R.color.design_default_color_error

    var successIcon: Drawable? = view?.context?.let {
        ContextCompat.getDrawable(it, R.drawable.ic_baseline_check_circle_24)
    }

    var errorIcon: Drawable? = view?.context?.let {
        ContextCompat.getDrawable(it, R.drawable.ic_baseline_error_24)
    }

    init {
        checkForMaxLengthValidator()
        setSuccessColorRes(defaultSuccessColorRes)
        setErrorColorRes(defaultErrorColorRes)

        if (view?.hasFocus() == true) {
            isInteracted = true
            onFocusSet()
        } else {
            hideHelperText()
            checkMaxLength()
        }
    }

    fun setSuccessIcon(@DrawableRes icon: Int) {
        view?.context?.let {
            val drawable = ContextCompat.getDrawable(it, icon)
            successIcon = drawable
            checkViewAppearance()
        }

    }

    fun setErrorIcon(@DrawableRes icon: Int) {
        view?.context?.let {
            val drawable = ContextCompat.getDrawable(it, icon)
            errorIcon = drawable
            checkViewAppearance()
        }

    }

    fun setSuccessColorRes(@ColorRes color: Int) {
        view?.context?.let {
            setSuccessColor(ContextCompat.getColorStateList(it, color))
        }
    }

    fun setSuccessColor(@ColorInt color: Int) {
        setSuccessColor(ColorStateList.valueOf(color))
    }

    fun setSuccessColor(color: ColorStateList?) {
        val icon = successIcon ?: return
        DrawableCompat.setTintList(icon, color)
    }

    fun setErrorColorRes(@ColorRes color: Int) {
        view?.context?.let {
            setErrorColor(ContextCompat.getColorStateList(it, color))
        }
    }

    fun setErrorColor(@ColorInt color: Int) {
        setErrorColor(ColorStateList.valueOf(color))
    }

    fun setErrorColor(color: ColorStateList?) {
        val icon = errorIcon ?: return
        DrawableCompat.setTintList(icon, color)
    }

    /**
     * Listens for focus changes [view]
     * @see onFocusChange
     * **/
    fun setAsFocusListener() {
        view?.onFocusChangeListener = this
    }

    /**
     * Forced check
     * Sets the [isInteracted] flag to true,
     * so as not to hide errors regardless of whether the user interacted with the field or not
     * **/
    fun forceCheck() {
        isInteracted = true
        validator.validate()
    }

    override fun onConditionsChanged(conditions: Set<Condition<D?>>) {
        checkForMaxLengthValidator()
    }

    override fun onOperatorChanged() {
        // Do nothing
    }

    /**
     * Validator triggered
     *
     * @see checkViewAppearance
     * **/
    override fun onValidationResult(view: TextView?, result: ValidationResult?) {
        checkViewAppearance(view, result)
    }

    private fun checkViewAppearance() {
        checkViewAppearance(view, validator.state.value)
    }

    /**
     * If the validator returned an error (false) but the user did not interact with the field ([isInteracted] = false) - ignore the error
     * **/
    private fun checkViewAppearance(view: TextView?, result: ValidationResult?) {
        if (result?.isValid == false && !isInteracted) {
            return
        }

        when (result?.isValid) {
            true -> {
                setViewAsValid(view)
            }
            false -> {
                setViewAsInvalid(view, result.errorMessage)
            }
            else -> {
                setViewAsDefault(view)
            }
        }
    }

    /**
     * The text is valid
     * Remove the error
     * Draw a check mark [successIcon]
     * **/
    private fun setViewAsValid(view: TextView?) {
        view?.removeError()
        view?.showSuccessIcon(successIcon)
    }

    /**
     * The text is invalid
     * Remove the check mark
     * Draw the error [errorIcon]
     * **/
    private fun setViewAsInvalid(view: TextView?, errorText: CharSequence?) {
        view?.removeSuccessIcon()
        view?.showError(
            errorText ?: "Error",
            errorIcon
        )
    }

    /**
     * Default view. Or if the validator returned null
     * Remove the error
     * Remove the check mark
     * **/
    private fun setViewAsDefault(view: TextView?) {
        view?.removeError()
        view?.removeSuccessIcon()
    }

    /**
     * When focus changes
     *
     * If focus is set:
     * - [isInteracted] = true
     * - If helperText was set on the field - shows this helperText
     * - Turns on counter for maximum text length if maxLength! = Null
     *
     * If focus is removed:
     * - Hide the hint text
     * - Checking the maximum field length
     * - Check gender for all validators
     * **/
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) {
            isInteracted = true
            onFocusSet()
        } else {
            hideHelperText()
            checkMaxLength()
            check()
        }
    }

    /**
     * Finds the maximum length validator and sets the maximum length for the character counter based on it
     * **/
    private fun checkForMaxLengthValidator() {
        getMaxLength()?.let {
            maxLength = it

            if (view?.hasFocus() == true) {
                viewParent?.counterMaxLength = it
                viewParent?.isCounterEnabled = true
            } else {
                viewParent?.isCounterEnabled = false
            }
        }
    }

    /**
     * Finds the maximum length for a field
     * Looks for a condition of type [Conditions.TextMaxLength] and returns its maxLength
     * **/
    private fun getMaxLength(): Int? {
        val maxLengthCondition = validator
            .getConditionsSet()
            .find { it is Conditions.TextMaxLength } as? Conditions.TextMaxLength

        return maxLengthCondition?.maxLength
    }

    private fun onFocusSet() {
        if (!this.helperText.isNullOrBlank()) {
            viewParent?.helperText = this.helperText
        }

        maxLength?.let {
            viewParent?.isCounterEnabled = true
            viewParent?.counterMaxLength = it
        }
    }

    /**
     * Hides the tooltip text
     * Saves the hint text because isHelperTextEnabled = false removes helperText from the field and then helperText will not be returned.
     * Therefore, the [helperText] value is saved so that you can put it back later
     * **/
    private fun hideHelperText() {
        val viewParent = view?.getParentInputLayout()

        this.helperText = viewParent?.helperText
        viewParent?.helperText = null
    }

    private fun checkMaxLength() {
        maxLength?.let {
            val textLength = view?.text?.length ?: 0
            viewParent?.isCounterEnabled = textLength > it
        }
    }
}


