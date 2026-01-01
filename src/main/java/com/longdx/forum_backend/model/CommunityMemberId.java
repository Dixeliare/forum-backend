package com.longdx.forum_backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityMemberId implements Serializable {

    private Long community;
    private Long user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunityMemberId that = (CommunityMemberId) o;
        return Objects.equals(community, that.community) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(community, user);
    }
}

