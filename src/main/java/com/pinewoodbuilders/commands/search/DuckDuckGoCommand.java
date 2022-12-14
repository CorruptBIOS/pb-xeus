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

package com.pinewoodbuilders.commands.search;

import com.pinewoodbuilders.Xeus;
import com.pinewoodbuilders.chat.MessageType;
import com.pinewoodbuilders.chat.PlaceholderMessage;
import com.pinewoodbuilders.commands.CommandMessage;
import com.pinewoodbuilders.contracts.commands.Command;
import com.pinewoodbuilders.factories.MessageFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.*;

public class DuckDuckGoCommand extends Command {

    private static final Map<String, String> HTTP_HEADERS = new HashMap<>();

    static {
        HTTP_HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.128 Safari/537.36");
        HTTP_HEADERS.put("Accept-Language", "en-US,en;q=0.9,en-GB;q=0.8,da;q=0.7");
        HTTP_HEADERS.put("Cache-Control", "no-cache, no-store, must-revalidate");
        HTTP_HEADERS.put("Pragma", "no-cache");
        HTTP_HEADERS.put("Expires", "0");
    }

    public DuckDuckGoCommand(Xeus avaire) {
        super(avaire);
    }

    @Override
    public String getName() {
        return "DuckDuckGo Command";
    }

    @Override
    public String getDescription() {
        return "Searches [DuckDuckGo.com](https://duckduckgo.com/) with the given query and returns the first six results, if the command is used in a channel with NSFW disabled, all NSFW search results will be removed from the results.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <query>` - Searchs DuckDuckGo for your query.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command Xeus Bot`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("duckduckgo", "ddg", "g");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "query");
        }

        try {
            context.getMessageChannel().sendTyping().queue();

            boolean nsfwEnabled = isNSFWEnabled(context);
            Document document = Jsoup.connect(generateUri(args, nsfwEnabled))
                .headers(new HashMap<>(HTTP_HEADERS))
                .timeout(10000)
                .get();

            int results = 0;
            List<String> result = new ArrayList<>();
            Elements elements = document.select("div#links div.result");
            for (Element element : elements) {
                Elements link = element.select("h2.result__title a");
                if (isAdvertisement(link)) {
                    continue;
                }

                if (results == 0) {
                    result.add(prepareLinkElement(link) + "\n" + context.i18n("seeAlso"));
                    results++;
                    continue;
                }

                result.add(prepareLinkElement(link));
                results++;

                if (results > 5) {
                    break;
                }
            }

            PlaceholderMessage resultMessage = MessageFactory.makeEmbeddedMessage(context.getMessageChannel(), Color.decode("#DE5833"))
                .setDescription(String.join("\n", result))
                .setTitle(context.i18n("searchResults", String.join(" ", args)));

            if (result.isEmpty() || (result.size() == 1 && result.get(0).startsWith("-1&uddg"))) {
                resultMessage
                    .setColor(MessageType.WARNING.getColor())
                    .setDescription(context.i18n("noResults"))
                    .set("query", String.join(" ", args));
            }

            resultMessage.queue();

            return true;
        } catch (IOException e) {
            Xeus.getLogger().error("ERROR: ", e);
            Xeus.getLogger().error("Failed to complete search query: ", e);
        }
        return false;
    }

    private boolean isAdvertisement(Elements link) {
        return link.attr("href").contains("duckduckgo.com/y.js") ||
            link.attr("href").contains("duckduckgo.com%2Fy.js") ||
            link.attr("rel").startsWith("noopener");
    }

    private String prepareLinkElement(Elements link) throws UnsupportedEncodingException {
        String[] parts = link.attr("href").split("=");

        return URLDecoder.decode(parts[parts.length - 1], "UTF-8");
    }

    private String generateUri(String[] args, boolean isNSFWEnabled) throws UnsupportedEncodingException {
        return "https://html.duckduckgo.com/html/?q=" + URLEncoder.encode(
            removeBangs(String.join(" ", args)), "UTF-8"
        ) + "&kp=" + (isNSFWEnabled ? "-2" : "1");
    }

    private String removeBangs(String text) {
        if (!Objects.equals(text.substring(0, 1), "!")) {
            return text;
        }
        return removeBangs(text.substring(1));
    }

    private boolean isNSFWEnabled(CommandMessage message) {
        return !message.isGuildMessage();
    }
}
