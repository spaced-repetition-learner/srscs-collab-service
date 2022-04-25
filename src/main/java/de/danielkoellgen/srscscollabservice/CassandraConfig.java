package de.danielkoellgen.srscscollabservice;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator;
import org.springframework.data.cassandra.core.cql.session.init.ResourceKeyspacePopulator;

import java.util.List;

public class CassandraConfig extends AbstractCassandraConfiguration {

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {

        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace("collab_service")
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withSimpleReplication(1);
        return List.of(specification);
    }

    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return List.of(DropKeyspaceSpecification.dropKeyspace("collab_service"));
    }

    @Nullable
    @Override
    protected KeyspacePopulator keyspacePopulator() {
        return new ResourceKeyspacePopulator(scriptOf("CREATE TABLE user_by_id (\n" +
                "    user_id UUID,\n" +
                "    username TEXT,\n" +
                "    is_active BOOLEAN,\n" +
                "    PRIMARY KEY ( user_id )\n" +
                ");"));
    }

    @Nullable
    @Override
    protected KeyspacePopulator keyspaceCleaner() {
        return new ResourceKeyspacePopulator(scriptOf("DROP TABLE my_table;"));
    }

    @Override
    protected @NotNull String getKeyspaceName() {
        return "collab_service";
    }
}
