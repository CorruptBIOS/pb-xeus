/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.contracts.commands;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.commands.CommandPriority;
import com.avairebot.contracts.commands.interactions.Lottery;
import com.avairebot.language.I18n;
import com.avairebot.utilities.CacheUtil;
import com.avairebot.utilities.MentionableUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class InteractionCommand extends Command {

    public static final Cache<String, Lottery> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    private final String interaction;
    private final boolean overwrite;

    public InteractionCommand(AvaIre avaire, String interaction, boolean overwrite) {
        super(avaire, false);

        this.overwrite = overwrite;
        this.interaction = interaction;
    }

    public InteractionCommand(AvaIre avaire, String interaction) {
        this(avaire, interaction, false);
    }

    public InteractionCommand(AvaIre avaire) {
        this(avaire, null, false);
    }

    @Override
    public String getDescription(CommandContext context) {
        return String.format(
            "Sends the **%s** interaction to the mentioned user.",
            getInteraction(context, true)
        );
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <user>`");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @AvaIre`");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,1,5");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIGH;
    }

    public Color getInteractionColor() {
        return null;
    }

    public abstract List<String> getInteractionImages();

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        User user = MentionableUtil.getUser(context, new String[]{String.join(" ", args)});
        if (user == null) {
            return sendErrorMessage(context, "You must mention a use you want to use the interaction for.");
        }

        context.getChannel().sendTyping().queue();

        List<String> interactionImages = getInteractionImages();

        Lottery lottery = (Lottery) CacheUtil.getUncheckedUnwrapped(cache, asKey(context),
            () -> new Lottery(interactionImages.size())
        );

        int imageIndex = lottery.getWinner();

        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder
            .setImage("attachment://" + getClass().getSimpleName() + "-" + imageIndex + ".gif")
            .setDescription(buildMessage(context, user))
            .setColor(getInteractionColor());

        messageBuilder.setEmbed(embedBuilder.build());

        try {
            InputStream stream = new URL(interactionImages.get(imageIndex)).openStream();

            context.getChannel().sendFile(stream, getClass().getSimpleName() + "-" + imageIndex + ".gif", messageBuilder.build()).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String buildMessage(CommandMessage context, User user) {
        if (overwrite) {
            return I18n.format(
                getInteraction(context, false),
                context.getMember().getEffectiveName(),
                context.getGuild().getMember(user).getEffectiveName()
            );
        }

        return String.format("**%s** %s **%s**",
            context.getMember().getEffectiveName(),
            getInteraction(context, false),
            context.getGuild().getMember(user).getEffectiveName()
        );
    }

    private String getInteraction(CommandContext context, boolean isDescription) {
        if (isDescription && overwrite) {
            return getTriggers().get(0);
        }
        return interaction == null ? context.i18nRaw(context.getI18nCommandPrefix()) : interaction;
    }

    private String asKey(CommandContext context) {
        return getClass().getSimpleName() + "." + context.getGuild().getIdLong();
    }
}
