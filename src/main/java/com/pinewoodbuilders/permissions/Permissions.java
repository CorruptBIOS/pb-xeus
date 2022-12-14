/*
 * Copyright (c) 2018.
 *
 * This file is part of Xeus.
 *
 * Xeus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xeus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Xeus.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.pinewoodbuilders.permissions;

import net.dv8tion.jda.api.Permission;

public enum Permissions {
    // General guild permissions
    ADMINISTRATOR("general.administrator", Permission.ADMINISTRATOR),
    MANAGE_ROLES("general.manage_roles", Permission.MANAGE_ROLES),
    MANAGE_SERVER("general.manage_server", Permission.MANAGE_SERVER),
    MANAGE_CHANNEL("general.manage_channels", Permission.MANAGE_CHANNEL),
    KICK_MEMBERS("general.kick_members", Permission.KICK_MEMBERS),
    BAN_MEMBERS("general.ban_members", Permission.BAN_MEMBERS),
    CREATE_INSTANT_INVITE("general.create_instant_invite", Permission.CREATE_INSTANT_INVITE),
    NICKNAME_MANAGE("general.manage_nickname", Permission.NICKNAME_MANAGE),
    NICKNAME_CHANGE("general.change_nickname", Permission.NICKNAME_CHANGE),
    MANAGE_EMOTES_AND_STICKERS("general.manage_emojis", Permission.MANAGE_EMOJIS_AND_STICKERS),
    MANAGE_WEBHOOKS("general.manage_webhooks", Permission.MANAGE_WEBHOOKS),
    VIEW_AUDIT_LOGS("general.view_audit_logs", Permission.VIEW_AUDIT_LOGS),

    // Text permissions,
    VIEW_CHANNEL("text.read_messages", Permission.VIEW_CHANNEL),
    MESSAGE_SEND("text.send_messages", Permission.MESSAGE_SEND),
    MESSAGE_TTS("text.send_tts_messages", Permission.MESSAGE_TTS),
    MESSAGE_MANAGE("text.manage_messages", Permission.MESSAGE_MANAGE),
    MESSAGE_EMBED_LINKS("text.embed_links", Permission.MESSAGE_EMBED_LINKS),
    MESSAGE_ATTACH_FILES("text.attach_files", Permission.MESSAGE_ATTACH_FILES),
    MESSAGE_HISTORY("text.read_message_history", Permission.MESSAGE_HISTORY),
    MESSAGE_MENTION_EVERYONE("text.mention_everyone", Permission.MESSAGE_MENTION_EVERYONE),
    MESSAGE_EXT_EMOJI("text.external_emotes", Permission.MESSAGE_EXT_EMOJI),
    MESSAGE_ADD_REACTION("text.add_reactions", Permission.MESSAGE_ADD_REACTION),

    // Voice permissions,
    VOICE_CONNECT("voice.connect", Permission.VOICE_CONNECT),
    VOICE_SPEAK("voice.speak", Permission.VOICE_SPEAK),
    VOICE_MUTE_OTHERS("voice.mute_members", Permission.VOICE_MUTE_OTHERS),
    VOICE_MOVE_OTHERS("voice.move_members", Permission.VOICE_MOVE_OTHERS),
    VOICE_DEAF_OTHERS("voice.deafen_members", Permission.VOICE_DEAF_OTHERS),
    VOICE_USE_VAD("voice.use_vad", Permission.VOICE_USE_VAD),
    VOICE_PRIORITY_SPEAKER("voice.priority_speaker", Permission.PRIORITY_SPEAKER);

    private final String node;
    private final Permission permission;

    /**
     * Creates a new permissions instance using the given
     * node and JDA permission instance.
     *
     * @param node       The stringified permission node representing the permission.
     * @param permission The JDA permission instance used for the permission.
     */
    Permissions(String node, Permission permission) {
        this.node = node;
        this.permission = permission;
    }

    /**
     * Gets the permission instance that matches the
     * given stringified permission node.
     *
     * @param node The node that should be matched against the permissions.
     * @return The permission that matched with the given stringified permission node.
     */
    public static Permissions fromNode(String node) {
        for (Permissions permission : values()) {
            if (permission.getNode().equalsIgnoreCase(node)) {
                return permission;
            }
        }
        return null;
    }

    /**
     * Gets the string node used to represent
     * the permission in middlewares.
     *
     * @return The stringified representation of the permission.
     */
    public String getNode() {
        return node;
    }

    /**
     * Gets the JDA permission instance.
     *
     * @return The JDA permission instance.
     */
    public Permission getPermission() {
        return permission;
    }
}
