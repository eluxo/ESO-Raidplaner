package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.logging.Logger;

/**
 * Command to list users on an event.
 */
public class UserListCommand extends CommandMessage {
    /**
     * Class logger.
     */
    private static Logger logger = Logger.getLogger(UserListCommand.class.getName());

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
        final MessageCreateEvent event = this.getMessageCreateEvent();
        final DiscordApi api = env.getDiscordApi();
        final String eventName = this.arg(ARG_EVENT_NAME);

        logger.info(String.format("executing for event %s", eventName));

        final GuildEvent guildEvent = env.eventByName(eventName);
        if (guildEvent == null) {
            logger.info("event not found");
            throw new DeliverableError("Event " + eventName + " konnte nicht gefunden werden.");
        }

        guildEvent.getUserList(api).send(event.getChannel()).join();
        this.removeCommand();
    }

}
