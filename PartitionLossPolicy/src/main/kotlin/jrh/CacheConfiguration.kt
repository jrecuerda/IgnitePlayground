package jrh

import org.apache.ignite.binary.BinaryObject
import org.apache.ignite.cache.*
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction
import org.apache.ignite.configuration.CacheConfiguration

fun getCacheConfig(cacheName: String): CacheConfiguration<Long, BinaryObject> {
    return CacheConfiguration<Long, BinaryObject>(cacheName).apply {
        isStatisticsEnabled = true

        atomicityMode = CacheAtomicityMode.ATOMIC
        cacheMode = CacheMode.PARTITIONED
        partitionLossPolicy = PartitionLossPolicy.READ_WRITE_SAFE
        backups = 1
        isOnheapCacheEnabled = true

        dataRegionName = "ephemeral"

        affinity = RendezvousAffinityFunction(true, 20)

        queryEntities = listOf(
                QueryEntity().apply {
                    tableName = "$cacheName-table"
                    sqlSchema = "SqlSchema"

                    fields = LinkedHashMap()
                    fields["param1"] = Long::class.java.name
                    fields["param2"] = String::class.java.name
                    fields["param3"] = String::class.java.name

                    keyFieldName = "param1"

                    valueType = "ValueType"

                    indexes = arrayListOf(
                            QueryIndex("param1", QueryIndexType.SORTED),
                            QueryIndex(arrayListOf("param2", "param3"), QueryIndexType.FULLTEXT)
                    )
                }
        )
    }
}
