package com.egorblagochinnov.validators

import android.content.Context
import androidx.annotation.StringRes

/**
 * Template classes for creating conditions ([Condition])
 * */
class Conditions {
    /**
     * Checks that a value is not null
     * true - not null
     * false - null
     *
     * @param errorText - Error message when value is null
     * **/
    class NotNull<T>(private val errorText: String? = "Value should not be null") : Condition<T?> {
        constructor(context: Context, @StringRes errorText: Int) : this(context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            return if (value != null) {
                ValidationResult.create(true, null)
            } else {
                ValidationResult.create(false, errorText)
            }
        }
    }

    /**
     * Required text [CharSequence] field
     * true - The field is not empty, not null and does not consist of only spaces
     * false - The field is empty, null, or contains only spaces
     * **/
    class RequiredField<T : CharSequence>(private val errorText: String? = "Required field") : Condition<T?> {
        constructor(
            context: Context,
            @StringRes errorText: Int = R.string.validators_required_field
        ) : this(context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            return if (!value.isNullOrBlank()) {
                ValidationResult.create(true)
            } else {
                ValidationResult.create(false, errorText)
            }
        }
    }

    class TextMaxLength<T : CharSequence?>(
        val maxLength: Int,
        private val errorText: String? = "Exceeded maximum text length: $maxLength"
    ) : Condition<T?> {
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
                ValidationResult.create(false, errorText)
            }
        }
    }

    class TextMinLength<T : CharSequence>(
        val minLength: Int,
        private val errorText: String? = "Not reached to minimum text length: $minLength"
    ) : Condition<T?> {

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
                ValidationResult.create(false, errorText)
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
            context.getString(
                R.string.validators_text_length_should_be_in_range,
                textLengthRange.first,
                textLengthRange.last
            )
        )

        constructor(
            textLengthRange: IntRange,
            context: Context,
            @StringRes errorText: Int
        ) : this(textLengthRange, context.getString(errorText))

        override fun validate(value: T?): ValidationResult {
            val textLength = (value?.length ?: 0)

            return if (textLengthRange.contains(textLength)) {
                ValidationResult.create(true)
            } else {
                ValidationResult.create(false, errorText)
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
                ValidationResult.create(true)
            } else {
                ValidationResult.create(false, errorText)
            }
        }
    }

    class RegEx<T : CharSequence?>(
        private val regEx: Regex,
        private val errorText: String? = "Text does not match given RegEx"
    ) : Condition<T?> {
        override fun validate(value: T?): ValidationResult {
            val text = value ?: ""
            return if (text.matches(regEx)) {
                ValidationResult.create(true)
            } else {
                ValidationResult.create(false, errorText)
            }
        }
    }
}