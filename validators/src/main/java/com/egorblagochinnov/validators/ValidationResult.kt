package com.egorblagochinnov.validators

/**
 * Результат проверки
 * **/
open class ValidationResult(
        /**
         * Првильно или неправильно
         * **/
        val isValid: Boolean,

        /**
         * Сообщение об ошибке
         * **/
        val errorMessage: String? = null,
) {
    /**
     * Сложение результатов валидации
     * Аналог булевого ИЛИ
     *
     * Например:
     * ValidationResult(true) + ValidationResult(false, "ашипка") = ValidationResult(true)
     * **/
    operator fun plus(increment: ValidationResult): ValidationResult {
        val isValid = this.isValid || increment.isValid

        val errorMessage = when {
            !this.isValid && !this.errorMessage.isNullOrBlank() -> this.errorMessage
            !increment.isValid && !increment.errorMessage.isNullOrBlank() -> increment.errorMessage
            else -> null
        }

        return obtain(isValid, errorMessage)
    }

    /**
     * Умножение результатов валидации
     * Аналог булевого И
     *
     * Например:
     * ValidationResult(true) * ValidationResult(false, "ашипка") = ValidationResult(false, "ашипка")
     * **/
    operator fun times(increment: ValidationResult): ValidationResult {
        val isValid = this.isValid && increment.isValid

        val errorMessage = when {
            !this.isValid && !this.errorMessage.isNullOrBlank() -> this.errorMessage
            !increment.isValid && !increment.errorMessage.isNullOrBlank() -> increment.errorMessage
            else -> null
        }

        return obtain(isValid, errorMessage)
    }

    companion object {
        /**
         * Создание ValidationResult
         *
         * @param isValid - результат проверки
         * @param errorMessage - сообщение для ошибки. Передается в результат, только если isValid false
         *
         * @return ValidationResult с параметром isValid == true (без errorMessage) Если @param isValid true
         * @return ValidationResult с параметром isValid == false (вместе с errorMessage) Если @param isValid false
         * **/
        fun obtain(isValid: Boolean, errorMessage: String?): ValidationResult {
            return if (isValid) {
                valid()
            } else {
                invalid(errorMessage)
            }
        }

        fun valid() = ValidationResult(true)

        fun invalid(errorMessage: String?) = ValidationResult(false, errorMessage)
    }
}