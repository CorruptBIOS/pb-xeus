package com.avairebot.roblox.group.v1.service;

import java.util.Date;
import java.util.List;

public class RobloxUserGroupRankService {

    public List<Data> data;
    public List<Data> getData() {
        return data;
    }
    public boolean hasData() {
        return getData() != null && !getData().isEmpty();
    }
    public class Data {
        private Group group;
        private Role role;

        public Group getGroup() {
            return group;
        }

        public Role getRole() {
            return role;
        }

        public class Owner {
            public String getBuildersClubMembershipType() {
                return buildersClubMembershipType;
            }

            public long getUserId() {
                return userId;
            }

            public String getUsername() {
                return username;
            }

            public String getDisplayName() {
                return displayName;
            }

            public String buildersClubMembershipType;
            public long userId;
            public String username;
            public String displayName;
        }

        public class Poster {
            public String getBuildersClubMembershipType() {
                return buildersClubMembershipType;
            }

            public long getUserId() {
                return userId;
            }

            public String getUsername() {
                return username;
            }

            public String getDisplayName() {
                return displayName;
            }

            public String buildersClubMembershipType;
            public long userId;
            public String username;
            public String displayName;
        }

        public class Shout {
            public String body;
            public Poster poster;
            public Date created;
            public Date updated;

            public String getBody() {
                return body;
            }

            public Poster getPoster() {
                return poster;
            }

            public Date getCreated() {
                return created;
            }

            public Date getUpdated() {
                return updated;
            }
        }

        public class Group {
            public int id;
            public String name;
            public String description;
            public Owner owner;
            public Shout shout;
            public long memberCount;
            public boolean isBuildersClubOnly;
            public boolean publicEntryAllowed;

            public long getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public String getDescription() {
                return description;
            }

            public Owner getOwner() {
                return owner;
            }

            public Shout getShout() {
                return shout;
            }

            public long getMemberCount() {
                return memberCount;
            }

            public boolean isBuildersClubOnly() {
                return isBuildersClubOnly;
            }

            public boolean isPublicEntryAllowed() {
                return publicEntryAllowed;
            }
        }

        public class Role {
            public int id;
            public String name;
            public int rank;
            public long memberCount;

            public int getId() {
                return id;
            }

            public String getName() {
                return name;
            }

            public int getRank() {
                return rank;
            }

            public long getMemberCount() {
                return memberCount;
            }
        }

        public class Datum {
            public Group group;
            public Role role;
            public boolean isPrimaryGroup;

            public Group getGroup() {
                return group;
            }

            public Role getRole() {
                return role;
            }

            public boolean isPrimaryGroup() {
                return isPrimaryGroup;
            }
        }

    }
}
