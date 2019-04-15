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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pvp extends GuildEvent {
    /**
     * Class logger.
     */
    private static final Logger logger = Logger.getLogger(Pvp.class.getName());

    /**
     * Type of the worldboss run event.
     */
    private static final String EVENT_TYPE_PVP = "pvp";

    /**
     * Folder for worldboss runs.
     */
    private static final File EVEMT_FOLDER_PVP = new File(EVENT_TYPE_PVP + "s");

    /**
     * List of emotes allowed on the message.
     */
    private final String[] emotes;

    /**
     * Property listing registered tanks.
     */
    private static final String PROP_REGISTERED_PARTICIPANTS = "registeredParticipants";

    /**
     * Position of the alliance name.
     */
    private static final int ARG_ALLIANCE = GuildEvent.ARG_OFFSET + 1;

    /**
     * Position of the description.
     */
    private static final int ARG_DESCRIPTION = GuildEvent.ARG_OFFSET + 2;

    /**
     * Names of the alliances.
     */
    private static final String[] ALLIANCE_NAMES = {
            "Aldmeri Dominion",
            "Dolchsturz Bündnis",
            "Ebenerz Pakt"
    };

    /**
     * Emotes for each alliance.
     */
    private static final String[][] ALLIANCE_EMOTES = {
            new String[] { Emote.EMOTE_ALDMERI },
            new String[] { Emote.EMOTE_DAGGERFALL },
            new String[] { Emote.EMOTE_EBONHEART }
    };

    /**
     * Property holding the selected PvP alliance.
     */
    private static final String PROP_ALLIANCE = "pvpAlliance";

    /**
     * Creates a new worldboss run from command.
     *
     * @param command The command to create the run on.
     * @throws IOException Thrown in IO errors.
     */
    protected Pvp(NewEventCommand command) throws IOException {
        super(EVEMT_FOLDER_PVP, command);

        final int alliance = this.setAlliance(command.arg(ARG_ALLIANCE));
        this.emotes = ALLIANCE_EMOTES[alliance];
        this.setAllowedEmotes(this.emotes);

        this.setDescription(command.arg(ARG_DESCRIPTION));
        this.setString(PROP_REGISTERED_PARTICIPANTS, "");
    }

    /**
     * Loads a worldboss run from file.
     *
     * @param eventId The unique id of the event.
     * @throws IOException Thrown in IO errors.
     */
    protected Pvp(UUID eventId) throws IOException {
        super(EVEMT_FOLDER_PVP, eventId);

        final int alliance = this.getAlliance();
        this.emotes = ALLIANCE_EMOTES[alliance];
        this.setAllowedEmotes(this.emotes);
    }

    /**
     * Sets the alliance for the event.
     *
     * @param alliance The alliance for the event.
     * @return Index of the selected alliance.
     */
    private int setAlliance(final String alliance) {
        logger.info(String.format("lookup alliance name for '%s'", alliance));
        for (int i = 0; i < ALLIANCE_NAMES.length; ++i) {
            if (ALLIANCE_NAMES[i].toLowerCase().startsWith(alliance)) {
                this.setLong(PROP_ALLIANCE, i);
                return i;
            }
        }

        logger.log(Level.SEVERE, "alliance name not found");
        throw new DeliverableError("Fehlerhafte Allianz %s. Mögliche Werte: aldmeri, dolchsturz oder ebenerz");
    }

    /**
     * Getter for the alliance of the event.
     *
     * @return Alliance of the event.
     */
    private int getAlliance() {
        return (int)this.getLong(PROP_ALLIANCE, 0);
    }

    /**
     * Getter for the selected alliance name.
     *
     * @return Name of the events alliance.
     */
    private String getAllianceName() {
        final int alliance = this.getAlliance();
        if (alliance < 0 || alliance >= ALLIANCE_NAMES.length) {
            logger.log(Level.SEVERE, String.format("alliance value out of bounds %d", alliance));
            return ALLIANCE_NAMES[0];
        }
        return ALLIANCE_NAMES[alliance];
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
        return EVENT_TYPE_PVP;
    }

    @Override
    public EmbedBuilder getEmbed() {
        final String description = this.getDescription();
        return new EmbedBuilder().setColor(Color.white).setTitle(this.getTitle()).setDescription(description);
    }

    @Override
    protected String getPrefix() {
        return String.format("PvP [%s]", this.getAllianceName());
    }

    @Override
    public MessageBuilder getUserList(DiscordApi api) {
        MessageBuilder messageBuilder = new MessageBuilder().append("__**Angemeldete Spieler f\u00fcr \"" + this.getName() + "\":**__\n\n");

        messageBuilder.append("**Teilnehmer " + this.emotes[0] + "**\n");
        this.renderUserList(PROP_REGISTERED_PARTICIPANTS, messageBuilder, api);
        return  messageBuilder;
    }

    @Override
    public void addReactions(Message message) {
        message.addReactions(this.emotes[0]);
    }

    @Override
    protected boolean onAddReaction(ReactionAddEvent event, DiscordApi api) {
        try {
            if (event.getEmoji().equalsEmoji(this.emotes[0])) {
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
            if (event.getEmoji().equalsEmoji(this.emotes[0])) {
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

    public static final Creator CREATOR = new Creator() {

        @Override
        public String getEventType() {
            return EVENT_TYPE_PVP;
        }

        @Override
        public GuildEvent newInstance(final NewEventCommand command) throws IOException {
            return new Pvp(command);
        }

        @Override
        public GuildEvent newInstance(final UUID uuid) throws IOException {
            return new Pvp(uuid);
        }

        @Override
        public List<UUID> listStoredEvents() {
            return getEventList(EVEMT_FOLDER_PVP);
        }
    };
}
