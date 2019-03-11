package com.github.deityexe;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletionException;

import static com.github.deityexe.Main.roleEmote;

public class Commands implements MessageCreateListener {

    private String prefix;
    private DiscordApi api;
    private List<Raid> raids;


    Commands(DiscordApi api, String prefix, List<Raid> rs) {
        this.api = api;
        this.prefix = prefix;
        this.raids = rs;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        String content = event.getMessage().getContent();

        //create Raid command
        if (content.startsWith(prefix + "raid ")) {
            String[] commandargs = content.split(" ");  //1 = name, 2 = date, 3 = time, 4 = amount tanks, 5 = amount supports, 6 = amount DD
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
            //time
            String[] timeint = commandargs[3].split(":");
            if (timeint.length != 2) {
                event.getChannel().sendMessage("Error: Invalid time!\nCorrect Format:\"HH:MM\"");
                return;
            }
            for(String s : timeint) {
                try {
                    Integer.valueOf(s);
                } catch (NumberFormatException e) {
                    event.getChannel().sendMessage("Error: Invalid time!\nCorrect Format:\"HH:MM\"");
                    return;
                }
            }
            //roles
            int tanks, supports, dds;
            //tanks
            try {
                tanks = Integer.valueOf(commandargs[4]);
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Error: Invalid integer at argument \"tank\"!");
                return;
            }
            //supports
            try {
                supports = Integer.valueOf(commandargs[5]);
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Error: Invalid integer at argument \"support\"!");
                return;
            }
            //dds
            try {
                dds = Integer.valueOf(commandargs[6]);
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
            Message RaidMessage = new MessageBuilder().setEmbed(raid.getEmbed()).send(event.getChannel()).join();

            //remove command
            try {
                event.getMessage().delete().join();
            } catch (CompletionException e) {
                e.printStackTrace();
            }

            //add reactions
            for (int i = 0; i < 3; i++) {
                RaidMessage.addReaction(roleEmote[i]);
            }

            //save Message in Raid Object
            raid.setRaidmessageID(RaidMessage);
        }


        //print registered raid user
        if (content.startsWith(prefix + "userlist")) {
            if (content.split(" ").length < 2) {
                event.getChannel().sendMessage("Error: No name given!");
                return;
            }
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
            //print result
            raid.getUserList(api).send(event.getChannel()).join();

            //remove command
            try {
                event.getMessage().delete().join();
            } catch (CompletionException e) {
                e.printStackTrace();
            }
        }

        //print all current raids
        if (content.startsWith(prefix + "raids")) {
            if (raids.size() == 0) {
                event.getChannel().sendMessage("**Es gibt noch keine aktiven Raid Events!**");
            } else {
                MessageBuilder msgbuilder = new MessageBuilder().append("**Alle aktiven Raid Events:**\n");
                for (Raid r : raids) {
                    msgbuilder.append("- " + r.getName() + " (" + r.getDate().get(Calendar.DATE) + "." + (r.getDate().get(Calendar.MONTH) + 1) + "." + r.getDate().get(Calendar.YEAR) + ", " + r.getTime() + ")\n");
                }
                msgbuilder.send(event.getChannel()).join();
            }

            //remove command
            try {
                event.getMessage().delete().join();
            } catch (CompletionException e) {
                e.printStackTrace();
            }
        }

        //end raid
        if (content.startsWith(prefix + "endraid")) {
            if (content.split(" ").length < 2) {
                event.getChannel().sendMessage("Error: No name given!");
                return;
            }
            String raidname = content.split(" ")[1];
            ListIterator<Raid> iterator = raids.listIterator();
            while(iterator.hasNext()) {
                Raid r = iterator.next();
                if (r.getName().equals(raidname)) {
                    if (!r.deleteRaid()) {
                        event.getChannel().sendMessage("Error: " + r.getName() + ".raid could not be deleted! The Raid is still active!");
                        return;
                    }
                    if (event.getMessageAuthor().asUser().isPresent()) {
                        try {
                            event.getChannel().sendMessage("__**Anmeldung f\u00fcr \"" + r.getName() + "\" wurde geschlossen!**__");
                            r.getUserList(api).send(event.getMessageAuthor().asUser().get().openPrivateChannel().get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    iterator.remove();
                }
            }
            //remove command
            try {
                event.getMessage().delete().join();
            } catch (CompletionException e) {
                e.printStackTrace();
            }
        }

        //help command
        if (content.equals(prefix + "help") || content.equals(prefix + "hilfe")) {
            //get User PrivateChannel
            TextChannel channel;
            if(event.isPrivateMessage()) {
                channel = event.getChannel();
            } else {
                //remove command
                try {
                    event.getMessage().delete().join();
                } catch (CompletionException e) {
                    e.printStackTrace();
                }
                if (event.getMessageAuthor().asUser().isPresent()) {
                    try {
                        channel = event.getMessageAuthor().asUser().get().openPrivateChannel().get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    return;
                }
            }

            new MessageBuilder().append("__**Commands:**__\n\n")
                    .append("**Raid erstellen:**\n").append(prefix).append("raid *[Raidname] [Datum] [Uhrzeit] [Anzahl Tanks] [Anzahl Healer] [Anzahl DDs]*\n\n")
                    .append("**Anmeldungslist anzeigen:**\n").append(prefix).append("userlist *[Raidname]*\n\n")
                    .append("**Alle aktiven Raids anzeigen:**\n").append(prefix).append("raids\n\n")
                    .append("**Raid beenden:**\n").append(prefix).append("endraid *[Raidname]*\n\n\n")
                    .append("*Bei Problemen wende dich an meinen Sch\u00f6pfer:*\n").append("https://twitter.com/DeiTYmon")
                    .send(channel).join();
        }
    }


}
