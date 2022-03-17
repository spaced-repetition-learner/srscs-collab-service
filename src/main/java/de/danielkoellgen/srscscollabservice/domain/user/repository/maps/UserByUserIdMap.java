package de.danielkoellgen.srscscollabservice.domain.user.repository.maps;

import de.danielkoellgen.srscscollabservice.domain.domainprimitives.Username;
import de.danielkoellgen.srscscollabservice.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Embedded;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table(value = "users_by_userid")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserByUserIdMap {

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @Column("username")
    private String username;

    @Column("is_active")
    private Boolean isActive;


    public UserByUserIdMap(User user) {
        userId = user.getUserId();
        username = user.getUsername().getUsername();
        isActive = user.getIsActive();
    }

    public User mapToUser() throws Exception {
        return new User(userId, new Username(username), isActive);
    }
}
