package com.egorblagochinnov.validators

import android.content.Context
import androidx.annotation.StringRes

/**
 * Условие проверки значения <T>
 * **/
fun interface Condition<T> {
    /**
     * Метод проверки значения
     *
     * @param value значение, которое нужно проверить
     * @return ValidationResult - результат проверки
     * **/
    fun validate(value: T?): ValidationResult

    /**
     * Сложение условий
     * Аналог булевого ИЛИ
     *
     * Например:
     * Condition { длина строки == 5 } + Condition { длина строки == 10 } = Condition { длина строки == 5 ИЛИ 10 }
     * **/
    operator fun plus(condition: Condition<T>): Condition<T> {
        return Condition { value ->
            this.validate(value) + condition.validate(value)
        }
    }

    /**
     * Умножение условий
     * Аналог булевого И
     *
     * Например:
     * Condition { строка должна содержать символ "а" } * Condition { строка должна содержать символ "b" } = Condition { строка должна содержать символы "а" и "b" }
     * **/
    operator fun times(condition: Condition<T>): Condition<T> {
        return Condition { value ->
            this.validate(value) * condition.validate(value)
        }
    }

    companion object {
        inline fun <V> create(
            context: Context,
            @StringRes description: Int,
            crossinline isValueValid: (value: V?) -> Boolean
        ): Condition<V?> = create(context.getString(description), isValueValid)

        /**
         * Создаёт экземпляр условия по переданным параметрам
         *
         * @param errorMessage - Описание ошибки
         * @param isValueValid - Функция проверки условия
         * **/
        inline fun <V> create(
                errorMessage: String? = null,
                crossinline isValueValid: (value: V?) -> Boolean
        ): Condition<V?> = Condition<V?> { value ->
            if (isValueValid(value)) {
                ValidationResult(true)
            } else {
                ValidationResult(false, errorMessage)
            }
        }
    }
}