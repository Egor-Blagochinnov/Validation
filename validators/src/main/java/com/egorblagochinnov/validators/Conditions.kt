package com.egorblagochinnov.validators

import android.content.Context
import androidx.annotation.StringRes

/**
 * Шаблонные классы для создания условий ([Condition])
 * */
class Conditions {
    /**
     * Проверяет, что значение не null
     * true - не null
     * false - null
     *
     * @param description - Описание условия
     * **/
    class NotNull<T>(private val errorText: String? = "Value should not be null") : Condition<T?> {
        constructor(context: Context, @StringRes errorText: Int): this(context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            return if (value != null) {
                ValidationResult(true, null)
            } else {
                ValidationResult(false, errorText)
            }
        }
    }

    /**
     * Необходимое для заполнения текстовое поле
     * true - Поле не пустое, не null и не состоит из одних пробелов
     * false - Поле пустое, null или содержит только пробелы
     * **/
    class RequiredField<T : CharSequence>(private val errorText: String? = "Required field") : Condition<T?> {
        constructor(context: Context, @StringRes errorText: Int = R.string.validators_required_field): this(context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            return if (!value.isNullOrBlank()) {
                ValidationResult(true)
            } else {
                ValidationResult(false, errorText)
            }
        }
    }

    class TextMaxLength<T : CharSequence>(
        val maxLength: Int,
        private val errorText: String? = "Exceeded maximum text length: $maxLength") : Condition<T?> {

        constructor(
            maxLength: Int,
            context: Context
        ) : this(
            maxLength,
            context.getString(R.string.validators_max_text_length_exceeded, maxLength)
        )

        constructor(
            maxLength: Int,
            context: Context,
            @StringRes errorText: Int
        ) : this(maxLength, context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            return if ((value?.length ?: 0) <= maxLength) {
                ValidationResult.valid()
            } else {
                ValidationResult(false, errorText)
            }
        }
    }

    class TextMinLength<T : CharSequence>(
        val minLength: Int,
        private val errorText: String? = "Not reached to minimum text length: $minLength") : Condition<T?> {

        constructor(
            minLength: Int,
            context: Context
        ) : this(
            minLength,
            context.getString(R.string.validators_min_text_length_not_reached, minLength)
        )

        constructor(
            maxLength: Int,
            context: Context,
            @StringRes errorText: Int
        ) : this(maxLength, context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            return if ((value?.length ?: 0) >= minLength) {
                ValidationResult.valid()
            } else {
                ValidationResult(false, errorText)
            }
        }
    }

    class TextLengthRange<T : CharSequence>(
        val textLengthRange: IntRange,
        private val errorText: String? = "Text length should be in range: ${textLengthRange.first} - ${textLengthRange.last}"
    ) : Condition<T?> {

        constructor(
            textLengthRange: IntRange,
            context: Context
        ) : this(
            textLengthRange,
            context.getString(R.string.validators_text_length_should_be_in_range, textLengthRange.first, textLengthRange.last)
        )

        constructor(
            textLengthRange: IntRange,
            context: Context,
            @StringRes errorText: Int
        ) : this(textLengthRange, context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            val textLength = (value?.length ?: 0)

            return if (textLengthRange.contains(textLength)) {
                ValidationResult(true)
            } else {
                ValidationResult(false, errorText)
            }
        }
    }

    class TextLength<T : CharSequence>(
        val textLength: Int,
        private val errorText: String? = "Text length must be $textLength"
    ) : Condition<T?> {

        constructor(
            textLength: Int,
            context: Context
        ) : this(
            textLength,
            context.getString(R.string.validators_text_length_must_be, textLength)
        )

        constructor(
            textLength: Int,
            context: Context,
            @StringRes errorText: Int
        ) : this(textLength, context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            return if (value?.length ?: 0 == textLength) {
                ValidationResult(true)
            } else {
                ValidationResult(false, errorText)
            }
        }
    }
}