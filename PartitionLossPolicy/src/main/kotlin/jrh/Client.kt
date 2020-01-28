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

private fun serializeValue(ignite: Ignite, param2: String, param3: String): BinaryObject {
    return ignite.binary().builder("ValueType")
            .setField("param2", param2)
            .setField("param3", param3)
            .build()
}

private fun insertData(ignite: Ignite) {
    val cache = ignite.getOrCreateCache(getCacheConfig("TestCache"))

    println(String.format("JRH: LostData = %s", cache.lostPartitions().toList()))
    println("Populating the cache...")
//    for (i in 0..9999L) {
    for (i in 0..2L) {
//        cache.putIfAbsent(i, serializeValue(ignite, "param2-$i", "param3-$i"))
        cache.putIfAbsent(i, serializeValue(ignite, "param2-2-$i", "param3-2-$i"))

        if (i > 0 && i % 1000L == 0L)
            println("Done: $i")
    }
}

private fun <K, V> cacheDrillDown(ignite: Ignite, cacheName: String) {
    val cache = ignite.cache<K, V>(cacheName).withKeepBinary<K, V>()
    val affinity = ignite.affinity<K>(cacheName)
    val keys = cache.map { it.key }
    keys.forEach { println("[JRH] Entry: $it [${affinity.partition(it)}] -> ${affinity.mapKeyToPrimaryAndBackups(it).map { it.consistentId() }}") }

    println("[JRH] Total Entries = ${keys.size}")
    println("[JRH] LostData: ${cache.lostPartitions()}")
//        val nodes = affinity.mapKeysToNodes(keys).map { it.key.consistentId() }
//    val nodes = affinity.mapKeyToPrimaryAndBackups(singleKey).map { it.consistentId() }
//    println("[JRH] Nodes with data: $nodes")
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
//        activeGrid(ignite)
        insertData(ignite)
//        printPartitions(ignite)
//        resetLostPartition(ignite, cacheName)
        cacheDrillDown<Long, BinaryObject>(ignite, cacheName)
        getValue<Long, BinaryObject>(ignite, cacheName,2L)
//        println("Configuration: ${getCacheConfig("TestCache")}")
    }
}
