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

package com.pinewoodbuilders.middleware;

import com.pinewoodbuilders.Xeus;
import com.pinewoodbuilders.commands.CommandContainer;
import com.pinewoodbuilders.contracts.commands.Command;
import com.pinewoodbuilders.contracts.middleware.Middleware;
import com.pinewoodbuilders.handlers.DatabaseEventHolder;
import com.pinewoodbuilders.metrics.Metrics;
import com.pinewoodbuilders.middleware.global.IncrementMetricsForCommand;
import com.pinewoodbuilders.middleware.global.IsCategoryEnabled;
import com.pinewoodbuilders.middleware.global.LogModeratorCommand;
import com.pinewoodbuilders.middleware.global.ProcessCommand;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MiddlewareStack {

    private static ProcessCommand processCommand;
    private static IsCategoryEnabled isCategoryEnabled;
    private static IncrementMetricsForCommand incrementMetricsForCommand;
    private static LogModeratorCommand logModeratorCommand;

    private final Message message;
    private final CommandContainer command;
    private final List<MiddlewareContainer> middlewares = new ArrayList<>();
    private final DatabaseEventHolder databaseEventHolder;
    private final boolean mentionableCommand;

    private int index = -1;

    public MiddlewareStack(Message message, CommandContainer command, DatabaseEventHolder databaseEventHolder, boolean mentionableCommand) {
        this.message = message;
        this.command = command;
        this.mentionableCommand = mentionableCommand;
        this.databaseEventHolder = databaseEventHolder;

        middlewares.add(new MiddlewareContainer(processCommand));

        buildMiddlewareStack();

        middlewares.add(new MiddlewareContainer(isCategoryEnabled));
        middlewares.add(new MiddlewareContainer(incrementMetricsForCommand));
        middlewares.add(new MiddlewareContainer(logModeratorCommand));

        Metrics.commandAttempts.labels(command.getClass().getSimpleName()).inc();
    }

    public MiddlewareStack(Message message, CommandContainer command, DatabaseEventHolder databaseEventHolder) {
        this(message, command, databaseEventHolder, false);
    }

    /**
     * Builds the global messages so they can be used when building the middleware stack.
     *
     * @param avaire The Xeus application instance.
     */
    static void buildGlobalMiddlewares(Xeus avaire) {
        processCommand = new ProcessCommand(avaire);
        isCategoryEnabled = new IsCategoryEnabled(avaire);
        incrementMetricsForCommand = new IncrementMetricsForCommand(avaire);
        logModeratorCommand = new LogModeratorCommand(avaire);
    }

    /**
     * Builds the middleware stack from the commands {@link Command#getMiddleware() getMiddleware()} method.
     */
    private void buildMiddlewareStack() {
        List<String> middleware = command.getMiddleware();
        if (middleware.isEmpty()) {
            return;
        }

        ListIterator <String> middlewareIterator = middleware.listIterator(middleware.size());
        while (middlewareIterator.hasPrevious()) {
            String previous = middlewareIterator.previous();
            String[] split = previous.split(":");

            Middleware middlewareReference = MiddlewareHandler.getMiddleware(split[0]);
            if (middlewareReference == null) {
                continue;
            }

            
            if (split.length == 1) {  
                middlewares.add(new MiddlewareContainer(middlewareReference));
                continue;
            }
            middlewares.add(new MiddlewareContainer(middlewareReference, split[1].split(",")));
        }
        //middlewares.forEach(action -> System.out.println(action.getMiddleware().getClass().getSimpleName()));
    }

    /**
     * Jumps to the next middleware in the stack, the end of the stack should
     * always be the {@link ProcessCommand Process Command} middleware in
     * order for the command to be invoked.
     *
     * @return <code>True</code> if the next middleware in the stack executed successfully, <code>False</code> otherwise.
     */
    public boolean next() {
        if (index == -1) {
            index = middlewares.size();
        }

        MiddlewareContainer middlewareContainer = middlewares.get(--index);

        return middlewareContainer
            .getMiddleware()
            .handle(message, this, middlewareContainer.getArguments());
    }

    /**
     * Gets the {@link Command command} the middleware stack is running for.
     *
     * @return The {@link Command command} the middleware stack is running for.
     */
    public Command getCommand() {
        return command.getCommand();
    }

    /**
     * Gets the {@link CommandContainer command container} the middleware stack is running for.
     *
     * @return the {@link CommandContainer command container} the middleware stack is running for.
     */
    public CommandContainer getCommandContainer() {
        return command;
    }

    /**
     * Returns <code>True</code> if the command was invoked through mentioning the bot first.
     *
     * @return <code>True</code> if the command was invoked through mentioning the bot first.
     */
    public boolean isMentionableCommand() {
        return mentionableCommand;
    }

    /**
     * Returns the {@link DatabaseEventHolder database event holder} object for the given
     * message, the object can be used to get the database record for the guild or user.
     *
     * @return The {@link DatabaseEventHolder database event holder} object.
     */
    public DatabaseEventHolder getDatabaseEventHolder() {
        return databaseEventHolder;
    }
}
