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

package com.pinewoodbuilders.database.migrate.migrations;

import com.pinewoodbuilders.Constants;
import com.pinewoodbuilders.contracts.database.migrations.Migration;
import com.pinewoodbuilders.database.schema.Schema;

import java.sql.SQLException;

public class CreateAppealTicketsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Sat, Dec 14, 2022 9:50 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.APPEAL_TICKETS_TABLE_NAME, table -> {
            table.Long("channel_id");
            table.String("user_id");
            table.Integer("type");
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.APPEAL_TICKETS_TABLE_NAME);
    }
}
