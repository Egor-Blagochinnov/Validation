package com.egorblagochinnov.validators

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

class LiveDataValidatorTest {
    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var source: MutableLiveData<String?>

    private lateinit var validator: LiveDataValidator<String?>

    private val xCondition = Condition.create<String?>("no x") { it?.contains("x") == true }
    private val yCondition = Condition.create<String?>("no y") { it?.contains("y") == true }
    private val zCondition = Condition.create<String?>("no z") { it?.contains("z") == true }

    @Before
    fun setup() {
        source = MutableLiveData<String?>().apply { value = null }
        validator = LiveDataValidator(source)
    }

    @Test
    fun validate() {
        validate_empty()
    }

    @Test
    fun validate_empty() {
        val emptyValidator = LiveDataValidator(source)

        source.value = null
        assertEquals(emptyValidator.validate().isValid, true)

        source.value = ""
        assertEquals(emptyValidator.validate().isValid, true)

        source.value = "1"
        assertEquals(emptyValidator.validate().isValid, true)
    }

    @Test
    fun validate_xyz_conditions() {
        val validator = LiveDataValidator(source)

        validator.addCondition(xCondition)
        validator.addCondition(yCondition)
        validator.addCondition(zCondition)

        source.value = null
        assertEquals(validator.validate().isValid, false)

        source.value = ""
        assertEquals(validator.validate().isValid, false)

        source.value = "1x"
        assertEquals(validator.validate().isValid, false)

        source.value = "1xy"
        assertEquals(validator.validate().isValid, false)

        source.value = "1xyz"
        assertEquals(validator.validate().isValid, true)
    }

    @Test
    fun observe() {
        var state = "INIT"

        val validator = LiveDataValidator(source)

        val observer = Observer<ValidationResult> {
            when(state) {
                "INIT" -> {
                    return@Observer
                }

                "X_CONDITION_FALSE" -> {
                    assertEquals(it.isValid, false)
                    assertEquals(it.errorMessage, "no x")
                }

                "Y_CONDITION_FALSE" -> {
                    assertEquals(it.isValid, false)
                    assertEquals(it.errorMessage, "no y")
                }
            }
        }

        validator.addCondition(xCondition)
        validator.addCondition(yCondition)

        validator.observeForever(observer)

        state = "X_CONDITION_FALSE"
        source.value = "123yz"

        state = "Y_CONDITION_FALSE"
        source.value = "123xz"

        validator.removeObserver(observer)
    }

    @Test
    fun validate_when_condition_changed() {
        var state = "NO_CONDITIONS"
        source.value = "123"

        val validator = LiveDataValidator(source)

        val observer = Observer<ValidationResult> {
            when(state) {
                "NO_CONDITIONS" -> {
                    return@Observer
                }

                "ADD_X_CONDITION" -> {
                    assertEquals(it.isValid, false)
                    assertEquals(it.errorMessage, "no x")
                }

                "REMOVE_X_CONDITION" -> {
                    assertEquals(it.isValid, true)
                    assertEquals(it.errorMessage, null)
                }
            }
        }

        validator.observeForever(observer)

        state = "ADD_X_CONDITION"
        validator.addCondition(xCondition)

        state = "REMOVE_X_CONDITION"
        validator.removeCondition(xCondition)
    }

    @Test
    fun trackSources() {
        var state = "INIT"

        val validator = LiveDataValidator(source)

        val source1 = MutableLiveData<String?>()
        val source2 = MutableLiveData<String?>()

        validator.watchOn(source1, source1) {
            when(state) {
                "INIT" -> { }
                "SOURCE_1_CHANGED" -> {
                    assertEquals((it as? String?), "1")
                }
                "SOURCE_2_CHANGED" -> {
                    assertEquals((it as? String?), "2")
                }
                "SOURCE_2_DELETED" -> {
                    throw Exception("Observer should not fire after source was removed")
                }
                else -> {
                    throw Exception("Unexpected state")
                }
            }
        }

        state = "SOURCE_1_CHANGED"
        source1.value = "1"

        state = "SOURCE_2_CHANGED"
        source2.value = "2"

        state = "SOURCE_2_DELETED"
        validator.removeSource(source2)
        source2.value = "new 2"
    }

    @Test
    fun triggerOn() {

    }

    @Test
    fun observeSource() {


    }

    @Test
    fun trigger() {
        val validator = LiveDataValidator(source)

        validator.validate()

    }
}