package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Command to end an event.
 */
public class EndEventCommand extends CommandMessage {
    /**
     * Class logger.
     */
    private static Logger logger = Logger.getLogger(EndEventCommand.class.getName());

    private static final int ARG_EVENT_NAME = 0;

    /**
     * Constructor.
     *
     * @param command Command to be decorated.
     */
    public EndEventCommand(final CommandMessage command) {
        super(command);
        if (this.getArgs().length < 1) {
            throw new CommandFormatException("Kein gültiges Event zum Beenden angegeben.");
        }
    }

    @Override
    public void execute() {
        final String eventName = this.arg(ARG_EVENT_NAME);
        logger.info(String.format("execute for %s", eventName));

        final ICommandEnvironment env = this.getCommandEnvironment();
        final DiscordApi api = env.getDiscordApi();
        final MessageCreateEvent event = this.getMessageCreateEvent();

        GuildEvent guildEvent = env.eventByName(eventName);
        if (guildEvent == null) {
            throw new DeliverableError(String.format("Event %s konnte nicht gefunden werden", eventName));
        }

        if (!guildEvent.delete()) {
            logger.info(String.format("could not delete %s", eventName));
            throw new DeliverableError("Entfernen des Events " + eventName + " fehlgeschlagen.")
                    .setPrivate(false);
        }
        logger.info(String.format("%s has been deleted", eventName));

        env.removeEvent(guildEvent);
        logger.info(String.format("%s has been removed", eventName));

        if (event.getMessageAuthor().asUser().isPresent()) {
            try {
                event.getChannel().sendMessage("__**Anmeldung f\u00fcr \"" + guildEvent.getName() + "\" wurde geschlossen!**__");
                guildEvent.getUserList(api).send(event.getMessageAuthor().asUser().get().openPrivateChannel().get());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "failed sending message to user", e);
            }
        }

        // TODO(eluxo): check, if we want to remove it in case of exceptions
        this.removeCommand();
    }
}
