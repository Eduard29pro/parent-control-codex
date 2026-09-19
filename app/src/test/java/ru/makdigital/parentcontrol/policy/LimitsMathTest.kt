package ru.makdigital.parentcontrol.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class LimitsMathTest {
    @Test fun brightnessBoundaries() {
        assertEquals(3, LimitsMath.brightnessValueFromPercent(1))
        assertEquals(102, LimitsMath.brightnessValueFromPercent(40))
        assertEquals(255, LimitsMath.brightnessValueFromPercent(100))
    }

    @Test fun volumeBoundaries() {
        assertEquals(0, LimitsMath.volumeStepsFromPercent(0, 15))
        assertEquals(5, LimitsMath.volumeStepsFromPercent(30, 15))
        assertEquals(15, LimitsMath.volumeStepsFromPercent(100, 15))
    }

    @Test fun valuesAreCoerced() {
        assertEquals(1, LimitsMath.brightnessValueFromPercent(-10))
        assertEquals(15, LimitsMath.volumeStepsFromPercent(120, 15))
    }
}
