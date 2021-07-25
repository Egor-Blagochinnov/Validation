package com.egorblagochinnov.validators

interface ValidationResult {
    val isValid: Boolean
    val errorMessage: String?

    /**
     * Addition of conditions
     * Boolean OR analog
     *
     * Example:
     * ValidationResult(true) + ValidationResult(false, "error") = ValidationResult(true)
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
     * Multiplication of validation results
     * Boolean AND analogue
     *
     * Example:
     * ValidationResult(true) * ValidationResult(false, "error") = ValidationResult(false, "error")
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
         * Creation of ValidationResult
         *
         * @param isValid - Result of validation
         * @param errorMessage - Error message. Passed into the returning [ValidationResult] only if [isValid] is false
         *
         * @return ValidationResult where [ValidationResult.isValid] == true and [ValidationResult.errorMessage] == null if [isValid] == true
         * @return ValidationResult where [ValidationResult.isValid] == false and [ValidationResult.errorMessage] == [errorMessage] if [isValid] == false
         * **/
        fun obtain(isValid: Boolean, errorMessage: String?): ValidationResult {
            return if (isValid) {
                valid()
            } else {
                invalid(errorMessage)
            }
        }

        /**
         * Creation of ValidationResult
         *
         * @param isValid - Result of validation
         * @param errorMessage - Error message
         *
         * @return [ValidationResult] where [ValidationResult.isValid] == [isValid] and [ValidationResult.errorMessage] == [errorMessage]
         * **/
        fun create(isValid: Boolean, errorMessage: String? = null): ValidationResult {
            return object : ValidationResult {
                override val isValid: Boolean
                    get() = isValid
                override val errorMessage: String?
                    get() = errorMessage
            }
        }

        /**
         * Creates valid [ValidationResult]
         *
         * @return [ValidationResult] where [ValidationResult.isValid] == true and [ValidationResult.errorMessage] == null
         * **/
        fun valid(): ValidationResult {
            return object : ValidationResult {
                override val isValid: Boolean
                    get() = true
                override val errorMessage: String?
                    get() = null
            }
        }

        /**
         * Creates invalid [ValidationResult]
         *
         * @return [ValidationResult] where [ValidationResult.isValid] == false and [ValidationResult.errorMessage] == [errorMessage]
         * **/
        fun invalid(errorMessage: String?): ValidationResult {
            return object : ValidationResult {
                override val isValid: Boolean
                    get() = false
                override val errorMessage: String?
                    get() = errorMessage
            }
        }
    }
}