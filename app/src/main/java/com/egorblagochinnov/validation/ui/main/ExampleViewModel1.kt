package com.egorblagochinnov.validation.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.egorblagochinnov.validators.Condition
import com.egorblagochinnov.validators.Conditions
import com.egorblagochinnov.validators.LiveDataValidator
import com.egorblagochinnov.validators.MuxLiveDataValidator

class ExampleViewModel1 : ViewModel() {
    val textMaxLength: MutableLiveData<String?> = MutableLiveData<String?>()

    val textField1: MutableLiveData<String?> = MutableLiveData<String?>()
    val textField1Validator = ExampleValidators.DigitsFieldValidator(textField1).apply {
        watchOn(textMaxLength) {
            val maxLength = kotlin.runCatching { it?.toInt() }.getOrNull()
            setMaxLength(maxLength)
        }
    }

    val textField2: MutableLiveData<String?> = MutableLiveData<String?>()
    val textField2Validator: LiveDataValidator<String?> = LiveDataValidator(textField2).apply {
        addCondition(Conditions.RequiredField())
        addCondition(Conditions.RegEx("[a-z]+".toRegex(), "only a-z symbols allowed"))
        addCondition(Conditions.TextMaxLength(10))
    }

    val muxValidator = MuxLiveDataValidator(
        textField1Validator,
        textField2Validator
    )

    class ExampleValidators {
        class DigitsFieldValidator<S : CharSequence?>(
            source: LiveData<S>,
            initialCondition: Condition<S?>? = null,
            operator: Operator = Operator.Conjunction()
        ) : LiveDataValidator<S>(
            source,
            initialCondition,
            operator
        ) {
            val onlyDigitsCondition = Conditions.RegEx<S>("[0-9]+".toRegex(), "only digits allowed")
            private var maxLengthCondition = Conditions.TextMaxLength<S?>(5)

            init {
                addCondition(onlyDigitsCondition)
                addCondition(maxLengthCondition)
            }

            fun setMaxLength(maxLength: Int?) {
                if (maxLength == null || maxLength < 0) {
                    removeCondition(maxLengthCondition)
                    return
                }

                val newCondition = Conditions.TextMaxLength<S?>(maxLength)

                changeConditionsSet {
                    remove(maxLengthCondition)
                    maxLengthCondition = newCondition
                    add(maxLengthCondition)
                }
            }
        }
    }
}