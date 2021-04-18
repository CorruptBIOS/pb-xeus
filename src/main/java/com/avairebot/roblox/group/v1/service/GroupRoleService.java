package com.avairebot.roblox.group.v1.service;

import java.util.List;

public class GroupRoleService {
    public int groupId;
    public List<Role> roles;

    public int getGroupId() {
        return groupId;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public boolean hasRoles() {
        return getRoles() != null && !getRoles().isEmpty();
    }

    public class Role{
        private int id;
        private String name;
        private int rank;
        private int memberCount;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getRank() {
            return rank;
        }

        public int getMemberCount() {
            return memberCount;
        }
    }



    public final String url;
    public GroupRoleService() {
        this.url = "https://groups.roblox.com/v1/groups/:groupId/roles";
    }

    public String getUrl() {
        return url;
    }
}
