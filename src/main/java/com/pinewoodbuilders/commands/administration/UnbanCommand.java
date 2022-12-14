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
import com.pinewoodbuilders.Constants;
import com.pinewoodbuilders.commands.CommandMessage;
import com.pinewoodbuilders.contracts.commands.CacheFingerprint;
import com.pinewoodbuilders.contracts.commands.Command;
import com.pinewoodbuilders.contracts.commands.CommandGroup;
import com.pinewoodbuilders.contracts.commands.CommandGroups;
import com.pinewoodbuilders.database.collection.Collection;
import com.pinewoodbuilders.modlog.local.moderation.Modlog;
import com.pinewoodbuilders.modlog.local.shared.ModlogAction;
import com.pinewoodbuilders.modlog.local.shared.ModlogType;
import com.pinewoodbuilders.utilities.NumberUtil;
import com.pinewoodbuilders.utilities.RestActionUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "banable-user-command")
public class UnbanCommand extends Command {

    public UnbanCommand(Xeus avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Unban Command";
    }

    @Override
    public String getDescription() {
        return "Unbans the user with the given ID from the server if they are banned, if a modlog channel is setup, the unban will be logged to the channel as well.";
    }

    @Override
    public List <String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <user id> [reason]` - Unbans the user with given ID and for the given reason."
        );
    }

    @Override
    public List <String> getExampleUsage() {
        return Collections.singletonList("`:command 88739639380172800 Wasn't actually a twat`");
    }

    @Override
    public List <Class <? extends Command>> getRelations() {
        return Arrays.asList(
            SoftBanCommand.class,
            BanCommand.class
        );
    }

    @Override
    public List <String> getTriggers() {
        return Collections.singletonList("unban");
    }

    @Override
    public List <String> getMiddleware() {
        return Arrays.asList(
            "isGuildHROrHigher",
            "require:bot,general.ban_members",
            "throttle:user,1,4"
        );
    }

    @Nonnull
    @Override
    public List <CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MODERATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "user id");
        }

        if (!NumberUtil.isNumeric(args[0]) || args[0].length() < 17) {
            return sendErrorMessage(context, context.i18n("invalidUserIdGiven"));
        }

        try {
            Collection c = avaire.getDatabase().newQueryBuilder(Constants.ANTI_UNBAN_TABLE_NAME).where("userId", args[0]).get();
            if (c.size() > 0) {
                context.makeError("This user is globally banned by a MGM Moderator, meaning you're not allowed to unban them thought this command. Please use the global unban command as a MGM member to revert this ban!").set("userId", args[0]).queue();
                return false;
            }
        } catch (SQLException ignored) {
            //
        }

        try {
            context.getGuild().retrieveBan(UserSnowflake.fromId(args[0])).queue(ban -> {
                handleBan(context, ban, Arrays.copyOfRange(args, 1, args.length));
            }, error -> {
                context.makeWarning(context.i18n("noUserIsBanned"))
                    .set("id", args[0])
                    .queue();
            });
        } catch (NumberFormatException e) {
            return sendErrorMessage(context, context.i18n("invalidUserIdGiven"));
        }

        return true;
    }

    private void handleBan(CommandMessage context, Guild.Ban ban, String[] args) {
        String reason = args.length == 0
            ? "No reason was given."
            : String.join(" ", args);

        context.getGuild().unban(ban.getUser()).queue(aVoid -> {
            ModlogAction modlogAction = new ModlogAction(
                ModlogType.UNBAN, context.getAuthor(), ban.getUser(), reason
            );

            Modlog.log(avaire, context, modlogAction);

            context.makeSuccess(context.i18n("success"))
                .set("target", ban.getUser().getName() + "#" + ban.getUser().getDiscriminator())
                .set("reason", reason)
                .queue(ignoreMessage -> context.delete().queue(null, RestActionUtil.ignore));
        }, throwable -> context.makeWarning(context.i18n("failedToUnban"))
            .set("target", ban.getUser().getName() + "#" + ban.getUser().getDiscriminator())
            .set("error", throwable.getMessage())
            .queue());
    }
}
