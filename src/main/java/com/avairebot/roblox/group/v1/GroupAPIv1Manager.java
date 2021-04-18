package com.avairebot.roblox.group.v1;

import com.avairebot.AvaIre;
import com.avairebot.roblox.RobloxAPIManager;
import com.avairebot.roblox.group.RobloxGroupAPIManager;
import com.avairebot.roblox.group.v1.service.GroupRoleService;
import com.avairebot.roblox.group.v1.service.RobloxUserGroupRankService;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class GroupAPIv1Manager {

    public final RobloxUserGroupRankService v1Group;
    private final RobloxAPIManager apiManager;
    private final okhttp3.Request.Builder builder;

    public GroupAPIv1Manager(RobloxGroupAPIManager groupManager, AvaIre avaire, RobloxAPIManager apiManager) {
        this.v1Group = new RobloxUserGroupRankService();
        this.apiManager = apiManager;
        this.builder = new okhttp3.Request.Builder();
    }

    public GroupRoleService getGroupRoles(String groupId) {
        Request request = builder.url("https://groups.roblox.com/v1/groups/:groupId/roles".replace(":groupId", groupId)).build();

        try (Response response = apiManager.getClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    return (GroupRoleService) apiManager.toService(response.body().string(), GroupRoleService.class);
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public RobloxUserGroupRankService getUserGroupRoles(String userId) {
        Request request = builder.url("https://groups.roblox.com/v1/groups/:userId/roles".replace(":userId", userId)).build();

        try (Response response = apiManager.getClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    return (RobloxUserGroupRankService) apiManager.toService(response.body().string(), GroupRoleService.class);
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }


}
