package com.egorblagochinnov.validators

import androidx.lifecycle.*

/**
 * Валидатор LiveData
 * Как только данные в [source] меняются - проверяет их валидность по валидатору
 *
 * - Активация
 * Чтобы [LiveDataValidator] работал - ему, а точнее [mediator] необходим наблюдатель [Observer].
 * То есть для того, чтобы активироать [LiveDataValidator] нужно подписаться на него. См. [observe], [observeForever]
 * Или можно добавить [LiveDataValidator.state] в [MediatorLiveData], чтобы использовать жизненный цикл [MediatorLiveData]
 *
 * - Валидация
 * Валидация происхрлит автоматическа при изменении [source]
 *
 * - Дополнительные источники
 * Дополнительные источники нужны для того, чтобы реагировать на изменения других источников, кроме [source]
 * См. [watchOn], [triggerOn]
 *
 * @param source - источник данных, за которым надо следить
 * @param initialCondition - Начальное условие. Необязательный параметр
 * **/
open class LiveDataValidator<T>(
        private val source: LiveData<T>,
        initialCondition: Condition<T?>? = null
) : Validator<T?>(initialCondition) {

    /**
     * Главный компонент [LiveDataValidator]
     * Отвечает за состояние валидатора
     * Именно медиатор следит за источником [source] и валидирует каждое изменение источника
     * Так же следит за набором условий валидатора [Validator.conditions]. Как только меняются условия - источник проверяется по новым условиям
     *
     * Тут же прослушиваются дополнительные источники см. [watchOn], [triggerOn]
     * **/
    protected val mediator = MediatorLiveData<ValidationResult>().apply {
        addSource(source) { data ->
            validateAndUpdateState(data)
        }

        addSource(conditions()) {
            validate()
        }
    }

    /**
     * Немутабельный [mediator]
     * Показывает текущее состояние [LiveDataValidator]
     * **/
    val state: LiveData<ValidationResult>; get() = mediator

    /**
     * Подписка на состояние валидатора [mediator]
     * @see LiveData.observe
     * **/
    fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<in ValidationResult>) {
        mediator.observe(lifecycleOwner, observer)
    }

    /**
     * Вечная подписка на состояние валидатора [mediator]
     * @see LiveData.observeForever
     * **/
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
     * В отличии от [LiveDataValidator] не привязывается к одному источнику с одним типом данных,
     * а следит за множеством источников с разными типами данных
     *
     * По-умолчанию Mux будет true если все его валидаторы true См. [Mux.defaultCheck]
     * Чтобы валидировать как-то иначе - переопредели [validate]
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
        val state: LiveData<ValidationResult> = mediator

        /**
         * Набор валидаторов по которым нужно делать проверку
         * **/
        protected val validators: MutableSet<LiveDataValidator<*>> = LinkedHashSet<LiveDataValidator<*>>()

        init {
            if (!initialValidators.isNullOrEmpty()) {
                initialValidators.forEach {
                    if (validators.add(it)) {
                        mediator.addSource(it.state, this)
                    }
                }

                check()
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
                mediator.addSource(validator.state, this)
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
                mediator.removeSource(validator.state)
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

        fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ValidationResult>) {
            mediator.observe(lifecycleOwner, observer)
        }

        fun observeForever(observer: Observer<ValidationResult>) {
            mediator.observeForever(observer)
        }

        /**
         * Проверка
         * Изменяет состояние [mediator]
         * Сводится к вызову [validate] и получения из него нового состояния (результата [ValidationResult])
         *
         * @param lastValidationResult - результат срабатывания какого-то валидатора.
         * **/
        private fun check(lastValidationResult: ValidationResult? = null) {
            val results = validators.mapNotNull { it.state.value }
            mediator.value = validate(lastValidationResult, results)
        }

        /**
         * Валидация
         * Изменяет состояние [mediator]
         * Сводится к вызову [validate] и получения из него нового состояния (результата [ValidationResult])
         *
         * Важно в этом методе не вызывать LiveDataValidator.validate() ни у одного из валидаторов [validators]
         * Это обновит состояния LiveDataValidator и соответственно у Mux сработает onChecked()
         * Получится бесконечный цикл
         *
         * @param lastValidationResult - Валидатор, который сработал. Точнее это его результат
         * @param allResults - Состояния (результаты) всех остальных валидаторов
         * **/
        protected open fun validate(
                lastValidationResult: ValidationResult? = null,
                allResults: List<ValidationResult>
        ): ValidationResult {
            return defaultCheck(lastValidationResult, allResults)
        }

        /**
         * Стандартная валидация
         *
         * @param lastValidationResult - Валидатор, который сработал. Точнее это его результат
         * @param allResults - Состояния (результаты) всех остальных валидаторов
         *
         * @return [ValidationResult] true если все валидаторы [validators] вернут true
         * @return [ValidationResult] false если хоть один валидатор вернет false
         * **/
        private fun defaultCheck(
                lastValidationResult: ValidationResult?,
                allResults: List<ValidationResult>
        ): ValidationResult {
            if (lastValidationResult?.isValid == false) {
                return lastValidationResult
            }

            val invalidResult = allResults.find { !it.isValid }

            return invalidResult?: ValidationResult(true)
        }

        fun isValid(): Boolean {
            return mediator.value?.isValid == true
        }
    }

}

