package com.github.deityexe;

import com.github.deityexe.command.*;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.List;
import java.util.concurrent.CompletionException;

public class Commands implements MessageCreateListener {
    /**
     * The PREFIX that is checked for each command.
     */
    private static final String PREFIX = "!";

    /**
     * Command to create a new event.
     */
    private static final String COMMAND_NEW = PREFIX + "new";

    /**
     * Command to list all users on an event.
     */
    private static final String COMMAND_USERLIST = PREFIX + "userlist";

    /**
     * Command to list all events.
     */
    private static final String COMMAND_EVENTLIST = PREFIX + "eventlist";

    /**
     * Command to end an event.
     */
    private static final String COMMAND_ENDEVENT = PREFIX + "end";

    /**
     * Command to show help.
     */
    private static final String COMMAND_HELP = PREFIX + "help";

    /**
     * Reference to the discord API.
     */
    final private DiscordApi api;

    /**
     * Reference to the guild events.
     */
    final private List<GuildEvent> guildEvents;

    private final ICommandEnvironment commandEnvironment = new ICommandEnvironment() {

        @Override
        public List<GuildEvent> getGuildEvents() {
            return guildEvents;
        }

        @Override
        public DiscordApi getDiscordApi() {
            return api;
        }
    };

    Commands(DiscordApi api, String prefix, List<GuildEvent> events) {
        this.api = api;
        this.guildEvents = events;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {

        final String content = event.getMessage().getContent();
        if (!content.startsWith(PREFIX)) {
            return;
        }

        try {
            final CommandMessage command = CommandMessage.parse(content)
                    .setMessageCreateEvent(event)
                    .setEnvironment(this.commandEnvironment);
            CommandMessage executable = null;

            if (command.getCommand().equals(COMMAND_NEW)) {
                executable = new NewEventCommand(command);
            } else if (command.getCommand().equals(COMMAND_USERLIST)) {
                executable = new UserListCommand(command);
            } else if (command.getCommand().equals(COMMAND_EVENTLIST)) {
                executable = new EventListCommand(command);
            } else if (command.getCommand().equals(COMMAND_ENDEVENT)) {
                executable = new EndEventCommand(command);
            } else if (command.getCommand().equals(COMMAND_HELP)) {
                this.help(event);
            } else {
            }

            if (executable != null) {
                executable.execute();
            }
        } catch (DeliverableError error) {
            error.printStackTrace();
            event.getChannel().sendMessage(error.toUserMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            event.getChannel().sendMessage("Serverinterner Fehler aufgetreten.");
        }
    }

    /**
     * Shows the command help.
     *
     * @param event The event for the message creation.
     */
    private void help(MessageCreateEvent event) {
        TextChannel channel;
        if (event.isPrivateMessage()) {
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

        new MessageBuilder()
                .append("__**Commands:**__\n\n")
                .append("Hinweis: Alle Parameter enden beim nächsten Leerzeichen. Soll ein Feld Leerzeichen ")
                .append("enthalten, muss es in Anführungsstriche gepackt werden.\n\n")
                .append("__**Event erstellen:**__\n")
                .append("`")
                .append(" - !new [Typ] [Name] [Datum] [Uhrzeit] [...]\n")
                .append("         [Typ]: Art oder Typ des Events (raid, bossrun)\n")
                .append("         [Name]: Eindeutiger Name des Events\n")
                .append("         [Datum]: Datum formatiert als TT.MM.JJJJ\n")
                .append("         [Uhrzeit]: Datum formatiert als HH:MM\n")
                .append("         [...]: Eventspezifische Parameter\n")
                .append("`\n\n")
                .append("_**Raid:**_\n")
                .append("`")
                .append(" - !new raid [Name] [Datum] [Uhrzeit] [Anzahl Tanks] [Anzahl Healer] [Anzahl DDs] <Beschreibung>\n")
                .append("         Beispiel: !new raid vCR+3 11.11.2019 11:11 3 2 7 \"Wir wollen uns nochmal an nCR+3 versuchen.\"\n")
                .append("`\n\n")
                .append("_**Worldboss Run:**_\n")
                .append("`")
                .append(" - !new bossrun [Name] [Datum] [Uhrzeit] <Beschreibung>\n")
                .append("         Beispiel: !new bossrun Kargstein 11.11.2019 11:11 3 2 7 \"Gruppenbosse in Kargstein\"\n")
                .append("`\n\n")
                .append("Hinweis: Die <Beschreibung> ist optional.\n\n")
                .append("__**Event beenden**__\n")
                .append("`")
                .append(" - !end [Name]\n")
                .append("         Beispiel: !end vCR+3\n")
                .append("`\n\n")
                .append("__**Alle aktiven Events anzeigen**__\n")
                .append("`")
                .append(" - !eventlist")
                .append("`\n\n")
                .append("__**Angemeldete Teilnehmer anzeigen**__\n")
                .append("`")
                .append(" - !userlist [Name]\n")
                .append("         [Name]: Eindeutiger Name des Events\n")
                .append("         Beispiel: !userlist vCR+3\n")
                .append("`\n\n")
                .append("__**Hilfe anzeigen**__\n")
                .append("`")
                .append(" - !help")
                .append("`\n\n")
                .append("Der Quellcode für diesen Bot ist unter https://github.com/eluxo/ESO-Raidplaner zu finden.\n")
                .append("Der Bot basiert auf einem Fork von https://github.com/DeityEXE/ESO-Raidplaner").append(".")
                .send(channel).join();
    }

    private void newEvent(NewEventCommand newEventCommand, MessageCreateEvent event) {

    }
}
