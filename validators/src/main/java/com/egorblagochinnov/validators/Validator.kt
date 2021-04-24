package com.egorblagochinnov.validators

import kotlin.collections.HashSet

/**
 * Валидатор
 * Проверяет значение <T> по множеству условий
 *
 * Сам валидатор так же является условием.
 * Валидатор валидный если всё множество его условий валидно и наоборот.
 * Валидатор валидный если он не содиржит ни одного условия для проверки
 * Описание валидатора есть описание первого невалидного условия
 *
 * @param initialCondition - Начальное условие. Необязательный параметр
 * **/
open class Validator<T>(
        initialCondition: Condition<T?>? = null,
        private var operator: Operator = Operator.Conjunction()
) : Condition<T?> {
    /**
     * Множество условий
     * Набор условий может меняться,
     * поэтому на него можно подписываться и следить за новыми условиями
     * **/
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
        onOperatorChangedListeners.forEach { it.onOperatorChanged() }
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
     * Проверка валидности
     *
     * Находит первое невалидное условие
     * Если не задано ни одного условия - вернет true
     *
     * @return true - Если невалидных условий нет; Условий нет вообще
     * @return false - Если нашелся невалидное условие
     *
     * @param value - проверяемое значение
     * **/
    override fun validate(value: T?): ValidationResult {
        val validationResults = conditions.map { it.validate(value) }.toSet()
        return operator.validate(validationResults)
    }

    /**
     * Перечисление текущих условий
     * **/
    fun conditionsSet(): Set<Condition<T?>> {
        return conditions
    }

    /**
     * Добавить условие
     * **/
    fun addCondition(condition: Condition<T?>) {
        changeConditionsSet {
            add(condition)
        }
    }

    /**
     * Удалить условие
     * Если в текущих условиях нет удаляемого условия, то ничего не происходит
     *
     * @param condition - Условие, которое надо удалить
     * **/
    fun removeCondition(condition: Condition<T?>) {
        if (!conditions.contains(condition)) {
            return
        }

        changeConditionsSet {
            remove(condition)
        }
    }

    /**
     * Изменяет список условий и оповещает слушателей об этом
     *
     * @param block - Преобразование, которое надо сделать с условиями
     * **/
    fun changeConditionsSet(block: MutableSet<Condition<T?>>.() -> Unit) {
        /* Делаем мутабельный Set из того, что есть сейчас */
        val currentConditionsSet = HashSet<Condition<T?>>().apply {
            addAll(conditionsSet())
        }

        /* Применяем преобразования */
        val newConditionsSet = currentConditionsSet.apply(block)

        /* Устанавливаем новые условия и оповещаем слушателей */
        dispatchConditions(newConditionsSet)
    }

    /**
     * Меняет список условий
     * Как только изменится conditions.value
     * все, кто наблюдает за условиями получат новый перечень условий
     *
     * @param conditionsSet новые условия
     * **/
    private fun dispatchConditions(conditionsSet: MutableSet<Condition<T?>>) {
        conditions.clear()
        conditions.addAll(conditionsSet)
        onConditionsChangedListeners.forEach { it.onConditionsChanged(conditions) }
    }

    fun interface Operator : Condition<Set<ValidationResult>> {
        class Conjunction : Operator {
            override fun validate(value: Set<ValidationResult>?): ValidationResult {
                return conjunction(value)
            }

            /**
             * Конъюнкция
             * Проверка валидности
             * Находит первое невалидное условие
             *
             * @return [ValidationResult](true) - Если невалидных условий нет; Условий нет вообще
             * @return [ValidationResult](false) - Если нашелся невалидное условие
             *
             * @param value - проверяемое значение
             * @param conditions - условия, по которым проверяется значение
             * **/
            private fun conjunction(value: Set<ValidationResult>?): ValidationResult {
                val invalidResult = value?.find { !it.isValid }
                return invalidResult ?: ValidationResult.valid()
            }
        }

        class Disjunction: Operator {
            override fun validate(value: Set<ValidationResult>?): ValidationResult {
                return disjunction(value)
            }

            /**
             * Дизъюнкция
             * Проверка валидности
             * Находит первое валидное условие
             *
             * @return [ValidationResult](true) - Если найдется хоть одно валидное условие
             * @return [ValidationResult](false) - Если все условия невалдные; Условий нет вообще
             *
             * @param value - проверяемое значение
             * @param conditions - условия, по которым проверяется значение
             * **/
            fun disjunction(value: Set<ValidationResult>?): ValidationResult {
                val validResult = value?.find { it.isValid }
                return validResult?: ValidationResult(false)
            }
        }
    }

    fun interface OnOperatorChangedListener {
        fun onOperatorChanged()

        class LiveData: androidx.lifecycle.LiveData<Unit>(), OnOperatorChangedListener {
            override fun onOperatorChanged() {
                value = Unit
            }
        }
    }

    fun interface OnConditionsChangedListener <T> {
        fun onConditionsChanged(conditions: Set<Condition<T?>>)

        class LiveData <T> : androidx.lifecycle.LiveData<Set<Condition<T?>>>(), OnConditionsChangedListener <T> {
            override fun onConditionsChanged(conditions: Set<Condition<T?>>) {
                value = conditions
            }
        }
    }

    companion object {
        /**
         * Создаёт экземпляр валидатора из по параметрам начального условия
         *
         * @param description - Описание начального условия
         * @param isValueValid - Функция проверки начального условия
         * **/
        fun <V> create(description: String? = null, isValueValid: (value: V?) -> Boolean): Validator<V?> =
            Validator(Condition.create(description, isValueValid))
    }
}