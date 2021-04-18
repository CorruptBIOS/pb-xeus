package com.avairebot.roblox.group;

import com.avairebot.AvaIre;
import com.avairebot.roblox.RobloxAPIManager;
import com.avairebot.roblox.group.v1.GroupAPIv1Manager;
import com.avairebot.roblox.group.v2.GroupAPIv2Manager;
import okhttp3.OkHttpClient;

public class RobloxGroupAPIManager {

    private final GroupAPIv1Manager v1;
    private final GroupAPIv2Manager v2;

    public RobloxGroupAPIManager(RobloxAPIManager robloxAPIManager, AvaIre avaire, OkHttpClient client) {
        this.v1 = new GroupAPIv1Manager(this, avaire, robloxAPIManager);
        this.v2 = new GroupAPIv2Manager(this, avaire, robloxAPIManager);
    }

    public GroupAPIv1Manager getV1() {
        return v1;
    }

    @Deprecated
    public GroupAPIv2Manager getV2() {
        return v2;
    }
}
