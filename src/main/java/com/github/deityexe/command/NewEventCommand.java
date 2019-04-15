package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.EventFactory;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Command to create a new event.
 */
public class NewEventCommand extends CommandMessage {
    /**
     * Class logger.
     */
    private static final Logger logger = Logger.getLogger(NewEventCommand.class.getName());

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
        final String eventName = this.getName();
        logger.info(String.format("executing for %s", eventName));

        final ICommandEnvironment env = this.getCommandEnvironment();
        final MessageCreateEvent event = this.getMessageCreateEvent();
        final DiscordApi api = env.getDiscordApi();

        if (env.eventByName(eventName) != null) {
            logger.info(String.format("event %s already exists", eventName));
            this.removeCommand();
            throw new DeliverableError("Event mit dem Namen " + eventName + " existiert bereits.");
        }

        final String args[] = Arrays.copyOfRange(this.getArgs(), 1, this.getArgs().length);
        try {
            logger.info(String.format("creating event %s", eventName));
            final GuildEvent guildEvent = (new EventFactory()).createEvent(this);
            final Message RaidMessage = new MessageBuilder()
                    .setEmbed(guildEvent.getEmbed())
                    .send(event.getChannel())
                    .join();

            logger.info("adding reaction symbols to event");
            guildEvent.addReactions(RaidMessage);

            logger.info("retrieve ID values");
            guildEvent.setMessageId(RaidMessage.getId());
            guildEvent.setChannelId(RaidMessage.getChannel().getId());
            RaidMessage.getServer().ifPresent(server -> {
                guildEvent.setServerId(server.getId());
            });

            logger.info(String.format("make sure that %s is stored", eventName));
            guildEvent.store();
            env.addEvent(guildEvent);

            this.removeCommand();
        } catch (DeliverableError error) {
            throw error;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DeliverableError("Fehler beim erstellen des Events: Interner Serverfehler.");
        }
    }
}
