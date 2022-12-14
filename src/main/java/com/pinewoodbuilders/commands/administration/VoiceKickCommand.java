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

package com.pinewoodbuilders.commands.administration;

import com.pinewoodbuilders.Xeus;
import com.pinewoodbuilders.commands.CommandMessage;
import com.pinewoodbuilders.contracts.commands.CacheFingerprint;
import com.pinewoodbuilders.contracts.commands.Command;
import com.pinewoodbuilders.contracts.commands.CommandGroup;
import com.pinewoodbuilders.contracts.commands.CommandGroups;
import com.pinewoodbuilders.modlog.local.moderation.Modlog;
import com.pinewoodbuilders.modlog.local.shared.ModlogAction;
import com.pinewoodbuilders.modlog.local.shared.ModlogType;
import com.pinewoodbuilders.utilities.MentionableUtil;
import com.pinewoodbuilders.utilities.RestActionUtil;
import com.pinewoodbuilders.utilities.RoleUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "kick-command")
public class VoiceKickCommand extends Command {

    public VoiceKickCommand(Xeus avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Voice Kick Command";
    }

    @Override
    public String getDescription() {
        return "Kicks the mentioned user from the voice channel they're currently connected to, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List <String> getUsageInstructions() {
        return Collections.singletonList("`:command <user> [reason]` - Kicks the mentioned user with the given reason.");
    }

    @Override
    public List <String> getExampleUsage() {
        return Collections.singletonList("`:command @Senither Yelling at people`");
    }

    @Override
    public List <Class <? extends Command>> getRelations() {
        return Collections.singletonList(KickCommand.class);
    }

    @Override
    public List <String> getTriggers() {
        return Arrays.asList("voicekick", "vkick");
    }

    @Override
    public List <String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.kick_members",
            "require:bot,general.manage_channels,voice.move_members",
            "throttle:user,1,5"
        );
    }

    @Nonnull
    @Override
    public List <CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MODERATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = MentionableUtil.getUser(context, args);
        if (user == null) {
            return sendErrorMessage(context, context.i18n("mustMentionUser"));
        }

        if (userHasHigherRole(user, context.getMember())) {
            return sendErrorMessage(context, context.i18n("sameOrHigherRole"));
        }

        final Member member = context.getGuild().getMember(user);
        if (!member.getVoiceState().inAudioChannel()) {
            return sendErrorMessage(context, context.i18n("notConnected"));
        }

        return kickUser(context, member, args);
    }

    private boolean kickUser(CommandMessage context, Member user, String[] args) {
        String reason = generateMessage(args);
        String voiceChannelName = user.getVoiceState().getChannel().getName();
        String voiceChannelId = user.getVoiceState().getChannel().getId();

        context.getGuild().kickVoiceMember(user)
            .queue(empty ->
                Modlog.log(avaire, context, new ModlogAction(
                    ModlogType.VOICE_KICK,
                    context.getAuthor(), user.getUser(),
                    voiceChannelName + " (ID: " + voiceChannelId + ")\n" + reason
                )));

        context.makeSuccess(context.i18n("message"))
            .set("target", user.getUser().getName() + "#" + user.getUser().getDiscriminator())
            .set("voiceChannel", voiceChannelName)
            .set("reason", reason)
            .queue(ignoreMessage -> context.delete().queue(null, RestActionUtil.ignore));
        return true;
    }

    private boolean userHasHigherRole(User user, Member author) {
        Role role = RoleUtil.getHighestFrom(author.getGuild().getMember(user));
        return role != null && RoleUtil.isRoleHierarchyHigher(author.getRoles(), role);
    }

    private String generateMessage(String[] args) {
        return args.length < 2 ?
            "No reason was given." :
            String.join(" ", Arrays.copyOfRange(args, 1, args.length));
    }
}
