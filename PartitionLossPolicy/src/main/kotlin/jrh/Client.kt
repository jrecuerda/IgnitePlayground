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

    println("[JRH] Populating the cache...")
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
    println("[JRH] Reset lost partitions of: $cacheName")
}

private fun setTopologyToCurrentServers(ignite: Ignite) {
    println("[JRH] Previous Baseline topology ${ignite.cluster().currentBaselineTopology()!!.map { it.consistentId() }}")
    ignite.cluster().setBaselineTopology(ignite.cluster().forRemotes().forServers().nodes())
    println("[JRH] New Baseline topology ${ignite.cluster().currentBaselineTopology()!!.map { it.consistentId() }}")
    println("[JRH] servers ${ignite.cluster().forRemotes().forServers().nodes()!!.map { it.consistentId() }}")
    println("[JRH] alive ${ignite.cluster().topology(ignite.cluster().topologyVersion())!!.map { it.consistentId() }}")
}

private fun <K, V> cacheRebalance(ignite: Ignite, cacheName: String) {
    ignite.cache<K, V>(cacheName).rebalance()
    println("[JRH] New Baseline topology $cacheName")
}

fun main() {
    Ignition.setClientMode(true)

    println("Configuration: ${igniteConfiguration("CLIENT")}")

    Ignition.start(igniteConfiguration("CLIENT")).use { ignite ->
        val cacheName = "TestCache"

        ///// STEP 1
        activeGrid(ignite)
        insertData(ignite)
        printPartitions(ignite)
        cacheDrillDown<Long, Long>(ignite, cacheName)
        getValue<Long, Long>(ignite, cacheName,2L)

        //// STEP 2
        // Get PID of the nodes storing the partition of the key 2 and kill one of them
        // kill -9 NODEx
//        cacheDrillDown<Long, Long>(ignite, cacheName)
//        getValue<Long, Long>(ignite, cacheName,2L)

        //// STEP 3
        // Notice that it is necessary to se this client as a server instead of client in order to modify the baseline topology
//        printPartitions(ignite)
//        setTopologyToCurrentServers(ignite)
//        cacheDrillDown<Long, Long>(ignite, cacheName)
//        getValue<Long, Long>(ignite, cacheName, 2L)

    }
}
