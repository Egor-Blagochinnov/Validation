package com.egorblagochinnov.validators

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import org.junit.Rule

class LiveDataValidatorMuxTest {
    @get:Rule
    var instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private var source: MutableLiveData<String?> = MutableLiveData()

    private val validatorX: LiveDataValidator<String?> by lazy {
        LiveDataValidator<String?>(source).apply {
            addCondition(Condition.create("no X") {
                it?.contains("X") == true
            })
        }
    }

    private val validatorY: LiveDataValidator<String?> by lazy {
        LiveDataValidator<String?>(source).apply {
            addCondition(Condition.create("no Y") {
                it?.contains("Y") == true
            })
        }
    }

    private val validatorZ: LiveDataValidator<String?> by lazy {
        LiveDataValidator<String?>(source).apply {
            addCondition(Condition.create("no Z") {
                it?.contains("Z") == true
            })
        }
    }

    private val mockObserver = Observer<ValidationResult> {

    }

    private val mux = MuxLiveDataValidator()

    @Before
    fun setup() {
        mux.observeForever(mockObserver)
    }

    @Test
    fun validate() {

    }

    @Test
    fun isValid() {
        mux.addValidator(validatorX)
        mux.addValidator(validatorY)
        mux.addValidator(validatorZ)

        source.value = "XY"
        val isValid = mux.isValid()
        val errorMessage = mux.state.value?.errorMessage

        assertFalse(isValid)
        assertEquals(errorMessage, "no Z")

        source.value = "XYZ"
        val isValid2 = mux.isValid()
        val errorMessage2 = mux.state.value?.errorMessage

        assertTrue(isValid2)
        assertEquals(errorMessage2, null)
    }

    @Test
    fun watchOn() {

    }

    @Test
    fun testWatchOn() {

    }

    @Test
    fun triggerOn() {

    }

    @Test
    fun testTriggerOn() {

    }

    @Test
    fun testTriggerOn1() {
    }

    @Test
    fun testWatchOn1() {
    }

    @Test
    fun removeSource() {
    }

    @After
    fun detach() {
        mux.observeForever(mockObserver)
    }
}