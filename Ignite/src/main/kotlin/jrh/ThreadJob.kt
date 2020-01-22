package jrh

import java.util.Random
import kotlin.math.abs

class ThreadJob internal constructor(
    private val maxExecutions: Int,
    private val runForever: Boolean,
    private val maxTimeIntervalMillis: Long,
    private val func: Runnable
) : Thread() {
    private val random: Random = Random()

    override fun run() {
        println("Begin ThreadJob")
        var numExecutions = -1
        while (true) {
            if (!runForever && numExecutions++ > maxExecutions) {
                break
            }
            try {
                sleep(abs(random.nextLong()) % maxTimeIntervalMillis)
                func.run()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }
}
