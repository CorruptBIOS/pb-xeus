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

package com.pinewoodbuilders.moderation.mute;

import com.pinewoodbuilders.time.Carbon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ScheduledFuture;

@SuppressWarnings("WeakerAccess")
public class MuteContainer {

    private final long guildId;
    private final long userId;
    private final Carbon expiresAt;
    private final long mgi;
    private final boolean global;
    private ScheduledFuture<?> schedule;


    /**
     * Creates a mute container using the given guild ID, user ID, and expiration time.
     *
     * @param guildId   The ID of the guild the mute is registered to.
     * @param userId    The ID of the user the mute is registered for.
     * @param expiresAt The date and time the mute should expire,
     *                  or {@code NULL} for permanent mutes.
     */
    public MuteContainer(long guildId, long userId, @Nullable Carbon expiresAt) {
        this.guildId = guildId;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.schedule = null;
        this.global = false;
        this.mgi = 0;
    }

    /**
     * Creates a mute container using the given guild ID, user ID, and expiration time.
     *
     * @param guildId   The ID of the guild the mute is registered to.
     * @param userId    The ID of the user the mute is registered for.
     * @param expiresAt The date and time the mute should expire,
     *                  or {@code NULL} for permanent mutes.
     * @param global    Whether the mute is global or not.
     */
    public MuteContainer(long guildId, long userId, @Nullable Carbon expiresAt, boolean global) {
        this.guildId = guildId;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.schedule = null;
        this.global = global;
        this.mgi = 0;
    }

    /**
     * Creates a mute container using the given guild ID, user ID, and expiration time.
     *
     * @param guildId   The ID of the guild the mute is registered to.
     * @param userId    The ID of the user the mute is registered for.
     * @param expiresAt The date and time the mute should expire,
     *                  or {@code NULL} for permanent mutes.
     * @param global    Whether the mute is global or not.
     */
    public MuteContainer(long guildId, long userId, @Nullable Carbon expiresAt, boolean global, long mgi) {
        this.guildId = guildId;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.schedule = null;
        this.global = global;
        this.mgi = mgi;
    }

    /**
     * Gets the ID of the guild the mute is registered for.
     *
     * @return The guild ID the mute is registered for.
     */
    public long getGuildId() {
        return guildId;
    }

    /**
     * Gets the ID of the user the mute is registered for.
     *
     * @return The user ID the mute is registered for.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Gets the date and time the mute should automatically expire,
     * or {@code NULL} if the mute is permanent.
     *
     * @return The carbon instance for when the mute should end, or {@code NULL}.
     */
    @Nullable
    public Carbon getExpiresAt() {
        return expiresAt;
    }

    /**
     * Gets the future scheduled task for the mute, this task is used
     * to automatically unmute a user if there is 5 minutes or less
     * left of their temporary mute.
     * <p>
     * If this value is {@code NULL} the automatic mute task haven't
     * yet been started for the mute container.
     *
     * @return The future scheduled task used to auto unmute the container, or {@code NULL}.
     */
    @Nullable
    public ScheduledFuture<?> getSchedule() {
        return schedule;
    }

    /**
     * Sets the future scheduled task that should automatically unmute the container.
     *
     * @param schedule The future task used to unmute the container.
     */
    public void setSchedule(@Nonnull ScheduledFuture<?> schedule) {
        this.schedule = schedule;
    }

    /**
     * Cancels the future scheduled task used to automatically
     * unmute the container if one has been started.
     */
    public void cancelSchedule() {
        if (schedule != null) {
            schedule.cancel(false);
            schedule = null;
        }
    }

    /**
     * Checks if the registered mute is permanent or temporary.
     *
     * @return {@code True} if the mute is permanent, {@code False} otherwise.
     */
    public boolean isPermanent() {
        return getExpiresAt() == null;
    }

    /**
     * Compares the current and given container, checking if
     * they're registered under the same guild and user IDs.
     *
     * @param container The container that should be compared.
     * @return {@code True} if the containers match, {@code False} otherwise.
     */
    public boolean isSame(@Nonnull MuteContainer container) {
        return isSame(container.getGuildId(), container.getUserId());
    }

    /**
     * Compares the current container with the given guild and user IDs,
     * checking if the current container is registered to the same
     * guild and user IDs given.
     *
     * @param guildId The guild ID that should be compared.
     * @param userId  The user ID that should be compared.
     * @return {@code True} if the IDs match, {@code False} otherwise.
     */
    public boolean isSame(long guildId, long userId) {
        return getGuildId() == guildId
            && getUserId() == userId;
    }

    public boolean isGlobalSame(long userId, long mgi) {
        return getUserId() == userId
            && getMgi() == mgi;
    }



    /**
     * Compares the current container with the given guild and user IDs,
     * checking if the current container is registered to the same
     * guild and user IDs given.
     *
     * @param userId  The user ID that should be compared.
     * @return {@code True} if the IDs match, {@code False} otherwise.
     */
    public boolean isSame(long userId) {
        return getUserId() == userId;
    }

    /**
     * returns if the mute is global or not. If it's global, it will apply in all guilds that are connected to the MGI
     *
     * @return {@code True} if the mute is global, {@code False} otherwise.
     * @since 3.0.5
     */
    public boolean isGlobal() {
        return global;
    }

    public long getMgi() {
        return mgi;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof MuteContainer && isSame((MuteContainer) obj);
    }

    @Override
    public String toString() {
        return String.format("MuteContainer={guildId=%s, userId=%s, expiresAt=%s}",
            getGuildId(), getUserId(), getExpiresAt()
        );
    }
}
