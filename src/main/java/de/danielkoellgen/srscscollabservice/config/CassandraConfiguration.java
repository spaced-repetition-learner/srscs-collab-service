package de.danielkoellgen.srscscollabservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

@Configuration
public class CassandraConfiguration extends AbstractCassandraConfiguration {

    private final String address;

    private final String keySpace;

    @Autowired
    public CassandraConfiguration(
            @Value("${cassandra.address}") String address,
            @Value("${spring.data.cassandra.keyspace-name}") String keySpace) {
        this.address = address;
        this.keySpace = keySpace;
    }

    /*
     * Provide a contact point to the configuration.
     */
    public String getContactPoints() {
        return address;
    }

    /*
     * Provide a keyspace name to the configuration.
     */
    public String getKeyspaceName() {
        return keySpace;
    }
}
