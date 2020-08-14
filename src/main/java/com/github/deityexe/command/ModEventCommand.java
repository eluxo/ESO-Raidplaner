package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.GuildEvent;
import com.github.deityexe.util.BotConfig;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModEventCommand extends CommandMessage {
    /**
     * Class logger.
     */
    private static final Logger logger = Logger.getLogger(ModEventCommand.class.getName());

    /**
     * Name of the event to be modified.
     */
    private static final int ARG_EVENT_NAME = 0;

    /**
     * Name of the field in the event to be modified.
     */
    private static final int ARG_MOD_FIELD = 1;

    /**
     * Index of first parameter.
     */
    private static final int ARG_FIRST_PARAM = 2;

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

        final String modField = this.arg(ARG_MOD_FIELD);
        final ICommandEnvironment env = this.getCommandEnvironment();
        final DiscordApi api = env.getDiscordApi();
        final MessageCreateEvent event = this.getMessageCreateEvent();
        boolean found = false;

        Collection<GuildEvent> guildEvents = env.getGuildEvents();
        for (GuildEvent guildEvent : guildEvents) {
            if (guildEvent.getName().equals(eventName)) {
                found = true;
                final long messageId = guildEvent.getMessageId();
                try {
                    logger.info(String.format("modifying guild event %s for parameter %s", eventName, modField));

                    // first we make sure that the event message still exists
                    final Server server = this.getCommandEnvironment().getServer();
                    final TextChannel channel = server.getTextChannelById(guildEvent.getChannelId()).get();
                    channel.getMessageById(messageId).join();

                    // retrieve modification handler for parameter
                    final GuildEvent.IModifyEventInterface modify = guildEvent.getEventModifierByName(modField);
                    modify.modify(this, ARG_FIRST_PARAM);

                    // and now, make sure, that the message is in sync
                    logger.info(String.format("update event message in channel %d", channel.getId()));
                    Message.edit(api, guildEvent.getChannelId(), messageId, guildEvent.getEmbed()).join();
                    guildEvent.store();

                    // send feedback to user
                    MessageBuilder msgbuilder = new MessageBuilder()
                            .append(String.format("Feld %s im Event %s geändert.", modField, eventName));
                    msgbuilder.send(event.getChannel()).join();
                } catch (DeliverableError e) {
                    throw e;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "failed to update event", e);
                    throw new DeliverableError("Fehler beim Aktualisieren des Events.");
                }
            }
        }

        if (!found) {
            throw new DeliverableError(String.format("Kein Event mit Namen %s gefunden.", eventName));
        }

        // TODO(eluxo): check, if we want to remove it in case of exceptions
        this.removeCommand();
    }
}
