package com.github.deityexe;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Main {
    static String[] roleEmote = {"\uD83D\uDEE1","\uD83D\uDC96","⚔"}; //0 = tanks, 1 = supports, 2 = dds

    public static void main(String[] args) {
        //create config + set prefix
        Config config = new Config();
        String prefix = config.getPrefix();
        //get bot token
        String token = config.getToken();
        if (token == null) {
            System.out.println("No token specified in \"config.ini\"! Please add a token to continue.");
            return;
        }

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();


        //init Raid Array
        List<Raid> raids = new ArrayList<>();
        try {
            File jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
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

        api.addMessageCreateListener(event -> {
            String content = event.getMessage().getContent();

            //create Raid command
            if (content.startsWith(prefix + "raid ")) {
                String[] commandargs = content.split(" ");  //1 = name, 2 = date, 3 = amount tanks, 4 = amount supports, 5 = amount DD
                //check for valid ints
                //date
                String[] dateint = commandargs[2].split("\\.");
                if (dateint.length != 3) {
                    event.getChannel().sendMessage("Error: Invalid date!\nCorrect Format: \"DD.MM.YYYY\"");
                    return;
                }
                for(String s : dateint) {
                    try {
                        Integer.valueOf(s);
                    } catch (NumberFormatException e) {
                        event.getChannel().sendMessage("Error: Invalid date!\nCorrect Format: \"DD.MM.YYYY\"");
                        return;
                    }
                }
                //roles
                int tanks, supports, dds;
                //tanks
                try {
                    tanks = Integer.valueOf(commandargs[3]);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Error: Invalid integer at argument \"tank\"!");
                    return;
                }
                //supports
                try {
                    supports = Integer.valueOf(commandargs[4]);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Error: Invalid integer at argument \"support\"!");
                    return;
                }
                //dds
                try {
                    dds = Integer.valueOf(commandargs[5]);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Error: Invalid integer at argument \"DD\"!");
                    return;
                }
                //check role amount
                if ((tanks + supports + dds) < 12) {
                    event.getChannel().sendMessage("Error: Not enough roles specified!");
                    return;
                }
                if ((tanks + supports + dds) > 12) {
                    event.getChannel().sendMessage("Error: Too many roles specified!");
                    return;
                }
                //create Raid object
                Raid raid = new Raid(commandargs);
                //add Raid object to List
                raids.add(raid);

                //send Raid info Message
                Message RaidMessage = new MessageBuilder().append("Raid Event erfolgreich erstellt:").setEmbed(raid.getEmbed()).send(event.getChannel()).join();

                //add reactions
                for (int i = 0; i < 3; i++) {
                    RaidMessage.addReaction(roleEmote[i]);
                }

                //save Message in Raid Object
                raid.setRaidmessageID(RaidMessage);
            }


            //print registered raid user
            if (content.startsWith(prefix + "raidlist ")) {
                String raidname = content.split(" ")[1];
                Raid raid = null;
                //search raid
                for (Raid r : raids) {
                    if (r.getName().equals(raidname)) {
                        raid = r;
                    }
                }
                if (raid == null) {
                    event.getChannel().sendMessage("Error: Raid not found!");
                    return;
                }

                MessageBuilder msgbuilder = new MessageBuilder().append("**Angemeldete Spieler für \"" + raid.getName() + "\":**\n");
                //add Tanks
                msgbuilder.append("Tanks " + roleEmote[0] + "\n");
                for (long usrID : raid.getRegisteredTanks()) {
                    try {
                        msgbuilder.append("- " + api.getUserById(usrID).get().getName() + "\n");
                    } catch (Exception e) {
                        msgbuilder.append("- *Missing User*\n");
                        e.printStackTrace();
                    }
                }
                //add Healer
                msgbuilder.append("Healer " + roleEmote[1] + "\n");
                for (long usrID : raid.getRegisteredSupports()) {
                    try {
                        msgbuilder.append("- " + api.getUserById(usrID).get().getName() + "\n");
                    } catch (Exception e) {
                        msgbuilder.append("- *Missing User*\n");
                        e.printStackTrace();
                    }
                }
                //add DD
                msgbuilder.append("DDs " + roleEmote[2] + "\n");
                for (long usrID : raid.getRegisteredDDs()) {
                    try {
                        msgbuilder.append("- " + api.getUserById(usrID).get().getName() + "\n");
                    } catch (Exception e) {
                        msgbuilder.append("- *Missing User*\n");
                        e.printStackTrace();
                    }
                }
                //print result
                msgbuilder.send(event.getChannel()).join();
            }

            //print all current raids
            if (content.startsWith(prefix + "raids")) {
                MessageBuilder msgbuilder = new MessageBuilder().append("**Alle aktiven Raid Events:**\n");
                for (Raid r : raids) {
                    msgbuilder.append("- " + r.getName() + " (" + r.getDate().get(Calendar.DATE) + "." + (r.getDate().get(Calendar.MONTH) + 1) + "." + r.getDate().get(Calendar.YEAR) + ")\n");
                }
                msgbuilder.send(event.getChannel()).join();
            }
        });

        api.addReactionAddListener(event -> {
           for (Raid r : raids) {
                   if (event.getMessageId() == r.getRaidmessageID() && event.getUser() != api.getYourself()) {
                       //register tank
                       if (event.getEmoji().equalsEmoji(roleEmote[0])) {
                           r.registerTank(event.getUser());
                           event.getUser().sendMessage("Erfolgreich für \"" + r.getName() + "\" als Rolle \"Tank " + roleEmote[0] + "\" angemeldet!");
                       }
                       //register support
                       if (event.getEmoji().equalsEmoji(roleEmote[1])) {
                           r.registerSupport(event.getUser());
                           event.getUser().sendMessage("Erfolgreic für \"" + r.getName() + "\" als Rolle \"Healer " + roleEmote[1] + "\" angemeldet!");
                       }
                       //register DD
                       if (event.getEmoji().equalsEmoji(roleEmote[2])) {
                           r.registerDD(event.getUser());
                           event.getUser().sendMessage("Erfolgreich für \"" + r.getName() + "\" als Rolle \"DD " + roleEmote[2] + "\" angemeldet!");
                       }
                   }
           }
        });
    }

}
