package com.github.deityexe.command;

import com.github.deityexe.event.GuildEvent;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command to list all events.
 */
public class EventListCommand extends CommandMessage {
    /**
     * Class logger.
     */
    private static Logger logger = Logger.getLogger(EventListCommand.class.getName());

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
        logger.info("execute");
        final ICommandEnvironment env = this.getCommandEnvironment();
        final Collection<GuildEvent> guildEvents = env.getGuildEvents();
        final MessageCreateEvent event = this.getMessageCreateEvent();

        if (guildEvents.size() == 0) {
            logger.info("no active events found");
            event.getChannel().sendMessage("**Es gibt noch keine aktiven Events!**");
        } else {
            logger.info(String.format("sending %d events to user", guildEvents.size()));
            MessageBuilder msgbuilder = new MessageBuilder().append("**Alle aktiven Events:**\n");
            for (GuildEvent guildEvent : guildEvents) {
                msgbuilder.append(String.format("- %s [%s] (%s, %s)\n", guildEvent.getName(),
                        guildEvent.getTypeName(), guildEvent.getDate(), guildEvent.getTime()));
            }
            msgbuilder.send(event.getChannel()).join();
        }

        this.removeCommand();
    }
}
