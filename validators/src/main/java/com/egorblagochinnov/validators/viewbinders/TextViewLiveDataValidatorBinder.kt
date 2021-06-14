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
 * Слушатель валидатора для текстовых полей
 * При этом валидатор может валидировать данные любого типа (double, например)
 *
 * @param viewRef слабая ссылка на TextView
 * @param validator валидатор, по которому проверяется view
 * **/
class TextViewLiveDataValidatorBinder<D>(
    viewRef: WeakReference<TextView>,
    validator: LiveDataValidator<D?>
) : LiveDataValidatorViewBinder<TextView, D>(viewRef, validator), View.OnFocusChangeListener {

    /**
     * Контейнер TextInputLayout в котором находится EditText (или TextView)
     * **/
    private val viewParent: TextInputLayout? by lazy { view?.getParentInputLayout() }

    /**
     * Сохраненный текст подсказки. Нужен для того чтобы после отображения ошибки можно было вернуть на место такст подсказки
     * **/
    private var helperText: CharSequence? = null

    /**
     * Максимальныя длина
     * **/
    private var maxLength: Int? = null

    /**
     * Взаимодействие с полем. Устанавливается в true при первой устновке фокуса на поле.
     * Нужен для того чтобы скрывать ошибки, пока пользователь не тыкнет на поле
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
     * Слушает изменения фокуса [view]
     * @see onFocusChange
     * **/
    fun setAsFocusListener() {
        view?.onFocusChangeListener = this
    }

    /**
     * Принудительная проверка
     * Переводит флаг isInteracted в true,
     * чтобы не скрывать ошибки независимо от того, взаимодействовал пользователь с полем или нет
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
     * Сработал валидатор
     * Если валидатор вернул ошибку (false) но пользователь не взаимодействовал с полем (isInteracted = false) - игнорируем ошибку
     * **/
    override fun onValidationResult(view: TextView?, result: ValidationResult?) {
        checkViewAppearance(view, result)
    }

    private fun checkViewAppearance() {
        checkViewAppearance(view, validator.state.value)
    }

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
     * Текст валидный
     * Удаляем ошибку
     * Рисуем галочку
     * **/
    private fun setViewAsValid(view: TextView?) {
        view?.removeError()
        view?.showSuccessIcon(successIcon)
    }

    /**
     * Текст невалидный
     * Удаляем галочку
     * Рисуем ошибку
     * **/
    private fun setViewAsInvalid(view: TextView?, errorText: CharSequence?) {
        view?.removeSuccessIcon()
        view?.showError(
            errorText ?: "Error",
            errorIcon
        )
    }

    /**
     * Вид по - умолчанию. Либо если валидатор вернул null
     * Удаляем ошибку
     * Удаляем галочку
     * **/
    private fun setViewAsDefault(view: TextView?) {
        view?.removeError()
        view?.removeSuccessIcon()
    }

    /**
     * При изменении фокуса
     *
     * Если установлен фокус:
     *   isInteracted = true
     *   Если на поле был установлен helperText - показывает этот helperText
     *   Включает счетчик максимальной длины текста если maxLength != null
     *
     * Если фокус снят:
     *   Скрываем текст подсказки
     *   Проверяем максимальную длину поля
     *   Проверяе полу по всем валидаторам
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
     * Находит валидатор максимальной длины и выставляет по нему максимальную длину для счетчика символов
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
     * Находит максимальную длину для поля
     * Ищет условие типа Condition.TextMaxLength и возвращает его maxLength
     * **/
    private fun getMaxLength(): Int? {
        val maxLengthCondition = validator
            .getConditionsSet()
            .find { it is Conditions.TextMaxLength } as? Conditions.TextMaxLength

        return maxLengthCondition?.maxLength
    }

    /**
     * Установлен фокус
     *
     * Если на поле был установлен helperText - показывает этот helperText
     * Включает счетчик максимальной длины текста если maxLength != null
     * **/
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
     * Скрывает текст подсказки
     * Сохраняет текст подсказки потому что isHelperTextEnabled = false удаляет helperText из поля и потом helperText не вернуть.
     * Поэтому значение helperText сохраняется, чтобы потом поставить его обратно
     * **/
    private fun hideHelperText() {
        val viewParent = view?.getParentInputLayout()

        this.helperText = viewParent?.helperText
        viewParent?.helperText = null
    }

    /**
     * Проверка по счетчику максимальной длины
     * **/
    private fun checkMaxLength() {
        maxLength?.let {
            val textLength = view?.text?.length ?: 0
            viewParent?.isCounterEnabled = textLength > it
        }
    }
}


