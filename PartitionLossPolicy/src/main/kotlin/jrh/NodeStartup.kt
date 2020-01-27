package jrh

import org.apache.ignite.Ignition
import org.apache.ignite.binary.BinaryObject


fun main(args: Array<String>) {
    Ignition.start(MyIgniteConfiguration[args[0]])
}
