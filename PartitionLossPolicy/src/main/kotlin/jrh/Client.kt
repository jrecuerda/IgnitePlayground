package jrh

import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.binary.BinaryObject

private fun activeGrid(ignite: Ignite) {
    if(ignite.cluster().active()) {
        println("JRH: Cluster was already active")
    }
    ignite.cluster().active(true)
    println("JRH: Cluster is active")
}

private fun setTopology(ignite: Ignite) { ignite.cluster().setBaselineTopology(ignite.cluster().forServers().nodes())
    println("JRH: BaselineTopology = ${ignite.cluster().currentBaselineTopology()!!.map { it.consistentId() }}")
}


private fun printPartitions(ignite: Ignite) {
    val cache = ignite.cache<Long, Long>("TestCache")
    val affinity = ignite.affinity<Long>("TestCache")
    println("LOST PARTITION = ${cache.lostPartitions()}")
    ignite.cluster().forServers().nodes().forEach {
        println("NODE: ${it.consistentId()} -> ${affinity.allPartitions(it).toList()}" )
    }
}

private fun insertData(ignite: Ignite) {
    val cacheCfg = getCacheConfig("TestCache")
    var cache = ignite.getOrCreateCache(cacheCfg)

    println(String.format("JRH: LostData = %s", cache.lostPartitions().toList()))
    println("Populating the cache...")
    for (i in 0..9999L) {
        cache.put(i, i+1)

        if (i > 0 && i % 1000L == 0L)
            println("Done: $i")
    }
}

fun main() {
    Ignition.setClientMode(true)

    Ignition.start(MyIgniteConfiguration.get("CLIENT")).use { ignite ->
        activeGrid(ignite)
//        setTopology(ignite)
        insertData(ignite)
        printPartitions(ignite)
    }
}
