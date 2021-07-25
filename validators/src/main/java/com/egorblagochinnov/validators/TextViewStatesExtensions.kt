package com.egorblagochinnov.validators

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.textfield.TextInputLayout

/**
 * Устанавливает ошибку на текстовое поле
 * Удаляет drawableEnd иконку
 *
 * Если TextView находится в TextInputLayout контейнере, то ошибка будет показана через этот самый TextInputLayout (ошибка будет под текстовым полем)
 * Если TextView не находится в TextInputLayout контейнере, то ошибка будет показана через сам TextView (ошибка будет в всплывающем окне, привязанном к TextView)
 *
 * @param errorMessage - текст ошибки, который надо показать
 * **/
fun TextView.showError(errorMessage: CharSequence, errorIcon: Drawable? = null) {
    val textInputLayout = this.getParentInputLayout() /* Пытаемся найти TextInputLayout для editText */

    if (textInputLayout != null) { /* TextInputLayout есть. Показываем ошибку в нём */
        textInputLayout.error = errorMessage
        textInputLayout.isErrorEnabled = true
        errorIcon?.let { textInputLayout.errorIconDrawable = it }
    } else { /* TextInputLayout нет. Показываем прямо в TextView */

        /* Проверяем параметры isFocusable и isFocusableInTouchMode. Поле должно быть доступно для фокусировки, иначе текст ошибки не появится */
        if (this.isFocusable == false) {
            this.isFocusable = true
        }

        if (this.isFocusableInTouchMode == false) {
            this.isFocusableInTouchMode = true
        }

        /* Устанавливаем ошибку на поле */
        if (errorIcon != null) {
            this.setError(errorMessage, errorIcon)
        } else {
            this.error = errorMessage
        }

        this.requestFocus() /* Запрашиваем фокус на поле, чтобы при установке ошибки появилось всплывающее окно */
    }
}

/**
 * Убирает ошибку из текстового поля
 *
 * Если TextView находится в TextInputLayout контейнере, то ошибка удирается у TextInputLayout
 * Если TextView не находится в TextInputLayout контейнере, то ошибка убирается у самого TextView
 *
 * **/
fun TextView.removeError() {
    val textInputLayout = this.getParentInputLayout() /* Пытаемся найти TextInputLayout для editText */

    if (textInputLayout != null) { /* TextInputLayout есть. Убираем из него ошибку */
        textInputLayout.error = null
    } else { /* TextInputLayout нет. Убираем ошибку из TextView */
        this.error = null
    }
}

/** Показывает, что поле корректно. Убирает ошибки и ставит зеленую галочку справа (drawableEnd) **/
fun TextView.showSuccessIcon(
    icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_baseline_check_circle_24),
    @ColorInt iconColor: Int? = ContextCompat.getColor(context, R.color.state_success),
    iconAlpha: Int? = 128
) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null)

    if (iconColor != null) {
        TextViewCompat.setCompoundDrawableTintList(
            this,
            ColorStateList.valueOf(iconColor)
        )
    }

    icon?.alpha = iconAlpha ?: 128
}

/** Показывает, что поле корректно. Убирает ошибки и ставит зеленую галочку справа (drawableEnd) **/
fun TextView.removeSuccessIcon() {
    setCompoundDrawablesRelativeWithIntrinsicBounds(
        compoundDrawablesRelative.getOrNull(0),
        compoundDrawablesRelative.getOrNull(1),
        null,
        compoundDrawablesRelative.getOrNull(3)
    )
}

fun TextView.getParentInputLayout(): TextInputLayout? {
    var parent = this.parent
    while (parent is View) {
        if (parent is TextInputLayout) {
            return parent
        }
        parent = parent.getParent()
    }
    return null
}