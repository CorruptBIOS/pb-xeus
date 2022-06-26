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

package com.pinewoodbuilders.handlers;

import com.pinewoodbuilders.database.transformers.GuildSettingsTransformer;
import com.pinewoodbuilders.database.transformers.GuildTransformer;
import com.pinewoodbuilders.database.transformers.VerificationTransformer;

import javax.annotation.Nullable;

public record DatabaseEventHolder(GuildTransformer guild,
                                  VerificationTransformer verification,
                                  GuildSettingsTransformer guildSettings) {

    /**
     * Gets the guild database transformer for the current JDA event, the guild
     * transformer can be used to pull specific bot server settings and information
     * about the current guild.
     *
     * @return The guild database transformer for the current JDA event, or
     * {@code NULL} if the event was not invoked for a guild.
     */
    public GuildTransformer getGuild() {
        return guild;
    }


    @Nullable
    public VerificationTransformer getVerification() {
        return verification;
    }

    @Nullable
    public GuildSettingsTransformer getGuildSettings() {
        return guildSettings;
    }
}
