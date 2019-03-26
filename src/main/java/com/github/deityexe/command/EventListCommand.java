package com.github.deityexe.command;

import com.github.deityexe.event.GuildEvent;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;

/**
 * Command to list all events.
 */
public class EventListCommand extends CommandMessage {
    /**
     * Constructor.
     *
     * @param command The command to be decorated.
     */
    public EventListCommand(CommandMessage command) {
        super(command);
    }

    @Override
    public void execute() {
        final ICommandEnvironment env = this.getCommandEnvironment();
        final List<GuildEvent> guildEvents = env.getGuildEvents();
        final MessageCreateEvent event = this.getMessageCreateEvent();

        if (guildEvents.size() == 0) {
            event.getChannel().sendMessage("**Es gibt noch keine aktiven Raid Events!**");
        } else {
            MessageBuilder msgbuilder = new MessageBuilder().append("**Alle aktiven Raid Events:**\n");
            for (GuildEvent guildEvent : guildEvents) {
                msgbuilder.append("- " + guildEvent.getName() + " (" + guildEvent.getDate() + ", " + guildEvent.getTime() + ")\n");
            }
            msgbuilder.send(event.getChannel()).join();
        }

        this.removeCommand();
    }
}
