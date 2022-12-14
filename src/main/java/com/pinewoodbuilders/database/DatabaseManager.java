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

package com.pinewoodbuilders.database;

import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;
import com.pinewoodbuilders.Xeus;
import com.pinewoodbuilders.contracts.database.BatchQueryFunction;
import com.pinewoodbuilders.contracts.database.Database;
import com.pinewoodbuilders.database.collection.Collection;
import com.pinewoodbuilders.database.connections.MySQL;
import com.pinewoodbuilders.database.connections.SQLite;
import com.pinewoodbuilders.database.exceptions.DatabaseException;
import com.pinewoodbuilders.database.migrate.Migrations;
import com.pinewoodbuilders.database.query.QueryBuilder;
import com.pinewoodbuilders.database.schema.Schema;
import com.pinewoodbuilders.database.seeder.SeederManager;
import com.pinewoodbuilders.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.WillClose;
import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private final Xeus avaire;
    private final Schema schema;
    private final Migrations migrations;
    private final SeederManager seeder;

    private final AtomicInteger batchIncrementer;
    private final Set<Integer> runningBatchRequests;

    private int queryRetries = 5;
    private Database connection = null;

    public DatabaseManager(Xeus avaire) {
        this.avaire = avaire;
        this.schema = new Schema(this);
        this.migrations = new Migrations(this);
        this.seeder = new SeederManager();

        this.batchIncrementer = new AtomicInteger(0);
        this.runningBatchRequests = new HashSet<>();
    }

    public Xeus getAvaire() {
        return avaire;
    }

    public Schema getSchema() {
        return schema;
    }

    public Migrations getMigrations() {
        return migrations;
    }

    public SeederManager getSeeder() {
        return seeder;
    }

    public Database getConnection() throws SQLException, DatabaseException {
        if (connection == null) {
            switch (avaire.getConfig().getString("database.type", "invalid").toLowerCase()) {
                case "mysql":
                    connection = new MySQL(this);
                    break;

                case "sqlite":
                    connection = new SQLite(this);
                    break;

                default:
                    throw new DatabaseException("Invalid database type given, failed to create a new database connection.");
            }
        }

        if (connection.isOpen()) {
            return connection;
        }

        if (!connection.open()) {
            throw new DatabaseException("Failed to connect to the database.");
        }

        return connection;
    }

    public void setRetries(int retries) {
        this.queryRetries = retries;
    }
    public QueryBuilder newQueryBuilder() {
        return new QueryBuilder(this);
    }

    public QueryBuilder newQueryBuilder(String table) {
        return new QueryBuilder(this, table);
    }

    /**
     * Executes the given SQL statement, which returns a single
     * <code>Collection</code> object.
     *
     * @param query an SQL statement to be sent to the database, typically a
     *              static SQL <code>SELECT</code> statement
     * @return a <code>Collection</code> object that contains the data produced
     * by the given query; never <code>null</code>
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    @WillClose
    public Collection query(String query) throws SQLException {
        log.debug("query(String query) was called with the following SQL query.\nSQL: " + query);
        MDC.put("query", query);

        return runQuery(query, queryRetries);
    }

    /**
     * Executes the SQL statement generated by the query builder, which returns a single
     * <code>Collection</code> object.
     *
     * @param query a QueryBuilder instance that should be sent to the database, typically a
     *              static SQL <code>SELECT</code> statement
     * @return a <code>Collection</code> object that contains the data produced
     * by the given query; never <code>null</code>
     * @throws SQLException        if a database access error occurs,
     *                             this method is called on a closed <code>Statement</code>, the given
     *                             SQL statement produces anything other than a single
     *                             <code>ResultSet</code> object, the method is called on a
     *                             <code>PreparedStatement</code> or <code>CallableStatement</code>
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value that was specified by the {@code setQueryTimeout}
     *                             method has been exceeded and has at least attempted to cancel
     *                             the currently running {@code Statement}
     */
    @WillClose
    public Collection query(QueryBuilder query) throws SQLException {
        return query(query.toSQL());
    }

    /**
     * Generates a prepared statement object and executes the SQL statement, which must be an SQL Data
     * Manipulation Language (DML) statement, such as <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code>; or an SQL statement that returns nothing, such as a DDL statement.
     *
     * @param query an SQL statement to be sent to the database, typically a static SQL DML statement
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * or (2) 0 for SQL statements that return nothing
     * @throws SQLException        if a database access error occurs;
     *                             this method is called on a closed  <code>PreparedStatement</code>
     *                             or the SQL statement returns a <code>ResultSet</code> object
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value that was specified by the {@code setQueryTimeout}
     *                             method has been exceeded and has at least attempted to cancel
     *                             the currently running {@code Statement}
     */
    @WillClose
    public int queryUpdate(String query) throws SQLException {
        log.debug("queryUpdate(String query) was called with the following SQL query.\nSQL: " + query);
        MDC.put("query", query);

        return runQueryUpdate(query, queryRetries);
    }

    /**
     * Generates a prepared statement object and executes the SQL statement, which must be an SQL Data
     * Manipulation Language (DML) statement, such as <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code>; or an SQL statement that returns nothing, such as a DDL statement.
     *
     * @param query a QueryBuilder instance that should be sent to the database, typically a
     *              static SQL DML statement
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * or (2) 0 for SQL statements that return nothing
     * @throws SQLException        if a database access error occurs;
     *                             this method is called on a closed  <code>PreparedStatement</code>
     *                             or the SQL statement returns a <code>ResultSet</code> object
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value that was specified by the {@code setQueryTimeout}
     *                             method has been exceeded and has at least attempted to cancel
     *                             the currently running {@code Statement}
     */
    @WillClose
    public int queryUpdate(QueryBuilder query) throws SQLException {
        return queryUpdate(query.toSQL());
    }

    /**
     * Generates a prepared statement object and executes the SQL statement, which must be an SQL INSERT
     * statement, such as <code>INSERT</code>; After the query has been executed the prepared statement
     * will be used to generate a set of keys, referring to the IDs of the inserted rows.
     *
     * @param query an SQL statement to be sent to the database, typically a static SQL INSERT statement
     * @return a set of IDs referring to the insert rows
     * @throws SQLException        if a database access error occurs;
     *                             this method is called on a closed  <code>PreparedStatement</code>
     *                             or the SQL statement returns a <code>ResultSet</code> object
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value that was specified by the {@code setQueryTimeout}
     *                             method has been exceeded and has at least attempted to cancel
     *                             the currently running {@code Statement}
     */
    @WillClose
    public Set<Integer> queryInsert(String query) throws SQLException {
        if (log.isDebugEnabled()) {
            log.debug("queryInsert(String query) was called with the following SQL query.\nSQL: " + query);
        }
        Metrics.databaseQueries.labels("INSERT").inc();
        MDC.put("query", query);

        if (!query.toUpperCase().startsWith("INSERT INTO")) {
            throw new DatabaseException("queryInsert was called with a query without an INSERT statement!");
        }

        return runQueryInsert(query, queryRetries);
    }

    /**
     * Generates a prepared statement object and executes the SQL statement, which must be an SQL INSERT
     * statement, such as <code>INSERT</code>; After the query has been executed the prepared statement
     * will be used to generate a set of keys, referring to the IDs of the inserted rows.
     *
     * @param queryBuilder a QueryBuilder instance that should be sent to the database, typically a
     *                     static SQL INSERT statement
     * @return a set of IDs referring to the insert rows
     * @throws SQLException        if a database access error occurs;
     *                             this method is called on a closed  <code>PreparedStatement</code>
     *                             or the SQL statement returns a <code>ResultSet</code> object
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value that was specified by the {@code setQueryTimeout}
     *                             method has been exceeded and has at least attempted to cancel
     *                             the currently running {@code Statement}
     */
    @WillClose
    public Set<Integer> queryInsert(QueryBuilder queryBuilder) throws SQLException {
        String query = queryBuilder.toSQL();
        log.debug("queryInsert(QueryBuilder queryBuilder) was called with the following SQL query.\nSQL: " + query);
        Metrics.databaseQueries.labels("INSERT").inc();
        MDC.put("query", query);

        if (query == null) {
            throw new SQLException("null query was generated, null can not be used as a valid query");
        }

        if (!query.toUpperCase().startsWith("INSERT INTO")) {
            throw new DatabaseException("queryInsert was called with a query without an INSERT statement!");
        }

        return runQueryInsert(queryBuilder, queryRetries);
    }

    /**
     * Creates a batch request query, creating a prepared statement from the given query, and sets
     * up a batch request which is then invoked at the end of the {@code queryFunction}, the
     * {@code queryFunction} should only add batches to the prepared statement, the actual
     * executing of the batch request, committing the query, and rolling back in case of
     * errors is all done by the queryBatch method.
     * <p>
     * <strong>Example:</strong>
     * <pre><code>
     * // The IDs that will be used within for the "condition" in the SQL query.
     * List<Integer> ids = Arrays.asList(1, 3, 5, 7, 9);
     *
     * databaseManager.queryBatch("UPDATE `some_table` SET `something` = true WHERE `condition` = ?", statement -> {
     *     for (int id : ids) {
     *         // Adds the ID to the first question mark(?)
     *         statement.setInt(1, id);
     *
     *         // Adds the finished statement to the batch request queue.
     *         statement.addBatch();
     *     }
     * });
     * // The batch query will automatically be executed and committed at this point.
     * </code></pre>
     *
     * @param query         The query that should be used for the batch request.
     * @param queryFunction The function that should be called for setting up the batch request.
     * @throws SQLException        if a database access error occurs;
     *                             this method is called on a closed  <code>PreparedStatement</code>
     *                             or the SQL statement returns a <code>ResultSet</code> object
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value that was specified by the {@code setQueryTimeout}
     *                             method has been exceeded and has at least attempted to cancel
     *                             the currently running {@code Statement}
     */
    public void queryBatch(String query, BatchQueryFunction<PreparedStatement> queryFunction) throws SQLException {
        runQueryBatch(query, queryFunction, batchIncrementer.getAndIncrement(), queryRetries);
    }

    /**
     * Checks if there are any running batch query requests running right now.
     *
     * @return {@code True} if there are batch requests running, {@code False} otherwise.
     */
    public boolean hasRunningBatchQueries() {
        return !runningBatchRequests.isEmpty();
    }

    @WillClose
    private Collection runQuery(String query, int retriesLeft) throws SQLException {
        try (ResultSet resultSet = getConnection().query(query)) {
            return new Collection(resultSet);
        } catch (MySQLTransactionRollbackException e) {
            if (--retriesLeft > 0) {
                return runQuery(query, --retriesLeft);
            }
            throw new MySQLTransactionRollbackException(
                e.getMessage(), e.getSQLState(), e.getErrorCode()
            );
        }
    }

    @WillClose
    private int runQueryUpdate(String query, int retriesLeft) throws SQLException {
        try (Statement stmt = getConnection().prepare(query)) {
            if (stmt instanceof PreparedStatement) {
                return ((PreparedStatement) stmt).executeUpdate();
            }

            return stmt.executeUpdate(query);
        } catch (MySQLTransactionRollbackException e) {
            if (--retriesLeft > 0) {
                return runQueryUpdate(query, retriesLeft);
            }
            throw new MySQLTransactionRollbackException(
                e.getMessage(), e.getSQLState(), e.getErrorCode()
            );
        }
    }

    @WillClose
    private Set<Integer> runQueryInsert(String query, int retriesLeft) throws SQLException {
        try (PreparedStatement stmt = getConnection().getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();

            Set<Integer> ids = new HashSet<>();

            ResultSet keys = stmt.getGeneratedKeys();
            while (keys.next()) {
                ids.add(keys.getInt(1));
            }

            return ids;
        } catch (MySQLTransactionRollbackException e) {
            if (--retriesLeft > 0) {
                return runQueryInsert(query, retriesLeft);
            }
            throw new MySQLTransactionRollbackException(
                e.getMessage(), e.getSQLState(), e.getErrorCode()
            );
        }
    }

    @WillClose
    private Set<Integer> runQueryInsert(QueryBuilder queryBuilder, int retriesLeft) throws SQLException {
        String query = queryBuilder.toSQL();

        try (PreparedStatement stmt = getConnection().getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            int preparedIndex = 1;
            for (Map<String, Object> row : queryBuilder.getItems()) {
                for (Map.Entry<String, Object> item : row.entrySet()) {
                    if (item.getValue() == null) {
                        continue;
                    }

                    String value = item.getValue().toString();

                    if (value.startsWith("RAW:") ||
                        value.equalsIgnoreCase("true") ||
                        value.equalsIgnoreCase("false") ||
                        value.matches("[-+]?\\d*\\.?\\d+")) {
                        continue;
                    }

                    stmt.setString(preparedIndex++, value);
                }
            }

            stmt.executeUpdate();

            Set<Integer> ids = new HashSet<>();

            ResultSet keys = stmt.getGeneratedKeys();
            while (keys.next()) {
                ids.add(keys.getInt(1));
            }

            return ids;
        } catch (MySQLTransactionRollbackException e) {
            if (--retriesLeft > 0) {
                return runQueryInsert(query, retriesLeft);
            }
            throw new MySQLTransactionRollbackException(
                e.getMessage(), e.getSQLState(), e.getErrorCode()
            );
        }
    }

    private void runQueryBatch(String query, BatchQueryFunction<PreparedStatement> queryFunction, int batchId, int retriesLeft) throws SQLException {
        log.debug("Running batch query with the following values:\n - Query: {}\n - Batch ID: {}\n - Retries Left: {}",
            query, batchId, retriesLeft
        );

        Connection connection = getConnection().getConnection();

        runningBatchRequests.add(batchId);

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                queryFunction.run(preparedStatement);

                preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            log.error("An SQL exception was thrown while running a batch query: {}", query, e);

            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.error("An SQL exception was thrown while attempting to rollback a batch query: {}", query, e);
            }
        } finally {
            try {
                connection.commit();

                runningBatchRequests.remove(batchId);
                if (runningBatchRequests.isEmpty()) {
                    connection.setAutoCommit(true);
                }
            } catch (MySQLTransactionRollbackException e) {
                if (--retriesLeft > 0) {
                    runQueryBatch(query, queryFunction, batchId, retriesLeft);
                }
            }
        }
    }
}
