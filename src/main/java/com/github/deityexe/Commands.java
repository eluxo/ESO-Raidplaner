package com.github.deityexe;

import com.github.deityexe.command.*;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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
     * Command to modify an existing event.
     */
    private static final String COMMAND_MODEVENT = PREFIX + "mod";

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
    final private Map<UUID, GuildEvent> guildEvents;

    /**
     * Stores the help message as read from the resource file.
     */
    private final String helpMessage;

    private final ICommandEnvironment commandEnvironment = new ICommandEnvironment() {

        @Override
        public Collection<GuildEvent> getGuildEvents() {
            return guildEvents.values();
        }

        @Override
        public DiscordApi getDiscordApi() {
            return api;
        }

        @Override
        public GuildEvent eventByName(final String name) {
            Collection<GuildEvent> guildEvents = this.getGuildEvents();
            for (GuildEvent event : guildEvents) {
                if (event.getName().equals(name)) {
                    return event;
                }
            }

            return null;
        }

        @Override
        public GuildEvent eventByUuid(final UUID uuid) {
            return guildEvents.get(uuid);
        }

        @Override
        public void addEvent(GuildEvent guildEvent) {
            if (this.eventByName(guildEvent.getName()) != null) {
                throw new DeliverableError("Event mit Namen " + guildEvent.getName() + " existiert bereits.");
            }
            guildEvents.put(guildEvent.getUuid(), guildEvent);
        }

        @Override
        public void removeEvent(GuildEvent guildEvent) {
            guildEvents.remove(guildEvent.getUuid());
        }
    };

    Commands(DiscordApi api, String prefix, Map<UUID, GuildEvent> events) {
        this.api = api;
        this.guildEvents = events;

        byte[] help = "Error reading help file.".getBytes();
        try {
            InputStream inputStream = getClass().getResourceAsStream("/messages/help.msg");
            help = this.readSteam(inputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.helpMessage = new String(help);
    }

    /**
     * Reads the given input stream completely and returns the array of characters that have been
     * read from the stream.
     *
     * @param inputStream Stream to be read.
     * @return Received buffer of characters.
     * @throws IOException This exception is thrown if reading the file fails.
     */
    private byte[] readSteam(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[512];
        int n = 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(buffer.length);

        while ((n = inputStream.read(buffer, 0, buffer.length)) > 0) {
            outputStream.write(buffer, 0, n);
        }

        return outputStream.toByteArray();
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
            } else if (command.getCommand().equals(COMMAND_MODEVENT)) {
                executable = new ModEventCommand(command);
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
                .append(this.helpMessage)
                .send(channel).join();
    }
}
