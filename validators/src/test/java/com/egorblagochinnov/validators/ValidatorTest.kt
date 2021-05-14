package com.egorblagochinnov.validators

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egorblagochinnov.validators.core.Condition
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ValidatorTest {
    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private val xCondition = Condition.create<String?>("no x") { it?.contains("x") == true }
    private val yCondition = Condition.create<String?>("no y") { it?.contains("y") == true }
    private val zCondition = Condition.create<String?>("no z") { it?.contains("z") == true }

    @Test
    fun validate() {
        validate_empty_conditions()
        validate_single_condition()
        validate_multiple_conditions()
        validate_complex_condition()
    }

    @Test
    fun validate_empty_conditions() {
        val emptyValidator = Validator<String?>()

        assertTrue(emptyValidator.validate("string").isValid)
        assertTrue(emptyValidator.validate("").isValid)
        assertTrue(emptyValidator.validate(null).isValid)
    }

    @Test
    fun validate_single_condition() {
        val validator = Validator<String?>().apply {
            addCondition(Condition.create { it?.contains("x") == true })
        }

        assertFalse(validator.validate("qwerty").isValid)

        assertTrue(validator.validate("qwertyx").isValid)
    }

    @Test
    fun validate_multiple_conditions() {
        val validator = Validator<String?>().apply {
            addCondition(xCondition)
            addCondition(yCondition)
            addCondition(zCondition)
        }

        assertTrue(validator.validate("xyz123").isValid)
        assertTrue(validator.validate("1x070y029438z").isValid)

        validator.validate("xy1234567890").let { validationResult ->
            assertFalse(validationResult.isValid)
            assertTrue(validationResult.errorMessage == "no z")
        }

        validator.validate("xz1234567890").let { validationResult ->
            assertFalse(validationResult.isValid)
            assertTrue(validationResult.errorMessage == "no y")
        }
    }

    @Test
    fun validate_complex_condition() {
        val validator = Validator<String?>(xCondition * (yCondition + zCondition))

        assertTrue(validator.validate("xyz123").isValid)
        assertTrue(validator.validate("xy123").isValid)

        validator.validate("yz123").let { validationResult ->
            assertFalse(validationResult.isValid)
            assertEquals(validationResult.errorMessage, "no x")
        }

        validator.validate("y123").let { validationResult ->
            assertFalse(validationResult.isValid)
            assertEquals(validationResult.errorMessage, "no x")
        }

        validator.validate("x123").let { validationResult ->
            assertFalse(validationResult.isValid)
            assertTrue(validationResult.errorMessage == "no y" || validationResult.errorMessage == "no z")
        }
    }

    @Test
    fun conditionsSet() {
        val validator = Validator<String?>().apply {
            addCondition(xCondition)
            addCondition(yCondition)
            addCondition(zCondition)
        }

        assertEquals(validator.getConditionsSet()?.size, 3)
        assertTrue(validator.getConditionsSet()?.contains(xCondition) == true)
        assertTrue(validator.getConditionsSet()?.contains(yCondition) == true)
        assertTrue(validator.getConditionsSet()?.contains(zCondition) == true)
    }

    @Test
    fun conditions() {
        val validator = Validator<String?>().apply {
            addCondition(xCondition)
            addCondition(yCondition)
            addCondition(zCondition)
        }

        assertNotNull(validator.getConditionsSet())
    }

    @Test
    fun addCondition() {
        val validator = Validator<String?>()

        validator.addCondition(xCondition)
        assertTrue(validator.getConditionsSet()?.size == 1)
        assertTrue(validator.validate("1234x").isValid)
        assertFalse(validator.validate("1234").isValid)

        validator.addCondition(yCondition)
        assertTrue(validator.getConditionsSet()?.size == 2)
        assertTrue(validator.validate("1234yx").isValid)
        assertFalse(validator.validate("1234x").isValid)

        validator.addCondition(zCondition)
        assertTrue(validator.getConditionsSet()?.size == 3)
        assertTrue(validator.validate("1z234zyx").isValid)
        assertFalse(validator.validate("1234yx").isValid)
    }

    @Test
    fun removeCondition() {
        val validator = Validator<String?>().apply {
            addCondition(xCondition)
            addCondition(yCondition)
            addCondition(zCondition)
        }

        validator.removeCondition(xCondition)

        assertTrue(validator.getConditionsSet()?.size == 2)
        assertTrue(validator.validate("1234yz").isValid)

        validator.removeCondition(yCondition)

        assertTrue(validator.getConditionsSet()?.size == 1)
        assertTrue(validator.validate("1234z").isValid)

        validator.removeCondition(zCondition)

        assertTrue(validator.getConditionsSet()?.size == 0)
        assertTrue(validator.validate("1234").isValid)
    }

    @Test
    fun changeConditionsSet() {
        val validator = Validator<String?>().apply {
            addCondition(xCondition)
            addCondition(yCondition)
            addCondition(zCondition)
        }

        validator.changeConditionsSet {
            remove(xCondition)
        }

        assertTrue(validator.getConditionsSet()?.size == 2)
        assertTrue(validator.getConditionsSet()?.contains(xCondition) == false)

        validator.changeConditionsSet {
            add(xCondition)
        }

        assertTrue(validator.getConditionsSet()?.size == 3)
        assertTrue(validator.getConditionsSet()?.contains(xCondition) == true)

        validator.changeConditionsSet {
            remove(xCondition)
            remove(zCondition)
        }

        assertTrue(validator.getConditionsSet()?.size == 1)
        assertTrue(validator.getConditionsSet()?.contains(xCondition) == false)
        assertTrue(validator.getConditionsSet()?.contains(yCondition) == true)
        assertTrue(validator.getConditionsSet()?.contains(zCondition) == false)
    }

    @Test
    fun changeConditionsSet_observer_test() {
        var state: String = "INIT"

        val observer = object : Validator.OnConditionsChangedListener<String?> {
            override fun onConditionsChanged(conditions: Set<Condition<String?>>) {
                if (state == "INIT") { return }

                if (state == "X_REMOVED") {
                    assertEquals(conditions?.size?:0, 2)
                    assertTrue(conditions?.contains(xCondition) == false)
                    return
                }

                if (state == "X_AND_Y_REMOVED") {
                    assertEquals(conditions?.size?:0, 1)
                    assertTrue(conditions?.contains(yCondition) == false)
                    return
                }

                if (state == "Z_REMOVED_ADD_X_Y") {
                    assertEquals(conditions?.size?:0, 2)
                    assertTrue(conditions?.contains(zCondition) == false)
                    assertTrue(conditions?.contains(xCondition) == true)
                    assertTrue(conditions?.contains(yCondition) == true)
                    return
                }
            }
        }

        val validator = Validator<String?>().apply {
            addCondition(xCondition)
            addCondition(yCondition)
            addCondition(zCondition)

            addConditionsChangedListener(observer)
        }

        state = "X_REMOVED"
        validator.changeConditionsSet {
            remove(xCondition)
        }

        state = "X_AND_Y_REMOVED"
        validator.changeConditionsSet {
            remove(yCondition)
        }

        state = "Z_REMOVED_ADD_X_Y"
        validator.changeConditionsSet {
            remove(zCondition)
            add(xCondition)
            add(yCondition)
        }

        validator.removeConditionsChangedListener(observer)
    }
}