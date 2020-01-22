package jrh

import org.apache.ignite.Ignition
import org.apache.ignite.lang.IgniteBiPredicate
import java.util.*


fun main(args: Array<String>) {
    val ig = Ignition.start(MyIgniteConfiguration[args[0]])

    val pred = IgniteBiPredicate { id: UUID, message: String ->
        println("Message received from ($id): $message")
        when (message) {
            "counter" -> ThreadJob(15, false, 1000, CounterTest(ig)).start()
        }
        true
    }
    ig.message().localListen("channel", pred)
}
