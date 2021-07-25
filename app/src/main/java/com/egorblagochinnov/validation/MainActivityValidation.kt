package com.egorblagochinnov.validation

import androidx.lifecycle.LiveData
import com.egorblagochinnov.validators.*

class MainActivityValidation(private val source: LiveData<String?>) {
    private val lengthCondition = Condition.create<String?>("length > 10") {
        (it?.length ?: 0) <= 10
    }

    private val validator2Condition = Condition<String?> {
        if (it?.contains("2") == true) {
            ValidationResult.invalid("2 - restricted")
        } else {
            ValidationResult.valid()
        }
    }

    val validator = LiveDataValidator<String?>(source).apply {

    }

    val validator2 = LiveDataValidator<String?>(source).apply {
        addCondition(validator2Condition)

        watchOn<ValidationResult>(validator.state) {
            if (it.isValid) {
                addCondition(validator2Condition)
            } else {
                removeCondition(validator2Condition)
            }
        }
    }

    val validator3 = LiveDataValidator<String?>(source).apply {
        addCondition {
            if (it?.contains("3") == true) {
                ValidationResult.invalid("3 - restricted")
            } else {
                ValidationResult.valid()
            }
        }
    }

    val validator4 = LiveDataValidator<String?>(source).apply {
        addCondition {
            if (it?.contains("4") == true) {
                ValidationResult.invalid("4 - restricted")
            } else {
                ValidationResult.valid()
            }
        }

        addCondition(Conditions.RegEx("[0-9]".toRegex(), null))
    }


    val mux = MuxLiveDataValidator(
        validator,
        validator2,
        validator3
    )
}