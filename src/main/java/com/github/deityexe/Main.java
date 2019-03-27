package com.github.deityexe;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;

import java.util.List;


public class Main {
    static String[] roleEmote = {"\uD83D\uDEE1","\uD83D\uDC96","\u2694"}; //0 = tanks, 1 = supports, 2 = dds

    public static void main(String[] args) {
        //create properties + set prefix
        Properties properties = new Properties();
        String prefix = properties.getPrefix();
        //get bot token
        String token = properties.getToken();
        if (token == null) {
            System.out.println("No token specified in \"bot.properties\"! Please add a token to continue.");
            return;
        }
        //init Raid Array
        List<Raid> raids = Raid.getRaidList();

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        api.addMessageCreateListener(new Commands(api, prefix, raids));

        api.addReactionAddListener(event -> {
            for (Raid r : raids) {
                if (event.getMessageId() == r.getRaidmessageID() && event.getUser() != api.getYourself()) {
                    handleAddReaction(r, event);
                }
           }
        });

        api.addReactionRemoveListener(event -> {
            for (Raid r : raids) {
                if (event.getMessageId() == r.getRaidmessageID() && event.getUser() != api.getYourself()) {
                    handleRemoveReaction(r, event);
                }
            }
        });
    }

    private static void handleRemoveReaction(Raid r, ReactionRemoveEvent event) {
        try {

            if (event.getEmoji().equalsEmoji(roleEmote[0])) {
                r.removeTank(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"Tank " + roleEmote[0] + "\" __**abgemeldet**__!");
            } else if (event.getEmoji().equalsEmoji(roleEmote[1])) {
                r.removeSupport(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"Healer " + roleEmote[1] + "\" __**abgemeldet**__!");
            } else if (event.getEmoji().equalsEmoji(roleEmote[2])) {
                r.removeDD(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"DD " + roleEmote[2] + "\" __**abgemeldet**__!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            event.getUser().sendMessage("Anmeldung aufgrund eines teschnischen Problems fehlgeschlagen.");
        }
    }

    private static void handleAddReaction(Raid r, ReactionAddEvent event) {
        try {
            if (event.getEmoji().equalsEmoji(roleEmote[0])) {
                r.registerTank(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"Tank " + roleEmote[0] + "\" angemeldet!");
            } else if (event.getEmoji().equalsEmoji(roleEmote[1])) {
                r.registerSupport(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"Healer " + roleEmote[1] + "\" angemeldet!");
            } else if (event.getEmoji().equalsEmoji(roleEmote[2])) {
                r.registerDD(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"DD " + roleEmote[2] + "\" angemeldet!");
            } else {
                if (event.getMessage().isPresent()) {
                    try {
                        event.getMessage().get().removeReactionByEmoji(event.getEmoji()).join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            event.getUser().sendMessage("Abmeldung aufgrund eines teschnischen Problems fehlgeschlagen.");
        }
    }
}
