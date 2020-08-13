package com.github.deityexe;

import com.github.deityexe.event.GuildEvent;
import com.github.deityexe.event.EventFactory;
import com.github.deityexe.util.BotConfig;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;


public class Main {
    /**
     * Class logger.
     */
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("entering main");
        final String prefix = "!";
        final String token = BotConfig.getInstance().getBotToken();

        if (token == null) {
            System.out.println("No token specified in \"bot.properties\"! Please add a token to continue.");
            return;
        }

        logger.info("loading stored events");
        Map<UUID, GuildEvent> guildEvents = (new EventFactory()).findAllEvents();

        logger.info("connecting to discord");
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        logger.info("create and connect listeners");
        api.addMessageCreateListener(new Commands(api, prefix, guildEvents));

        api.addReactionAddListener(event -> {
            try {
                logger.info(String.format("reaction added on message"));
                for (GuildEvent guildEvent : guildEvents.values()) {
                    guildEvent.dispatchEvent(event, api);
                }
            } catch (DeliverableError error) {
                event.getUser().sendMessage(error.toUserMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        api.addReactionRemoveListener(event -> {
            try {
                logger.info(String.format("reaction removed on message"));
                for (GuildEvent guildEvent : guildEvents.values()) {
                    guildEvent.dispatchEvent(event, api);
                }
            } catch (DeliverableError error) {
                event.getUser().sendMessage(error.toUserMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
