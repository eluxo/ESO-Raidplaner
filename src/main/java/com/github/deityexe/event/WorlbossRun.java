package com.github.deityexe.event;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.command.NewEventCommand;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class WorlbossRun extends GuildEvent {
    /**
     * Type of the worldboss run event.
     */
    private static final String EVENT_TYPE_WORLDBOSS_RUN = "bossrun";

    /**
     * Folder for worldboss runs.
     */
    private static final File EVENT_FOLDER_WORLDBOSS_RUN = new File(EVENT_TYPE_WORLDBOSS_RUN + "s");

    /**
     * List of emotes allowed on the message.
     */
    private static final String ALLOWED_EMOTES[] = new String[] { Emote.EMOTE_PARTICIPANT };

    /**
     * Property listing registered tanks.
     */
    private static final String PROP_REGISTERED_PARTICIPANTS = "registeredParticipants";

    private static final int ARG_DESCRIPTION = GuildEvent.ARG_OFFSET + 1;

    /**
     * Creates a new worldboss run from command.
     *
     * @param command The command to create the run on.
     * @throws IOException Thrown in IO errors.
     */
    protected WorlbossRun(NewEventCommand command) throws IOException {
        super(EVENT_FOLDER_WORLDBOSS_RUN, command);
        this.setAllowedEmotes(ALLOWED_EMOTES);

        this.setDescription(command.arg(ARG_DESCRIPTION));
        this.setString(PROP_REGISTERED_PARTICIPANTS, "");
    }

    /**
     * Loads a worldboss run from file.
     *
     * @param eventId The unique id of the event.
     * @throws IOException Thrown in IO errors.
     */
    protected WorlbossRun(UUID eventId) throws IOException {
        super(EVENT_FOLDER_WORLDBOSS_RUN, eventId);
        this.setAllowedEmotes(ALLOWED_EMOTES);

    }

    /**
     * Registers the current user as participant.
     *
     * @param user The user to be registered as participant.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void registerParticipant(final User user) throws IOException {
        this.registerParticipant(user, PROP_REGISTERED_PARTICIPANTS);
    }


    /**
     * Unregister the current user as participant.
     *
     * @param user The user to be unreghistered as participant.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void unregisterParticipant(final User user) throws IOException {
        this.unregisterParticipant(user, PROP_REGISTERED_PARTICIPANTS);
    }

    @Override
    protected String getEventType() {
        return EVENT_TYPE_WORLDBOSS_RUN;
    }

    @Override
    public EmbedBuilder getEmbed() {
        final String description = this.getDescription();
        return new EmbedBuilder().setColor(Color.white).setTitle(this.getTitle()).setDescription(description);
    }

    @Override
    public MessageBuilder getUserList(DiscordApi api) {
        MessageBuilder messageBuilder = new MessageBuilder().append("__**Angemeldete Spieler f\u00fcr \"" + this.getName() + "\":**__\n\n");

        messageBuilder.append("**Teilnehmer " + Emote.EMOTE_PARTICIPANT + "**\n");
        this.renderUserList(PROP_REGISTERED_PARTICIPANTS, messageBuilder, api);
        return  messageBuilder;
    }

    @Override
    public void addReactions(Message message) {
        message.addReactions(Emote.EMOTE_PARTICIPANT);
    }

    @Override
    protected boolean onAddReaction(ReactionAddEvent event, DiscordApi api) {
        try {
            if (event.getEmoji().equalsEmoji(Emote.EMOTE_PARTICIPANT)) {
                this.registerParticipant(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + " angemeldet!");
            }
        } catch (DeliverableError error) {
            throw error;
        } catch (Exception ex) {
            ex.printStackTrace();
            DeliverableError error = DeliverableError
                    .create("Anmeldung aufgrund eines teschnischen Problems fehlgeschlagen.")
                    .setException(ex);
            throw error;
        }
        return true;
    }

    @Override
    protected boolean onRemoveReaction(ReactionRemoveEvent event, DiscordApi api) {
        try {
            if (event.getEmoji().equalsEmoji(Emote.EMOTE_PARTICIPANT)) {
                this.unregisterParticipant(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" __**abgemeldet**__!");
            }
        } catch (DeliverableError error) {
            throw error;
        } catch (Exception ex) {
            ex.printStackTrace();
            DeliverableError error = DeliverableError
                    .create("Anmeldung aufgrund eines teschnischen Problems fehlgeschlagen.")
                    .setException(ex);
            throw error;
        }
        return true;
    }

    public static final GuildEvent.Creator CREATOR = new GuildEvent.Creator() {

        @Override
        public String getEventType() {
            return EVENT_TYPE_WORLDBOSS_RUN;
        }

        @Override
        public GuildEvent newInstance(final NewEventCommand command) throws IOException {
            return new WorlbossRun(command);
        }

        @Override
        public GuildEvent newInstance(final UUID uuid) throws IOException {
            return new WorlbossRun(uuid);
        }

        @Override
        public List<UUID> listStoredEvents() {
            return getEventList(EVENT_FOLDER_WORLDBOSS_RUN);
        }
    };
}
