package com.egorblagochinnov.validators

import com.egorblagochinnov.validators.core.Condition
import com.egorblagochinnov.validators.core.ValidationResult
import kotlin.collections.HashSet

/**
 * Проверяет значение <T> по множеству условий
 *
 * @param initialCondition - Начальное условие.
 * @param operator - Оператор, который определяет результат валидации
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
     * Пропускает значение через все валидаторы
     * Полученный список результатов передает оператору
     * Оператор определяет общий результат
     *
     * @param value - проверяемое значение
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
     * Изменяет список условий и оповещает слушателей об этом
     *
     * @param block - Преобразование, которое надо сделать с условиями
     * **/
    fun changeConditionsSet(block: MutableSet<Condition<T?>>.() -> Unit) {
        /* Делаем мутабельный Set из того, что есть сейчас */
        val currentConditionsSet = HashSet<Condition<T?>>().apply {
            addAll(conditions)
        }

        /* Применяем преобразования */
        val newConditionsSet = currentConditionsSet.apply(block)

        /* Устанавливаем новые условия и оповещаем слушателей */
        dispatchConditions(newConditionsSet)
    }

    /**
     * Меняет список условий
     * Оповещает слушателей
     * **/
    private fun dispatchConditions(newConditionsSet: MutableSet<Condition<T?>>) {
        conditions.clear()
        conditions.addAll(newConditionsSet)
        onConditionsChangedListeners.forEach { it.onConditionsChanged(conditions) }
    }

    /**
     * Оператор
     * Проверяет набор результатов валидации и по этому набору возвращает один результат
     * **/
    fun interface Operator : Condition<Set<ValidationResult>> {
        /**
         * Оператор конъюнкция
         *
         * @return [ValidationResult](true) - Если невалидных условий нет; Условий нет вообще
         * @return [ValidationResult](false) - Если нашелся хотья бы одно невалидное условие
         * **/
        class Conjunction : Operator {
            override fun validate(value: Set<ValidationResult>?): ValidationResult {
                return conjunction(value)
            }

            private fun conjunction(value: Set<ValidationResult>?): ValidationResult {
                val invalidResult = value?.find { !it.isValid }
                return invalidResult ?: ValidationResult.valid()
            }
        }

        /**
         * Оператор дизъюнкции
         *
         * @return [ValidationResult](true) - Если найдется хоть одно валидное условие
         * @return [ValidationResult](false) - Если все условия невалдные; Условий нет вообще
         * **/
        class Disjunction : Operator {
            override fun validate(value: Set<ValidationResult>?): ValidationResult {
                return disjunction(value)
            }

            private fun disjunction(value: Set<ValidationResult>?): ValidationResult {
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
         * Создаёт экземпляр валидатора из по параметрам начального условия
         *
         * @param description - Описание начального условия
         * @param isValueValid - Функция проверки начального условия
         * **/
        fun <V> create(description: String? = null, isValueValid: (value: V?) -> Boolean): Validator<V?> =
            Validator(Condition.create(description, isValueValid))
    }
}