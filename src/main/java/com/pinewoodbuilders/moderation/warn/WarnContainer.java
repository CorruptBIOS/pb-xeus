/*
 * Copyright (c) 2019.
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

package com.pinewoodbuilders.moderation.warn;

import com.pinewoodbuilders.time.Carbon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

@SuppressWarnings("WeakerAccess")
public class WarnContainer {

    private final long guildId;
    private final long userId;
    private final Carbon expiresAt;
    private final String caseId;
    private ScheduledFuture<?> schedule;

    /**
     * Creates a ban container using the given guild ID, user ID, and expiration time.
     *  @param guildId   The ID of the guild the ban is registered to.
     * @param userId    The ID of the user the ban is registered for.
     * @param expiresAt The date and time the ban should expire,
 *                  or {@code NULL} for permanent bans.
     * @param caseId
     */
    public WarnContainer(long guildId, long userId, @Nullable Carbon expiresAt, String caseId) {
        this.guildId = guildId;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.schedule = null;
        this.caseId = caseId;
    }

    /**
     * Gets the ID of the guild the ban is registered for.
     *
     * @return The guild ID the ban is registered for.
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * Gets the ID of the user the ban is registered for.
     *
     * @return The user ID the ban is registered for.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Gets the date and time the ban should automatically expire,
     * or {@code NULL} if the ban is permanent.
     *
     * @return The carbon instance for when the ban should end, or {@code NULL}.
     */
    @Nullable
    public Carbon getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the future scheduled task for the ban, this task is used
     * to automatically unban a user if there is 5 minutes or less
     * left of their temporary ban.
     * <p>
     * If this value is {@code NULL} the automatic ban task haven't
     * yet been started for the ban container.
     *
     * @return The future scheduled task used to auto unban the container, or {@code NULL}.
     */
    @Nullable
    public ScheduledFuture<?> getSchedule() {
        return schedule;
    }

    /**
     * Sets the future scheduled task that should automatically unban the container.
     *
     * @param schedule The future task used to unban the container.
     */
    public void setSchedule(@Nonnull ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }

    /**
     * Cancels the future scheduled task used to automatically
     * unban the container if one has been started.
     */
    public void cancelSchedule() {
        if (schedule != null) {
            schedule.cancel(false);
            schedule = null;
        }
    }

    /**
     * Checks if the registered ban is permanent or temporary.
     *
     * @return {@code True} if the ban is permanent, {@code False} otherwise.
     */
    public boolean isPermanent() {
        return getExpiresAt() == null;
    }

    /**
     * Get the
     * @return
     */
    public String getCaseId() {
        return caseId;
    }

    /**
     * Compares the current and given container, checking if
     * they're registered under the same guild and user IDs.
     *
     * @param container The container that should be compared.
     * @return {@code True} if the containers match, {@code False} otherwise.
     */
    public boolean isSame(@Nonnull WarnContainer container) {
        return isSame(container.getGuildId(), container.getUserId(), container.getCaseId());
    }

    /**
     * Compares the current container with the given guild and user IDs,
     * checking if the current container is registered to the same
     * guild and user IDs given.
     *
     * @param guildId The guild ID that should be compared.
     * @param userId  The user ID that should be compared.
     * @param caseId  The case ID that should be compaired
     * @return {@code True} if the IDs match, {@code False} otherwise.
     */
    public boolean isSame(long guildId, long userId, String caseId) {
        return getGuildId() == guildId
            && getUserId() == userId
            && Objects.equals(getCaseId(), caseId);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof WarnContainer && isSame((WarnContainer) obj);
    }

    @Override
    public String toString() {
        return String.format("WarnContainer={guildId=%s, userId=%s, expiresAt=%s, caseId=%s}",
            getGuildId(), getUserId(), getExpiresAt(), getCaseId()
        );
    }
}
