package com.avairebot.roblox.user;

import com.avairebot.AvaIre;
import com.avairebot.roblox.RobloxAPIManager;
import com.avairebot.roblox.user.general.GeneralUserAPIManager;
import com.avairebot.roblox.user.v1.UserAPIv1Manager;
import com.avairebot.roblox.user.v2.UserAPIv2Manager;

public class RobloxUserAPIManager {

    private final RobloxAPIManager robloxAPIManager;
    private final AvaIre avaire;
    private final UserAPIv1Manager v1;
    private final UserAPIv2Manager v2;
    private final GeneralUserAPIManager general;

    public RobloxUserAPIManager(RobloxAPIManager robloxAPIManager, AvaIre avaire) {
        this.robloxAPIManager = robloxAPIManager;
        this.avaire = avaire;
        this.general = new GeneralUserAPIManager(this, avaire, robloxAPIManager);
        this.v1 = new UserAPIv1Manager();
        this.v2 = new UserAPIv2Manager();
    }

    public UserAPIv1Manager getV1() {
        return v1;
    }

    public UserAPIv2Manager getV2() {
        return v2;
    }

}
