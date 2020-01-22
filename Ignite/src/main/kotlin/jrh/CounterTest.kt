package jrh

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteAtomicSequence
import org.apache.ignite.configuration.AtomicConfiguration

class CounterTest internal constructor(private val ignite: Ignite) : Runnable {

    override fun run() {
        val sequence = createSequence()
        println("SequenceValue = " + sequence.incrementAndGet())
    }

    private fun createSequence(): IgniteAtomicSequence {
        return ignite.atomicSequence("Sequence",0, true)
    }
}
