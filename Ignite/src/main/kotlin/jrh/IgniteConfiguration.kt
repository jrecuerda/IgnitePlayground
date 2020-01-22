package jrh

import org.apache.ignite.configuration.DataRegionConfiguration
import org.apache.ignite.configuration.DataStorageConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder

object MyIgniteConfiguration {
    internal operator fun get(workdir: String) = IgniteConfiguration().apply {
        consistentId = workdir
        workDirectory = "./ignite-tests/$workdir"

        dataStorageConfiguration = DataStorageConfiguration().apply {
            defaultDataRegionConfiguration = DataRegionConfiguration().apply {
                isPersistenceEnabled = true
            }
            val persistentDataRegion = DataRegionConfiguration().apply {
                isPersistenceEnabled = true
                name = "persistent"
            }
            setDataRegionConfigurations(persistentDataRegion)
        }

        discoverySpi = TcpDiscoverySpi().apply {
            ipFinder = TcpDiscoveryMulticastIpFinder().apply {
                setAddresses(listOf("127.0.0.1:47500..47502"))
            }
        }
    }
}
