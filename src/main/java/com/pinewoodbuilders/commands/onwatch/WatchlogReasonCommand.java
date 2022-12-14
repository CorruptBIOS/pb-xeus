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

package com.pinewoodbuilders.commands.onwatch;

import com.pinewoodbuilders.Xeus;
import com.pinewoodbuilders.Constants;
import com.pinewoodbuilders.commands.CommandMessage;
import com.pinewoodbuilders.contracts.commands.Command;
import com.pinewoodbuilders.contracts.commands.CommandContext;
import com.pinewoodbuilders.contracts.commands.CommandGroup;
import com.pinewoodbuilders.contracts.commands.CommandGroups;
import com.pinewoodbuilders.database.collection.Collection;
import com.pinewoodbuilders.database.collection.DataRow;
import com.pinewoodbuilders.database.query.QueryBuilder;
import com.pinewoodbuilders.database.transformers.GuildSettingsTransformer;
import com.pinewoodbuilders.database.transformers.GuildTransformer;
import com.pinewoodbuilders.factories.MessageFactory;
import com.avairebot.shared.DiscordConstants;
import com.pinewoodbuilders.utilities.NumberUtil;
import com.pinewoodbuilders.utilities.RestActionUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WatchlogReasonCommand extends Command {

    public WatchlogReasonCommand(Xeus avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Modlog Reason Command";
    }

    @Override
    public String getDescription(@Nullable CommandContext context) {
        String prefix = context != null && context.isGuildMessage()
            ? generateCommandPrefix(context.getMessage())
            : DiscordConstants.DEFAULT_COMMAND_PREFIX;

        return String.format(
            "Sets the reason for an old modlog case, this command requires the server has a modlog channel set using the `%smodlog` command.\n%s",
            prefix, "You can only set modlog reasons for old modlog cases if you were the moderator for the case."
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <case id> <reason>` - Sets the reason for the given ID"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command 9 Advertising stuff in #general` - Sets the 9th modlog case to \"Advertising stuff in #general\""
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            WatchlogCommand.class,
            WatchlogHistoryCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("watchreason");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "isPinewoodGuild",
            "isGuildHROrHigher",
            "throttle:user,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.ON_WATCH);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildSettingsTransformer transformer = context.getGuildSettingsTransformer();
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (transformer == null || guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (transformer.getOnWatchChannel() == 0) {
            return sendErrorMessage(context, context.i18n("modlogNotEnabled"));
        }

        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "case id");
        }

        if (args.length == 1) {
            return sendErrorMessage(context, "errors.missingArgument", "reason");
        }

        int caseId = NumberUtil.parseInt(args[0], -1);
        if (caseId < 1 || caseId > guildTransformer.getOnWatchCase()) {
            return sendErrorMessage(context, context.i18n("invalidCaseId", guildTransformer.getOnWatchCase()));
        }

        final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        try {
            QueryBuilder query = avaire.getDatabase().newQueryBuilder(Constants.ON_WATCH_LOG_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .where("modlogCase", caseId);

            if (!context.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                query.where("user_id", context.getAuthor().getId());
            }

            Collection collection = query.get();

            if (collection.isEmpty()) {
                return sendErrorMessage(context, context.i18n("couldntFindCaseWithId", caseId));
            }

            DataRow first = collection.first();
            if (first.getBoolean("pardon")) {
                return sendErrorMessage(context, context.i18n("modlogCaseWasPardoned", caseId));
            }

            TextChannel channel = context.getGuild().getTextChannelById(transformer.getOnWatchChannel());
            if (channel == null) {
                return sendErrorMessage(context, context.i18n("couldntFindModlogChannel"));
            }

            avaire.getDatabase().newQueryBuilder(Constants.ON_WATCH_LOG_TABLE_NAME)
                .useAsync(true)
                .where("guild_id", context.getGuild().getId())
                .where("modlogCase", caseId)
                .update(statement -> statement.set("reason", reason, true));

            channel.retrieveMessageById(first.getString("message_id")).queue(message -> {
                if (message.getEmbeds().isEmpty()) {
                    // What? This code should never be hit...
                    return;
                }

                EmbedBuilder embeddedBuilder = MessageFactory.createEmbeddedBuilder();

                MessageEmbed embed = message.getEmbeds().get(0);

                embeddedBuilder.setTitle(embed.getTitle());
                embeddedBuilder.setDescription(embed.getDescription());
                embeddedBuilder.setColor(embed.getColor());
                embeddedBuilder.setTimestamp(embed.getTimestamp());

                embeddedBuilder.setFooter("Edited by " + context.getAuthor().getAsTag() + " (ID: " + context.getAuthor().getId() + ")", null);

                for (MessageEmbed.Field field : embed.getFields()) {
                    if (!field.getName().equalsIgnoreCase("Reason")) {
                        embeddedBuilder.addField(field);
                        continue;
                    }

                    embeddedBuilder.addField("Reason", reason, field.isInline());
                }

                message.editMessageEmbeds(embeddedBuilder.build()).queue(newMessage -> {
                    context.makeSuccess(context.i18n("success"))
                        .set("id", caseId)
                        .set("reason", reason)
                        .queue(successMessage -> successMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));
                    context.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore);
                }, error -> {
                    context.makeError(context.i18n("failedToEdit", error.getMessage()))
                        .queue(successMessage -> successMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));
                    context.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore);
                });
            }, error -> {
                context.makeWarning(context.i18n("failedToFindMessage"))
                    .queue(null, RestActionUtil.ignore);
            });
        } catch (SQLException error) {
            return sendErrorMessage(context, "Something went wrong while trying to edit the modlog message: " + error.getMessage());
        }

        return true;
    }
}
