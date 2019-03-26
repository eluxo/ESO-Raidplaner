package com.github.deityexe;

import com.github.deityexe.event.GuildEvent;
import com.github.deityexe.event.EventFactory;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.List;


public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();
        String prefix = properties.getPrefix();

        String token = properties.getToken();
        if (token == null) {
            System.out.println("No token specified in \"bot.properties\"! Please add a token to continue.");
            return;
        }

        List<GuildEvent> guildEvents = (new EventFactory()).findAllEvents();
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        api.addMessageCreateListener(new Commands(api, prefix, guildEvents));

        api.addReactionAddListener(event -> {
            try {
                for (GuildEvent guildEvent : guildEvents) {
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
                for (GuildEvent guildEvent : guildEvents) {
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
