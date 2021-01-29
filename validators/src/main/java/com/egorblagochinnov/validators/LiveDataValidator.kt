package com.egorblagochinnov.validators

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

/**
 * Валидатор для LiveData
 * Как только данные меняются проверяет их валидность по валидатору
 *
 * На LiveDataValidator нужно подписаться как на любую другую LiveData.
 * После подписки LiveDataValidator начнет слушать источник
 *
 * Наследуется MediatorLiveData<Boolean> и хранит результат последней проверки
 * Можно добавлять источники данных методом addSource(liveData)
 *
 * @param source - источник данных, за которым надо следить
 * @param initialCondition - Начальное условие. Необязательный параметр
 * **/
open class LiveDataValidator<T>(
        private val source: LiveData<T>,
        initialCondition: Condition<T?>? = null
) : Validator<T?>(initialCondition) {

    protected val mediator = MediatorLiveData<ValidationResult>().apply {
        addSource(source) { data ->
            validateAndUpdateState(data)
        }

        addSource(conditions()) {
            validate()
        }
    }

    val result: LiveData<ValidationResult>; get() = mediator

    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<in ValidationResult>) {
        mediator.observe(lifecycleOwner, observer)
    }

    fun observeForever(observer: Observer<in ValidationResult>) {
        mediator.observeForever(observer)
    }

    fun removeObserver(observer: Observer<in ValidationResult>) {
        mediator.removeObserver(observer)
    }

    /**
     * Проверяем текущее значение источника (source)
     * **/
    fun validate(): ValidationResult {
        return validateAndUpdateState(source.value)
    }

    private fun validateAndUpdateState(data: T?): ValidationResult {
        val isValid = validate(data)

        mediator.value = isValid

        return isValid
    }

    /**
     * Проверяем текущее значение источника (source)
     * **/
    fun isValid(): Boolean {
        return isValid(source.value)
    }

    /**
     * Отслеживание изменений из множества источников
     * Слушает множество источников различных данных
     * При изменении любого из них выполняет одно и то же действие из observer
     *
     * @param sources - источники данных
     * @param observer - наблюдатель, который будет подписан на все источники
     * **/
    fun watchOn(vararg sources: LiveData<*>, observer: Observer<Any?>) {
        sources.forEach {
            mediator.addSource(it, observer)
        }
    }

    /**
     * Отслеживание изменений из множества источников
     * Слушает множество источников различных данных
     * При изменении любого из них выполняет одно и то же действие из observer
     *
     * @param sources - источники данных
     * @param observer - наблюдатель, который будет подписан на все источники
     * **/
    fun watchOn(sources: List<LiveData<*>>, observer: Observer<Any?>) {
        sources.forEach {
            mediator.addSource(it, observer)
        }
    }

    /**
     * Добавляет источник данных, при изменении которого нужно перепроверить (обновить) валидатор
     * **/
    fun triggerOn(source: LiveData<*>) {
        mediator.addSource(source) {
            validate()
        }
    }

    /**
     * Добавляет источник данных, при изменении которого нужно перепроверить (обновить) валидатор
     * **/
    fun triggerOn(vararg sources: LiveData<*>) {
        sources.forEach { source ->
            triggerOn(source)
        }
    }

    /**
     * Добавляет источник данных, при изменении которого нужно перепроверить (обновить) валидатор
     * **/
    fun triggerOn(sources: Collection<LiveData<*>>) {
        sources.forEach { source ->
            triggerOn(source)
        }
    }

    fun <D> watchOn(newSource: LiveData<D>, observer: Observer<D>) {
        mediator.addSource(newSource, observer)
    }

    fun removeSource(source: LiveData<*>) {
        mediator.removeSource(source)
    }

    /**
     * Валидатор-мультиплексор
     * Подписывается на другие валидаторы и при срабатывании любого из них
     * вызывает [Mux.validate] чтобы поменять свое состояние
     *
     * По-умолчанию Mux будет true если все его валидаторы true [Mux.defaultCheck]
     *
     * @param initialValidators - начальные валидаторы.
     * Не обязательно передавать в конструкторе, можно добавить их позже, но по одному через [Mux.addValidator]
     * **/
    open class Mux(
            initialValidators: Collection<LiveDataValidator<*>>? = null,
    ): Observer<ValidationResult> {
        constructor(vararg initialValidators: LiveDataValidator<*>): this(initialValidators.toList())

        /**
         * Медиатор. Отвечает за состояние Mux
         * Значение меняется при каждом вызове [check]
         * **/
        protected val mediator = MediatorLiveData<ValidationResult>()

        /**
         * Состояние
         * **/
        val result: LiveData<ValidationResult> = mediator

        /**
         * Набор валидаторов по которым нужно делать проверку
         * **/
        protected val validators: MutableSet<LiveDataValidator<*>> = LinkedHashSet<LiveDataValidator<*>>()

        init {
            initialValidators?.let {
                validators.addAll(initialValidators)
            }
        }

        /**
         * Добавляет валидатор
         * Если валидатор добавлен (то есть такого валидатора ещё нет в наборе [validators]),
         * то подписываемся на этот валидатор и сразу делаем проверку [check]
         *
         * @param validator - Валидатор
         * **/
        fun addValidator(validator: LiveDataValidator<*>) {
            if (validators.add(validator)) {
                mediator.addSource(validator.result, this)
                check(validator.validate())
            }
        }

        /**
         * Наблюдает за каким-то сторонним источником
         * Нужен чтобы Mux мог реагировать на источники данных, которые он не валидирует
         * Например при изменении какого-то стороннего параметра можно изменить набор валидаторов [validators]
         * **/
        fun <T> watchOn(source: LiveData<T>, observer: Observer<T>) {
            mediator.addSource(source, observer)
        }

        /**
         * Удаляет валидатор
         * Если удалился - перепроверяем [check]
         * **/
        fun removeValidator(validator: LiveDataValidator<*>) {
            if (validators.remove(validator)) {
                mediator.removeSource(validator.result)
                check()
            }
        }

        /**
         * Какой-то валидатор изменился
         * Проверяем
         *
         * @param t - результат срабатывания какого-то валидатора
         * **/
        override fun onChanged(t: ValidationResult?) {
            check(t)
        }

        /**
         * Проверка
         * Изменяет состояние [mediator]
         * Сводится к вызову [validate] и получения из него нового состояния (результата [ValidationResult])
         *
         * @param lastValidationResult - результат срабатывания какого-то валидатора.
         * **/
        private fun check(lastValidationResult: ValidationResult? = null) {
            mediator.value = validate(lastValidationResult, validators)
        }

        /**
         * Валидация
         * Изменяет состояние [mediator]
         * Сводится к вызову [validate] и получения из него нового состояния (результата [ValidationResult])
         *
         * @param lastValidationResult - Валидатор, который сработал. Точнее это его результат
         * @param validators - все валидаторы
         * **/
        protected open fun validate(
                lastValidationResult: ValidationResult? = null,
                validators: Set<LiveDataValidator<*>>
        ): ValidationResult {
            return defaultCheck(lastValidationResult, validators)
        }

        /**
         * Стандартная валидация
         *
         * @param lastValidationResult - Валидатор, который сработал. Точнее это его результат
         * @param validators - все валидаторы
         *
         * @return [ValidationResult] true если все валидаторы [validators] вернут true
         * @return [ValidationResult] false если хоть один валидатор вернет false
         * **/
        private fun defaultCheck(
                lastValidationResult: ValidationResult?,
                validators: Set<LiveDataValidator<*>>
        ): ValidationResult {
            if (lastValidationResult?.isValid == false) {
                return lastValidationResult
            }

            var invalidResult: ValidationResult? = null

            validators.forEach {
                val result = it.validate()

                if (it.isValid()) {
                    invalidResult = result
                    return@forEach
                }
            }

            return invalidResult?: ValidationResult(true)
        }

        fun isValid(): Boolean {
            return mediator.value?.isValid == true
        }
    }

}

