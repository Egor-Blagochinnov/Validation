package com.egorblagochinnov.validators

import android.content.Context
import androidx.annotation.StringRes

fun interface Condition<T> {
    fun validate(value: T?): ValidationResult

    /**
     * Addition of conditions
     * Boolean OR analog
     *
     * @see ValidationResult.plus
     * **/
    operator fun plus(condition: Condition<T>): Condition<T> {
        return Condition { value ->
            this.validate(value) + condition.validate(value)
        }
    }

    /**
     * Multiplication of validation results
     * Boolean AND analogue
     *
     * @see ValidationResult.times
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

        inline fun <V> create(
            errorMessage: String? = null,
            crossinline isValueValid: (value: V?) -> Boolean
        ): Condition<V?> = Condition<V?> { value ->
            if (isValueValid(value)) {
                ValidationResult.valid()
            } else {
                ValidationResult.invalid(errorMessage)
            }
        }

        fun <T> Condition<T>.isValid(value: T): Boolean {
            return this.validate(value).isValid
        }
    }
}