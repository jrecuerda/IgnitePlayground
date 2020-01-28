package jrh

import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.binary.BinaryObject

private fun activeGrid(ignite: Ignite) {
    if (ignite.cluster().active()) {
        println("JRH: Cluster was already active")
    }
    ignite.cluster().active(true)
    println("JRH: Cluster is active")
}

private fun printPartitions(ignite: Ignite) {
    val cache = ignite.cache<Long, BinaryObject>("TestCache")
    val affinity = ignite.affinity<Long>("TestCache")
    println("LOST PARTITION = ${cache.lostPartitions()}")
    ignite.cluster().forServers().nodes().forEach {
        println("NODE: ${it.consistentId()} -> ${affinity.allPartitions(it).toList()}")
    }
}

private fun insertData(ignite: Ignite) {
    val cache = ignite.getOrCreateCache(getCacheConfig("TestCache"))

    println("Populating the cache...")
    cache.putIfAbsent(0, 1)
    cache.putIfAbsent(1, 2)
    cache.putIfAbsent(2, 3)
}

private fun insertData2(ignite: Ignite) {
    val cache = ignite.getOrCreateCache(getCacheConfig("TestCache"))

    println("Populating the cache...")
    cache.putIfAbsent(0, 10)
    cache.putIfAbsent(1, 20)
    cache.putIfAbsent(2, 30)
}

private fun <K, V> cacheDrillDown(ignite: Ignite, cacheName: String) {
    val cache = ignite.cache<K, V>(cacheName).withKeepBinary<K, V>()
    val affinity = ignite.affinity<K>(cacheName)
    val keys = cache.map { it.key }
    cache.forEach { println("[JRH] Entry: KEY=${it.key} PARTITION=${affinity.partition(it.key)} VALUE=${it.value} NODES=${affinity.mapKeyToPrimaryAndBackups(it.key).map { it.consistentId() }}") }

    println("[JRH] Total Entries = ${keys.size}")
    println("[JRH] LostData: ${cache.lostPartitions()}")
}

private fun <K, V> getValue(ignite: Ignite, cacheName: String, key: K) {
    val cache = ignite.cache<K, V>(cacheName).withKeepBinary<K, V>()
    val value = cache.get(key)
    println("[JRH] Value: $value")
}

private fun resetLostPartition(ignite: Ignite, cacheName: String) {
    ignite.resetLostPartitions(listOf(cacheName))
}

fun main() {
    Ignition.setClientMode(true)

    println("Configuration: ${igniteConfiguration("CLIENT")}")

    Ignition.start(igniteConfiguration("CLIENT")).use { ignite ->
        val cacheName = "TestCache"

        ///// STEP 1
//        activeGrid(ignite)
//        insertData(ignite)
//        printPartitions(ignite)
//        cacheDrillDown<Long, BinaryObject>(ignite, cacheName)
//        getValue<Long, BinaryObject>(ignite, cacheName,2L)

        //// STEP 2
        // Get PID of the nodes storing the partition of the key 2
        // kill -9 NODEx NODEy
//        cacheDrillDown<Long, BinaryObject>(ignite, cacheName)
//        getValue<Long, BinaryObject>(ignite, cacheName,2L)

        //// STEP 3
//        insertData2(ignite)
//        cacheDrillDown<Long, BinaryObject>(ignite, cacheName)
//        getValue<Long, BinaryObject>(ignite, cacheName,2L)

        //// STEP 4
        // Turn on NODEx
//        cacheDrillDown<Long, BinaryObject>(ignite, cacheName)
//        getValue<Long, BinaryObject>(ignite, cacheName,2L)

        //// STEP 5
//        resetLostPartition(ignite, cacheName)
//        cacheDrillDown<Long, BinaryObject>(ignite, cacheName)
//        getValue<Long, BinaryObject>(ignite, cacheName,2L)

    }
}
