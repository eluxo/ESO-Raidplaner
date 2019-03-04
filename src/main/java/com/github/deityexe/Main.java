package com.github.deityexe;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
            System.out.println("No token specified in \"properties.ini\"! Please add a token to continue.");
            return;
        }
        //init Raid Array
        List<Raid> raids = new ArrayList<>();
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File jarDir = jarFile.getParentFile();
            File raidFolder = new File(jarDir, "raids");
            File[] raidfiles = raidFolder.listFiles();
            if(raidFolder.exists() && raidfiles != null) {
                //iterate trough folder
                for(File f : raidfiles){
                    raids.add(new Raid(f));
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        api.addMessageCreateListener(new Commands(api, prefix, raids));

        api.addReactionAddListener(event -> {
           for (Raid r : raids) {
                   if (event.getMessageId() == r.getRaidmessageID() && event.getUser() != api.getYourself()) {
                       //register tank
                       if (event.getEmoji().equalsEmoji(roleEmote[0])) {
                           r.registerTank(event.getUser());
                           event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"Tank " + roleEmote[0] + "\" angemeldet!");
                       }
                       //register support
                       if (event.getEmoji().equalsEmoji(roleEmote[1])) {
                           r.registerSupport(event.getUser());
                           event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"Healer " + roleEmote[1] + "\" angemeldet!");
                       }
                       //register DD
                       if (event.getEmoji().equalsEmoji(roleEmote[2])) {
                           r.registerDD(event.getUser());
                           event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"DD " + roleEmote[2] + "\" angemeldet!");
                       }
                   }
           }
        });

        api.addReactionRemoveListener(event -> {
            for (Raid r : raids) {
                if (event.getMessageId() == r.getRaidmessageID() && event.getUser() != api.getYourself()) {
                    //remove tank
                    if (event.getEmoji().equalsEmoji(roleEmote[0])) {
                        r.removeTank(event.getUser());
                        event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"Tank " + roleEmote[0] + "\" __**abgemeldet**__!");
                    }
                    //remove support
                    if (event.getEmoji().equalsEmoji(roleEmote[1])) {
                        r.removeSupport(event.getUser());
                        event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"Healer " + roleEmote[1] + "\" __**abgemeldet**__!");
                    }
                    //remove DD
                    if (event.getEmoji().equalsEmoji(roleEmote[2])) {
                        r.removeDD(event.getUser());
                        event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + r.getName() + "\" als Rolle \"DD " + roleEmote[2] + "\" __**abgemeldet**__!");
                    }
                }
            }
        });
    }

}
