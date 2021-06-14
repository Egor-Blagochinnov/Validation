package com.egorblagochinnov.validators

import androidx.lifecycle.*

/**
 * Validator for LiveData values
 * As soon as the data in [source] changes - checks their validity by the set of conditions ([conditions])
 *
 * - Activation
 * For [LiveDataValidator] to work - it, or rather [mediator], needs an observer [Observer].
 * That is, in order to activate [LiveDataValidator] you need to subscribe to it. See [observe], [observeForever]
 * Or you can add [LiveDataValidator.state] to [MediatorLiveData] to use the lifecycle of [MediatorLiveData]
 *
 * - Validation
 * Validation will be performed automatically when [source] is changing
 *
 * - Additional sources
 * Additional sources are needed in order to respond to their changes.
 * For example, you can change the set of conditions [LiveDataValidator.changeConditionsSet] when changing LiveData<Boolean>
 * See [watchOn], [triggerOn]
 *
 * @param source - Data source to watch out for
 * @param initialCondition - Initial condition
 * @param operator - An operator that defines the result of the validation.
 * **/
open class LiveDataValidator<T>(
    var source: LiveData<T>,
    initialCondition: Condition<T?>? = null,
    operator: Operator = Operator.Conjunction()
) : Validator<T?>(initialCondition, operator) {
    constructor(
        source: LiveData<T>,
        operator: Operator
    ) : this(source, null, operator)

    private val dataSourceObserver = Observer<T?> { data ->
        validate(data)
    }

    /**
     * Responsible for the state of the validator
     * Observes source [source] and validates each value in it
     * Observes additional sources. See [watchOn], [triggerOn]
     * **/
    protected val mediator = MediatorLiveData<ValidationResult>().apply {
        addSource(source) { data ->
            validate(data)
        }
    }

    /**
     * Shows the current state of [LiveDataValidator]
     * **/
    val state: LiveData<ValidationResult>; get() = mediator

    private val conditionsChangedListener = OnConditionsChangedListener<T?> {
        validate()
    }

    private val operatorChangedListener = OnOperatorChangedListener {
        validate()
    }

    init {
        addOperatorChangedListener(operatorChangedListener)
        addConditionsChangedListener(conditionsChangedListener)
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<in ValidationResult>) {
        mediator.observe(lifecycleOwner, observer)
    }

    fun observeForever(observer: Observer<in ValidationResult>) {
        mediator.observeForever(observer)
    }

    /**
     * @see LiveData.removeObserver
     * **/
    fun removeObserver(observer: Observer<in ValidationResult>) {
        mediator.removeObserver(observer)
    }

    /**
     * Validate the current value ([LiveData.getValue]) of the source [source]
     * **/
    fun validate(): ValidationResult {
        return validate(source?.value)
    }

    /**
     * Validates the value in the same way as [Validator.validate]
     * Also updates the [mediator] state
     * **/
    override fun validate(value: T?): ValidationResult {
        val isValid = super.validate(value)

        mediator.value = isValid

        return isValid
    }

    /**
     * Track changes from multiple [sources]
     *
     * Listens to many different data sources
     * When changing any of them, perform the same action from [observer]
     *
     * @param sources - Data sources
     * @param observer - An observer that will subscribe to all sources
     * **/
    fun watchOn(vararg sources: LiveData<*>, observer: Observer<Any?>) {
        sources.forEach {
            mediator.addSource(it, observer)
        }
    }

    /**
     * Track changes from multiple [sources]
     *
     * Listens to many different data sources
     * When changing any of them, perform the same action from [observer]
     *
     * @param sources - Data sources
     * @param observer - An observer that will subscribe to all sources
     * **/
    fun watchOn(sources: List<LiveData<*>>, observer: Observer<Any?>) {
        sources.forEach {
            mediator.addSource(it, observer)
        }
    }

    /**
     * Add data sources, when any of [sources] changed, the validator will be re-validated ([validate])
     * **/
    fun triggerOn(sources: Collection<LiveData<*>>) {
        sources.forEach { source ->
            triggerOn(source)
        }
    }

    /**
     * Add data source, when [source] changed, the validator will be re-validated ([validate])
     * **/
    fun triggerOn(source: LiveData<*>) {
        mediator.addSource(source) {
            validate()
        }
    }

    /**
     * Add data source, when [source] changed, the validator will be re-validated ([validate])
     * **/
    fun triggerOn(vararg sources: LiveData<*>) {
        sources.forEach { source ->
            triggerOn(source)
        }
    }

    /**
     * Performs action from [observer] when [newSource] changes
     * **/
    fun <D> watchOn(newSource: LiveData<D>, observer: Observer<D>) {
        mediator.addSource(newSource, observer)
    }

    fun removeSource(source: LiveData<*>) {
        mediator.removeSource(source)
    }
}

