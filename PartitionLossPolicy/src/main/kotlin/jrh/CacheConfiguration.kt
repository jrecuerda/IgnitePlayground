package jrh

import org.apache.ignite.cache.*
import org.apache.ignite.configuration.CacheConfiguration

fun getCacheConfig(cacheName: String): CacheConfiguration<Long, Long> {
    return CacheConfiguration<Long, Long>(cacheName).apply {
        isStatisticsEnabled = false

        atomicityMode = CacheAtomicityMode.ATOMIC
        cacheMode = CacheMode.PARTITIONED
        partitionLossPolicy = PartitionLossPolicy.READ_WRITE_SAFE
        backups = 1
        isOnheapCacheEnabled = true

        dataRegionName = "persistent"
    }
}
