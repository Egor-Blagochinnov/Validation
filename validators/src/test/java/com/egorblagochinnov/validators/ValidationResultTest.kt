package com.egorblagochinnov.validators

import org.junit.Test
import org.junit.Assert.*

class ValidationResultTest {

    @Test
    fun plus() {
        plus_true_true()
        plus_true_false()
        plus_false_false()
    }

    /**
     * Add ("+") two [ValidationResult]
     *
     * true + true = true (no error message)
     * **/
    @Test
    fun plus_true_true() {
        val resultTrue1 = ValidationResult(true, "false error1")
        val resultTrue2 = ValidationResult(true, "false error2")
        val plus = resultTrue1 + resultTrue2
        val plusReversed = resultTrue2 + resultTrue1

        assertTrue(plus.isValid)
        assertTrue(plus.errorMessage == null)

        assertTrue(plusReversed.isValid)
        assertTrue(plusReversed.errorMessage == null)
    }

    /**
     * Add ("+") two [ValidationResult]
     *
     * true + false (with error message) = true (no error message)
     * false (with error message) + true = true (no error message)
     * **/
    @Test
    fun plus_true_false() {
        val resultTrue = ValidationResult(true)
        val resultFalse = ValidationResult(false, "error")
        val plus = resultTrue + resultFalse
        val plusReversed = resultFalse + resultTrue

        assertTrue(plus.isValid)
        assertTrue(plus.errorMessage == null)

        assertTrue(plusReversed.isValid)
        assertTrue(plusReversed.errorMessage == null)
    }

    /**
     * Add ("+") two [ValidationResult]
     *
     * false (with error message 1) + false (with error message 2) = false (with error message 1)
     * false (with error message 2) + false (with error message 1) = false (with error message 2)
     * **/
    @Test
    fun plus_false_false() {
        val resultFalse1 = ValidationResult(false, "error1")
        val resultFalse2 = ValidationResult(false, "error2")
        val plus = resultFalse1 + resultFalse2
        val plusReversed = resultFalse2 + resultFalse1

        assertFalse(plus.isValid)
        assertTrue(plus.errorMessage == "error1")

        assertFalse(plusReversed.isValid)
        assertTrue(plusReversed.errorMessage == "error2")
    }

    @Test
    fun times() {
        times_true_true()
        times_true_false()
        times_false_false()
    }

    /**
     * Multiply ("*") two [ValidationResult]
     *
     * true (with error message 1) * true (with error message 2) = true (no error message)
     * **/
    @Test
    fun times_true_true() {
        val resultTrue1 = ValidationResult(true, "false error1")
        val resultTrue2 = ValidationResult(true, "false error2")
        val times = resultTrue1 * resultTrue2
        val timesReversed = resultTrue2 * resultTrue1

        assertTrue(times.isValid)
        assertTrue(times.errorMessage == null)

        assertTrue(times.isValid)
        assertTrue(timesReversed.errorMessage == null)
    }

    /**
     * Multiply ("*") two [ValidationResult]
     *
     * true (error message 1) * false (with error message 2) = false (with error message 2)
     * false (with error message 1) * true (error message 2) = false (with error message 1)
     * **/
    @Test
    fun times_true_false() {
        val resultTrue = ValidationResult(true, "false error")
        val resultFalse = ValidationResult(false, "true error")
        val times = resultTrue * resultFalse
        val timesReversed = resultFalse * resultTrue

        assertFalse(times.isValid)
        assertTrue(times.errorMessage == "true error")

        assertFalse(times.isValid)
        assertTrue(timesReversed.errorMessage == "true error")
    }

    /**
     * Multiply ("*") two [ValidationResult]
     *
     * false (with error message 1) * false (with error message 2) = false (with error message 1)
     * false (with error message 2) * false (with error message 1) = false (with error message 2)
     * **/
    @Test
    fun times_false_false() {
        val resultFalse1 = ValidationResult(false, "error1")
        val resultFalse2 = ValidationResult(false, "error2")
        val times12 = resultFalse1 * resultFalse2
        val times21 = resultFalse2 * resultFalse1

        assertFalse(times12.isValid)
        assertTrue(times12.errorMessage == "error1")

        assertFalse(times21.isValid)
        assertTrue(times21.errorMessage == "error2")
    }
}