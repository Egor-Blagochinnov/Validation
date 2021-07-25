package com.egorblagochinnov.validators

/**
 * Validate the value of <T> against multiple conditions
 *
 * When [Validator] is triggered ([validate])
 * value [T] checking against all conditions ([conditions]) then
 * all results of validation are passed to operator ([Operator]) which decides the final result ([ValidationResult])
 *
 * @param initialCondition - Initial condition
 * @param operator - An operator that defines the result of the validation.
 *
 * @see Condition
 * @see Operator
 * **/
open class Validator<T>(
    initialCondition: Condition<T?>? = null,
    private var operator: Operator = Operator.Conjunction()
) : Condition<T?> {
    private val conditions: MutableSet<Condition<T?>> by lazy {
        LinkedHashSet<Condition<T?>>()
    }

    private val onOperatorChangedListeners: ArrayList<OnOperatorChangedListener> by lazy { ArrayList() }
    private val onConditionsChangedListeners: ArrayList<OnConditionsChangedListener<T>> by lazy { ArrayList() }

    init {
        initialCondition?.let {
            addCondition(it)
        }
    }

    fun setOperator(operator: Operator) {
        this.operator = operator
        dispatchOnOperatorChanged()
    }

    fun getOperator(): Operator {
        return operator
    }

    private fun dispatchOnOperatorChanged() {
        onOperatorChangedListeners.forEach {
            it.onOperatorChanged()
        }
    }

    fun addOperatorChangedListener(listener: OnOperatorChangedListener) {
        onOperatorChangedListeners.add(listener)
    }

    fun removeOperatorChangedListener(listener: OnOperatorChangedListener) {
        onOperatorChangedListeners.remove(listener)
    }

    fun addConditionsChangedListener(listener: OnConditionsChangedListener<T>) {
        onConditionsChangedListeners.add(listener)
    }

    fun removeConditionsChangedListener(listener: OnConditionsChangedListener<T>) {
        onConditionsChangedListeners.remove(listener)
    }

    /**
     * Launches validation
     *
     * 1) Check value against all validators
     * 2) The resulting list of results is passing to the operator
     * 3) The operator determines the overall result
     *
     * @param value - checking value
     * **/
    override fun validate(value: T?): ValidationResult {
        val validationResults = conditions.map { it.validate(value) }.toSet()
        return operator.validate(validationResults)
    }

    fun getConditionsSet(): Set<Condition<T?>> {
        return conditions
    }

    fun addCondition(condition: Condition<T?>) {
        changeConditionsSet {
            add(condition)
        }
    }

    fun removeCondition(condition: Condition<T?>) {
        if (!conditions.contains(condition)) {
            return
        }

        changeConditionsSet {
            remove(condition)
        }
    }

    /**
     * Modifies the list of conditions and notifies listeners about it
     *
     * @param block - Transformation to be applied to [conditions]
     * **/
    fun changeConditionsSet(block: MutableSet<Condition<T?>>.() -> Unit) {
        conditions.apply(block)
        dispatchConditions()
    }

    private fun dispatchConditions() {
        dispatchConditions(conditions)
    }

    /**
     * Оповещает слушателей [onConditionsChangedListeners]
     * **/
    private fun dispatchConditions(newConditionsSet: MutableSet<Condition<T?>>) {
        onConditionsChangedListeners.forEach {
            it.onConditionsChanged(newConditionsSet)
        }
    }

    /**
     * Operator
     * Checks a set of validation results and returns one result for this set
     * **/
    fun interface Operator : Condition<Collection<ValidationResult>> {
        /**
         * Conjunction operator
         *
         * @return [ValidationResult] (true) - If there are no invalid conditions or there are no conditions at all
         * @return [ValidationResult] (false) - If at least one invalid condition is found
         * **/
        class Conjunction : Operator {
            override fun validate(value: Collection<ValidationResult>?): ValidationResult {
                return conjunction(value)
            }

            private fun conjunction(value: Collection<ValidationResult>?): ValidationResult {
                val invalidResult = value?.find { !it.isValid }
                return invalidResult ?: ValidationResult.valid()
            }
        }

        /**
         * Disjunction operator
         *
         * @return [ValidationResult] (true) - If at least one valid condition is found
         * @return [ValidationResult] (false) - If all conditions are invalid; There are no conditions at all
         * **/
        class Disjunction : Operator {
            override fun validate(value: Collection<ValidationResult>?): ValidationResult {
                return disjunction(value)
            }

            private fun disjunction(value: Collection<ValidationResult>?): ValidationResult {
                val validResult = value?.find { it.isValid }
                return validResult ?: ValidationResult.invalid(null)
            }
        }
    }

    fun interface OnOperatorChangedListener {
        fun onOperatorChanged()
    }

    fun interface OnConditionsChangedListener<T> {
        fun onConditionsChanged(conditions: Set<Condition<T?>>)
    }

    companion object {
        /**
         * Creates an instance of the [Validator] with initial condition from the parameters
         *
         * @param errorMessage - Error message [ValidationResult.errorMessage] of the initial condition
         * @param isValueValid - Initial condition [Condition.validate] function
         * **/
        fun <V> create(errorMessage: String? = null, isValueValid: (value: V?) -> Boolean): Validator<V?> =
            Validator(Condition.create(errorMessage, isValueValid))
    }
}