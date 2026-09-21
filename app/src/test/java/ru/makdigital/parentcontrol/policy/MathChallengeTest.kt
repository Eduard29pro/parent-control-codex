package ru.makdigital.parentcontrol.policy

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MathChallengeTest {
    @Test fun operandsAreWithinOneToTen() {
        val random = Random(42)
        repeat(500) {
            val problem = MathChallenge.generate(random)
            assertTrue(problem.a in 1..10)
            assertTrue(problem.b in 1..10)
        }
    }

    @Test fun answerIsSum() {
        assertEquals(7, MathChallenge.Problem(3, 4).answer)
    }

    @Test fun isCorrectMatchesAnswer() {
        val problem = MathChallenge.Problem(2, 5)
        assertTrue(MathChallenge.isCorrect(problem, 7))
        assertFalse(MathChallenge.isCorrect(problem, 8))
    }
}
