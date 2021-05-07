package com.avairebot.roblox.user.general;

import com.avairebot.AvaIre;
import com.avairebot.roblox.RobloxAPIManager;
import com.avairebot.roblox.group.v1.service.GroupRoleService;
import com.avairebot.roblox.user.RobloxUserAPIManager;
import com.avairebot.roblox.user.general.service.RoVerVerificationService;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class GeneralUserAPIManager {
    private final RobloxAPIManager apiManager;
    private final okhttp3.Request.Builder builder;

    public GeneralUserAPIManager(RobloxUserAPIManager groupManager, AvaIre avaire, RobloxAPIManager apiManager) {
        this.apiManager = apiManager;
        this.builder = new okhttp3.Request.Builder();
    }

    public RoVerVerificationService getRoverUserId(String userId) {
        Request request = builder.url("https://verify.eryn.io/api/user/:userId".replace(":userId", userId)).build();

        try (Response response = apiManager.getClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    return (RoVerVerificationService) apiManager.toService(response.body().string(), GroupRoleService.class);
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

}
