package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ListIterator;

/**
 * Command to end an event.
 */
public class EndEventCommand extends CommandMessage {
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
        final ICommandEnvironment env = this.getCommandEnvironment();
        final DiscordApi api = env.getDiscordApi();
        final MessageCreateEvent event = this.getMessageCreateEvent();

        ListIterator<GuildEvent> iterator = env.getGuildEvents().listIterator();
        while(iterator.hasNext()) {
            GuildEvent guildEvent = iterator.next();
            if (guildEvent.getName().equals(eventName)) {
                if (!guildEvent.delete()) {
                    throw new DeliverableError("Entfernen des Events " + eventName + " fehlgeschlagen.")
                            .setPrivate(false);
                }

                if (event.getMessageAuthor().asUser().isPresent()) {
                    try {
                        event.getChannel().sendMessage("__**Anmeldung f\u00fcr \"" + guildEvent.getName() + "\" wurde geschlossen!**__");
                        guildEvent.getUserList(api).send(event.getMessageAuthor().asUser().get().openPrivateChannel().get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                iterator.remove();
            }
        }

        // TODO(eluxo): check, if we want to remove it in case of exceptions
        this.removeCommand();
    }
}
