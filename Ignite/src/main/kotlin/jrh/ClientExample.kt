package jrh

import org.apache.ignite.IgniteBinary
import org.apache.ignite.Ignition
import org.apache.ignite.binary.BinaryObject
import org.apache.ignite.cache.*
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction
import org.apache.ignite.cache.query.SqlFieldsQuery
import org.apache.ignite.configuration.CacheConfiguration
import java.util.*

private val UPDATE = true

fun getCacheConfig(cacheName: String): CacheConfiguration<BinaryObject, BinaryObject> {
    val cacheCfg = CacheConfiguration<BinaryObject, BinaryObject>(cacheName)
    cacheCfg.isStatisticsEnabled = true

    cacheCfg.atomicityMode = CacheAtomicityMode.ATOMIC
    cacheCfg.cacheMode = CacheMode.PARTITIONED
    cacheCfg.partitionLossPolicy = PartitionLossPolicy.READ_ONLY_SAFE
    cacheCfg.backups = 1
    cacheCfg.isOnheapCacheEnabled = true
    cacheCfg.setSqlSchema("Shapelets")
    cacheCfg.affinity = RendezvousAffinityFunction(true, 20)

    val fields = LinkedHashMap<String, String>()
    fields["param1"] = String::class.java.name
    fields["param2"] = String::class.java.name
    fields["param3"] = Long::class.java.name
    fields["param4"] = Double::class.java.name
    fields["param5"] = String::class.java.name

    val keyFields = HashSet<String>()
    keyFields.add("param2")
    keyFields.add("param3")

    val indexes = ArrayList<QueryIndex>()
    val indexFields = ArrayList<String>()
    indexFields.add("param1")
    indexFields.add("param2")
    indexes.add(QueryIndex(indexFields, QueryIndexType.FULLTEXT))

    val queryEntity = QueryEntity()
    queryEntity
        .setTableName(cacheName + "table")
        .setKeyType("KeyType")
        .setValueType("ValueType")
        .setFields(fields)
        .setKeyFields(keyFields)
        .setIndexes(indexes)

    val queryEntities = ArrayList<QueryEntity>()
    queryEntities.add(queryEntity)
    cacheCfg.queryEntities = queryEntities

    return cacheCfg
}

fun serializeKey(binary: IgniteBinary, param2: String, param3: Long?): BinaryObject {
    val builder = binary.builder("KeyType")
    builder.setField("param2", param2)
    builder.setField("param3", param3)
    return builder.build()
}

fun serializeValue(binary: IgniteBinary, param1: String, param4: Double?, param5: String): BinaryObject {
    val builder = binary.builder("ValueType")
    builder.setField("param1", param1)
    builder.setField("param4", param4)
    builder.setField("param5", param5)
    return builder.build()
}

/**
 * @param args Program arguments, ignored.
 * @throws Exception If failed.
 */
fun main() {
    Ignition.setClientMode(true)

    Ignition.start(MyIgniteConfiguration.get("CLIENT-EXAMPLE")).use { ignite ->
        // Activate the cluster. Required to do if the persistent store is enabled because you might need
        // to wait while all the nodes, that store a subset of data on disk, join the cluster.
        ignite.active(true)
        val binary = ignite.binary()

        val cacheCfg = getCacheConfig("TestCache")
        var cache = ignite.getOrCreateCache(cacheCfg).withKeepBinary<BinaryObject, BinaryObject>()
        cache = cache.withPartitionRecover()

        println(String.format("JRH: LostData = %s", cache.lostPartitions().size))

        if (UPDATE) {
            println("Populating the cache...")

            for (i in 0..9999L) {
                cache.put(
                    serializeKey(binary, "param2$i", i),
                    serializeValue(binary, "param1$i", i.toDouble(), "param5$i")
                )

                if (i > 0 && i % 1000L == 0L)
                    println("Done: $i")
            }
        }

        val cur = cache.query(
            SqlFieldsQuery("select param2, param5 from TestCachetable")
        )

        println("SQL Result: " + cur.getAll().size)
    }
}
