package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;

/**
 * Command to list users on an event.
 */
public class UserListCommand extends CommandMessage {
    private static final int ARG_EVENT_NAME = 0;

    /**
     * Constrcutor.
     *
     * @param command The command  to be decorated.
     */
    public UserListCommand(CommandMessage command) {
        super(command);
        if (this.getArgs().length < 1) {
            throw new DeliverableError("Kein Event-Name angegeben.");
        }
    }

    @Override
    public void execute() {
        final ICommandEnvironment env = this.getCommandEnvironment();
        final List<GuildEvent> guildEvents = env.getGuildEvents();
        final MessageCreateEvent event = this.getMessageCreateEvent();
        final DiscordApi api = env.getDiscordApi();
        final String eventName = this.arg(ARG_EVENT_NAME);

        final GuildEvent guildEvent = this.findEvent(guildEvents, eventName);
        guildEvent.getUserList(api).send(event.getChannel()).join();
        this.removeCommand();
    }

    private GuildEvent findEvent(List<GuildEvent> guildEvents, String eventName) {
        for (GuildEvent guildEvent : guildEvents) {
            if (guildEvent.getName().equals(eventName)) {
                return guildEvent;
            }
        }
        throw new DeliverableError("Event " + eventName + " konnte nicht gefunden werden.");
    }
}
