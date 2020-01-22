package jrh

import org.apache.ignite.Ignition
import org.apache.ignite.events.EventAdapter
import org.apache.ignite.events.EventType


fun main() {
    Ignition.setClientMode(true)

    Ignition.start(MyIgniteConfiguration.get("CLIENT")).use { ignite ->
        ignite.cluster().active(true)
        ignite.message().sendOrdered("channel", "counter", 0)
    }
}
