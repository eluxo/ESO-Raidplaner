package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.EventFactory;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Command to create a new event.
 */
public class NewEventCommand extends CommandMessage {
    /**
     * Minimum number of required arguments for this type of command.
     */
    public static final int MINIMUM_ARGUMENT_COUNT = 4;

    private static final int ARG_EVENT_TYPE = 0;
    private static final int ARG_EVENT_NAME = 1;
    private static final int ARG_DATE = 2;
    private static final int ARG_TIME = 3;

    /**
     * Constructor.
     *
     * @param command The base command to be decorated.
     */
    public NewEventCommand(CommandMessage command) {
        super(command);
        if (this.getArgs().length < MINIMUM_ARGUMENT_COUNT) {
            throw new DeliverableError("Es wurden zu wenig parameter angegeben.");
        }
    }

    /**
     * Getter for the name of the event.
     *
     * @return Name of the event.
     */
    public String getName() {
        return this.arg(ARG_EVENT_NAME);
    }

    /**
     * Getter for the type of the event.
     *
     * @return The type of the event.
     */
    public String getType() {
        return this.arg(ARG_EVENT_TYPE).toLowerCase();
    }

    /**
     * Getter for the date.
     *
     * @return The date.
     */
    public String getDate() {
        return this.arg(ARG_DATE);
    }

    /**
     * Getter for the time of the event.
     *
     * @return Time of the event.
     */
    public String getTime() {
        return this.arg(ARG_TIME);
    }

    @Override
    public void execute() {
        final ICommandEnvironment env = this.getCommandEnvironment();
        final List<GuildEvent> guildEvents = env.getGuildEvents();
        final MessageCreateEvent event = this.getMessageCreateEvent();
        final DiscordApi api = env.getDiscordApi();

        final String eventType = this.getType();
        final String eventName = this.getName();

        for (GuildEvent guildEvent : guildEvents) {
            if (guildEvent.getName().equals(eventName)) {
                this.removeCommand();
                throw new DeliverableError("Event mit dem Namen " + eventName + " existiert bereits.");
            }
        }

        final String args[] = Arrays.copyOfRange(this.getArgs(), 1, this.getArgs().length);
        try {
            final GuildEvent guildEvent = (new EventFactory()).createEvent(this);
            guildEvents.add(guildEvent);
            final Message RaidMessage = new MessageBuilder()
                    .setEmbed(guildEvent.getEmbed())
                    .send(event.getChannel())
                    .join();

            this.removeCommand();
            guildEvent.addReactions(RaidMessage);
            guildEvent.setMessageId(RaidMessage.getId());
            guildEvent.store();
        } catch (DeliverableError error) {
            throw error;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DeliverableError("Fehler beim erstellen des Events: Interner Serverfehler.");
        }
    }
}
