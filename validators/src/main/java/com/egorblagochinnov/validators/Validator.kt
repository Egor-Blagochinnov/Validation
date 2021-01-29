package com.egorblagochinnov.validators

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
open class Validator<T>(initialCondition: Condition<T?>? = null) : Condition<T?> {
    /**
     * Множество условий
     * Набор условий может меняться,
     * поэтому на него можно подписываться и следить за новыми условиями
     * **/
    private val conditions: MutableLiveData<Set<Condition<T?>>> by lazy {
        MutableLiveData<Set<Condition<T?>>>().apply {
            value = LinkedHashSet<Condition<T?>>()
        }
    }

    init {
        initialCondition?.let {
            addCondition(it)
        }
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
        val conditionSet = conditions.value

        return if (conditionSet.isNullOrEmpty()) {
            ValidationResult(true)
        } else {
            conjunction(value, conditionSet)
        }
    }

    /**
     * Перечисление текущих условий
     * **/
    fun conditionsSet(): Set<Condition<T?>>? {
        return conditions().value
    }

    /**
     * Условия валидации
     *
     * @return LiveData с перечислением всех условий
     * **/
    fun conditions(): LiveData<Set<Condition<T?>>> {
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
        if (conditions.value?.contains(condition) != true) {
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
            conditionsSet()?.let { addAll(it) }
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
    private fun dispatchConditions(conditionsSet: MutableSet<Condition<T?>>?) {
        conditions.value = conditionsSet
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
        fun <T> conjunction(value: T?, conditions: Collection<Condition<T?>>): ValidationResult {
            var invalidResult: ValidationResult? = null

            conditions.forEach {
                val result = it.validate(value)

                if (!result.isValid) {
                    invalidResult = result
                    return@forEach
                }
            }

            return invalidResult?: ValidationResult(true)
        }

        /**
         * Дизъюнкция
         * Проверка валидности
         * Находит первое валидное условие
         *
         * @return [ValidationResult](true) - Если найдется хоть одно валидное условие; Условий нет вообще
         * @return [ValidationResult](false) - Если все условия невалдные
         *
         * @param value - проверяемое значение
         * @param conditions - условия, по которым проверяется значение
         * **/
        fun <T> disjunction(value: T?, conditions: Collection<Condition<T?>>): ValidationResult {
            var validResult: ValidationResult? = null

            conditions.forEach {
                val result = it.validate(value)

                if (result.isValid) {
                    validResult = result
                    return@forEach
                }
            }

            return validResult?: ValidationResult(true)
        }
    }
}