package com.avairebot.roblox;

import com.avairebot.AvaIre;
import com.avairebot.roblox.group.RobloxGroupAPIManager;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobloxAPIManager {
    private final Logger log = LoggerFactory.getLogger(RobloxAPIManager.class);
    private final OkHttpClient client = new OkHttpClient();
    private final RobloxGroupAPIManager robloxGroupAPIManager;

    private final AvaIre avaire;
    private String cookie = null;
    private String cfrsToken = null;

    public RobloxAPIManager(AvaIre avaire) {
        this.avaire = avaire;
        this.robloxGroupAPIManager = new RobloxGroupAPIManager(this, avaire, client);
    }

    public RobloxGroupAPIManager getGroupApiManager() {
        return robloxGroupAPIManager;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public Logger getLog() {
        return log;
    }

    public Object toService(String body, Class<?> clazz) {
        return AvaIre.gson.fromJson(body, clazz);
    }

}
