package com.egorblagochinnov.validators

import androidx.lifecycle.*

/**
 * Validator-multiplexer
 * Subscribes to other validators and when any of them is triggered
 * calls [MuxLiveDataValidator.check] to change its [state]
 *
 * Unlike [LiveDataValidator], it does not bind to one source with one data type,
 * but keeps track of many sources with different data types
 *
 * @param initialValidators - initial validators.
 * @param operator - An operator that defines the result of the validation.
 * * **/
open class MuxLiveDataValidator(
    initialValidators: Collection<LiveDataValidator<*>>? = null,
    private var operator: Validator.Operator = Validator.Operator.Conjunction()
) : Observer<ValidationResult>, LifecycleObserver {
    constructor(vararg initialValidators: LiveDataValidator<*>) : this(initialValidators.toList())
    constructor(
        vararg initialValidators: LiveDataValidator<*>,
        operator: Validator.Operator = Validator.Operator.Conjunction()
    ) : this(
        initialValidators.toList(),
        operator
    )

    /**
     * Mediator. Responsible for the state of Mux
     * The value changes with each call to [check]
     * **/
    protected val mediator = MediatorLiveData<ValidationResult>()

    /**
     * Shows the current state of [MuxLiveDataValidator]
     * **/
    val state: LiveData<ValidationResult> = mediator

    /**
     * A set of validators [LiveDataValidator] for which you need to check
     * **/
    protected val validators: MutableSet<LiveDataValidator<*>> = LinkedHashSet<LiveDataValidator<*>>()

    private val onOperatorChangedListeners: ArrayList<Validator.OnOperatorChangedListener> by lazy { ArrayList() }

    init {
        addValidators(initialValidators)

        addOperatorChangedListener {
            check()
        }
    }

    fun setOperator(operator: Validator.Operator) {
        this.operator = operator
        onOperatorChangedListeners.forEach { it.onOperatorChanged() }
    }

    fun addOperatorChangedListener(listener: Validator.OnOperatorChangedListener) {
        onOperatorChangedListeners.add(listener)
    }

    fun removeOperatorChangedListener(listener: Validator.OnOperatorChangedListener) {
        onOperatorChangedListeners.remove(listener)
    }

    fun addValidators(newValidators: Collection<LiveDataValidator<*>>?) {
        if (newValidators.isNullOrEmpty()) {
            return
        }

        newValidators.forEach {
            if (this.validators.add(it)) {
                mediator.addSource(it.state, this)
            }
        }

        check()
    }

    fun addValidator(validator: LiveDataValidator<*>) {
        if (validators.add(validator)) {
            mediator.addSource(validator.state, this)
            check()
        }
    }

    /**
     * Watches over some third-party source
     * Needed for Mux to be able to respond to data sources that it does not validate
     * For example, when you change some third-party parameter, you can change the set of validators [validators]
     * **/
    fun <T> watchOn(source: LiveData<T>, observer: Observer<T>) {
        mediator.addSource(source, observer)
    }

    /**
     * Removes the validator
     * If deleted - recheck [check]
     * **/
    fun removeValidator(validator: LiveDataValidator<*>) {
        if (validators.remove(validator)) {
            mediator.removeSource(validator.state)
            check()
        }
    }

    /**
     * One of the [validators] has been changed
     *
     * @param t - the result of triggered validator
     * **/
    override fun onChanged(t: ValidationResult?) {
        check()
    }

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ValidationResult>) {
        mediator.observe(lifecycleOwner, observer)
    }

    fun observeForever(observer: Observer<ValidationResult>) {
        mediator.observeForever(observer)
    }

    fun removeObserver(observer: Observer<ValidationResult>) {
        mediator.removeObserver(observer)
    }

    private fun check() {
        val results = validators
            .mapNotNull { it.state.value }
            .toSet()
        mediator.value = operator.validate(results)
    }

    fun isValid(): Boolean {
        return mediator.value?.isValid == true
    }
}