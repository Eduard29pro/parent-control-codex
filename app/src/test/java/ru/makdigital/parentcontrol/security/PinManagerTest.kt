package ru.makdigital.parentcontrol.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinManagerTest {
    @Test fun pinValidationRequiresFourToEightDigits() {
        assertFalse(PinManager.isValidPin("123".toCharArray()))
        assertTrue(PinManager.isValidPin("1234".toCharArray()))
        assertTrue(PinManager.isValidPin("12345678".toCharArray()))
        assertFalse(PinManager.isValidPin("123456789".toCharArray()))
        assertFalse(PinManager.isValidPin("12a4".toCharArray()))
    }
}
