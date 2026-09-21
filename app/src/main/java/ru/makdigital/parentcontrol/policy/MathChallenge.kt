package ru.makdigital.parentcontrol.policy

import kotlin.random.Random

object MathChallenge {
    data class Problem(val a: Int, val b: Int) {
        val answer: Int get() = a + b
    }

    fun generate(random: Random = Random.Default): Problem =
        Problem(random.nextInt(1, 11), random.nextInt(1, 11))

    fun isCorrect(problem: Problem, submitted: Int): Boolean = submitted == problem.answer
}
