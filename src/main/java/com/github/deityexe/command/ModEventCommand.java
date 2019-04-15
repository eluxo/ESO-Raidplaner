package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModEventCommand extends CommandMessage {
    /**
     * Class logger.
     */
    private static final Logger logger = Logger.getLogger(ModEventCommand.class.getName());

    private static final int ARG_EVENT_NAME = 0;
    private static final int ARG_EVENT_DATE = 1;
    private static final int ARG_EVENT_TIME = 2;

    /**
     * Constructor.
     *
     * @param command Command to be decorated.
     */
    public ModEventCommand(final CommandMessage command) {
        super(command);
        if (this.getArgs().length < 3) {
            throw new CommandFormatException("Nicht genügend Parameter zum Bearbeiten des Events.");
        }
    }

    @Override
    public void execute() {
        final String eventName = this.arg(ARG_EVENT_NAME);
        logger.info(String.format("executing for event %s", eventName));

        final String eventDate = this.arg(ARG_EVENT_DATE);
        final String eventTime = this.arg(ARG_EVENT_TIME);
        final ICommandEnvironment env = this.getCommandEnvironment();
        final DiscordApi api = env.getDiscordApi();
        final MessageCreateEvent event = this.getMessageCreateEvent();

        Collection<GuildEvent> guildEvents = env.getGuildEvents();
        for (GuildEvent guildEvent : guildEvents) {
            if (guildEvent.getName().equals(eventName)) {
                final long messageId = guildEvent.getMessageId();
                try {
                    logger.info(String.format("modifying message id %d in channel %d", messageId,
                            event.getChannel().getId()));
                    // make sure that message exists in our channel
                    event.getChannel().getMessageById(messageId).join();
                    guildEvent.setDateFromString(eventDate, eventTime);
                    Message.edit(api, event.getChannel().getId(), messageId, guildEvent.getEmbed());
                    guildEvent.store();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "failed to update event", e);
                    throw new DeliverableError("Fehler beim Aktualisieren des Events.");
                }
            }
        }

        // TODO(eluxo): check, if we want to remove it in case of exceptions
        this.removeCommand();
    }
}
