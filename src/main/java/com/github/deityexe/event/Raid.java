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
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

class Raid extends GuildEvent {
    /**
     * The class logger.
     */
    private static final Logger logger = Logger.getLogger(Raid.class.getName());

    /**
     * Property listing registered tanks.
     */
    private static final String PROP_TANKS_REGISTERED = "tanksRegistered";

    /**
     * Property holding the total number of required tanks.
     */
    private static final String PROP_TANKS_REQUIRED = "tanksRequired";

    /**
     * Property listing registered DDs.
     */
    private static final String PROP_DDS_REGISTERED = "ddsRegistered";

    /**
     * Property holding the total number of required DDs.
     */
    private static final String PROP_DDS_REQUIRED = "ddsRequired";

    /**
     * Property listing registered supports.
     */
    private static final String PROP_SUPPORTS_REGISTERED = "supportsRegistered";

    /**
     * Property listing registered reservists.
     */
    private static final String PROP_RESERVISTS_REGISTERED = "reservistsRegistered";

    /**
     * Property holding the total number of required supports.
     */
    private static final String PROP_SUPPORTS_REQUIRED = "supportsRequired";

    /**
     * Event type for a raid.
     */
    public static final String EVENT_TYPE_RAID = "raid";

    /**
     * Folder holding the raids.
     */
    private static final File EVENT_FOLDER_RAID = new File(EVENT_TYPE_RAID + "s");

    /**
     * List of emotes allowed on the message.
     */
    private static final String ALLOWED_EMOTES[] = new String[] { Emote.EMOTE_DD, Emote.EMOTE_TANK,
            Emote.EMOTE_SUPPORT, Emote.EMOTE_RESERVIST };

    private static final int ARG_TANKS_REQUIRED =    ARG_OFFSET + 1;
    private static final int ARG_SUPPORTS_REQUIRED = ARG_OFFSET + 2;
    private static final int ARG_DDS_REQUIRED =      ARG_OFFSET + 3;
    private static final int ARG_DESCRIPTION =       ARG_OFFSET + 4;

    /**
     * Holding the raid configuration properties. This is used to load and store the raid configuration.
     */
    private Properties raidProperties = new Properties();

    /**
     * Construct Raid Object from arguments
     *
     * @param command The command creating the Raid.
     */
    protected Raid(NewEventCommand command) throws IOException, NumberFormatException {
        super(EVENT_FOLDER_RAID, command);
        this.setAllowedEmotes(ALLOWED_EMOTES);

        try {
            this.setTanksRequired(Integer.parseInt(command.arg(ARG_TANKS_REQUIRED)));
            this.setSupportsRequired(Integer.parseInt(command.arg(ARG_SUPPORTS_REQUIRED)));
            this.setDdsRequired(Integer.parseInt(command.arg(ARG_DDS_REQUIRED)));
        } catch (NumberFormatException ex) {
            logger.log(Level.SEVERE, "error converting number", ex);
            throw new DeliverableError("Fehler bei der Zahlenkonvertierung: Anzahl der benötigten Tanks, Heals und DDs "
                    + "müssen Zahlen sein.");
        }

        this.setDescription(command.arg(ARG_DESCRIPTION));
        this.setString(PROP_TANKS_REGISTERED, "");
        this.setString(PROP_DDS_REGISTERED, "");
        this.setString(PROP_SUPPORTS_REGISTERED, "");
        this.setString(PROP_SUPPORTS_REGISTERED, "");
        this.setString(PROP_RESERVISTS_REGISTERED, "");
    }

    /**
     * Constructs Raid Object from file
     * @param eventId The UUID of the event.
     * @throws IOException Raised when the raid could not be loaded from file.
     */
    protected Raid(UUID eventId) throws IOException {
        super(EVENT_FOLDER_RAID, eventId);
        this.setAllowedEmotes(ALLOWED_EMOTES);
    }

    /**
     * Sets the number of required DDs.
     *
     * @param ddsRequired The number of required DDs.
     */
    private void setDdsRequired(int ddsRequired) {
        this.setInt(PROP_DDS_REQUIRED, ddsRequired);
    }

    /**
     * Sets the number of required supports.
     *
     * @param supportsRequired Number of required supports.
     */
    private void setSupportsRequired(int supportsRequired) {
        this.setInt(PROP_SUPPORTS_REQUIRED, supportsRequired);
    }

    /**
     * Sets the number of required tanks.
     *
     * @param tanksRequired Number of required tanks.
     */
    private void setTanksRequired(int tanksRequired) {
        this.setInt(PROP_TANKS_REQUIRED, tanksRequired);
    }

    /**
     * Registers the current user as tank.
     *
     * @param user The user to be registered as tank.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void registerTank(final User user) throws IOException {
        this.registerParticipant(user, PROP_TANKS_REGISTERED);
    }

    /**
     * Unregister the current user as tank.
     *
     * @param user The user to be unreghistered as tank.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void removeTank(final User user) throws IOException {
        this.unregisterParticipant(user, PROP_TANKS_REGISTERED);
    }

    /**
     * Registers the current user as support.
     *
     * @param user The user to be registered as support.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void registerSupport(final User user) throws IOException {
        this.registerParticipant(user, PROP_SUPPORTS_REGISTERED);
    }

    /**
     * Unregister the current user as support.
     *
     * @param user The user to be unreghistered as support.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void removeSupport(final User user) throws IOException {
        this.unregisterParticipant(user, PROP_SUPPORTS_REGISTERED);
    }

    /**
     * Registers the current user as reservist.
     *
     * @param user The user to be registered as reservist.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void registerReservist(final User user) throws IOException {
        this.registerParticipant(user, PROP_RESERVISTS_REGISTERED);
    }

    /**
     * Unregister the current user as reservist.
     *
     * @param user The user to be unreghistered as reservist.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void removeReservist(final User user) throws IOException {
        this.unregisterParticipant(user, PROP_RESERVISTS_REGISTERED);
    }

    /**
     * Registers the current user as DD.
     *
     * @param user The user to be registered as DD.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void registerDD(final User user) throws IOException {
        this.registerParticipant(user, PROP_DDS_REGISTERED);
    }

    /**
     * Unregister the current user as DD.
     *
     * @param user The user to be unreghistered as DD.
     * @throws IOException Thrown, when updating the event fails.
     */
    public void removeDD(final User user) throws IOException {
        this.unregisterParticipant(user, PROP_DDS_REGISTERED);
    }

    @Override
    protected String getEventType() {
        return EVENT_TYPE_RAID;
    }

    /**
     * Renders the embed frame for the raid.
     *
     * @return EmbedBuilder rendering the frame.
     */
    public EmbedBuilder getEmbed() {
        final String description = this.getDescription() + String.format("\n Gesuchte Rollen:\n"
                        + Emote.EMOTE_TANK + " Tanks\t%d\n"
                        + Emote.EMOTE_SUPPORT + " Healer\t%d\n"
                        + Emote.EMOTE_DD + " DDs\t%d\n"
                        + Emote.EMOTE_RESERVIST + " Springer",
                this.getLong(PROP_TANKS_REQUIRED, 0),
                this.getLong(PROP_SUPPORTS_REQUIRED, 0),
                this.getLong(PROP_DDS_REQUIRED, 0));
        return new EmbedBuilder().setColor(Color.white).setTitle(this.getTitle()).setDescription(description);
    }

    /**
     * Message builder for the user list of the event.
     *
     * @param api The {@link DiscordApi} instance that will be used to send the message back.
     * @return The {@link MessageBuilder} instace for rendering the message content.
     */
    public MessageBuilder getUserList(DiscordApi api) {
        MessageBuilder messageBuilder = new MessageBuilder().append("__**Angemeldete Spieler f\u00fcr \"" + this.getName() + "\":**__\n\n");

        messageBuilder.append("**Tanks " + Emote.EMOTE_TANK + "**\n");
        this.renderUserList(PROP_TANKS_REGISTERED, messageBuilder, api);

        messageBuilder.appendNewLine();
        messageBuilder.append("**Heiler " + Emote.EMOTE_SUPPORT + "**\n");
        this.renderUserList(PROP_SUPPORTS_REGISTERED, messageBuilder, api);

        messageBuilder.appendNewLine();
        messageBuilder.append("**DDs " + Emote.EMOTE_DD + "**\n");
        this.renderUserList(PROP_DDS_REGISTERED, messageBuilder, api);

        messageBuilder.appendNewLine();
        messageBuilder.append("**Springer " + Emote.EMOTE_RESERVIST + "**\n");
        this.renderUserList(PROP_RESERVISTS_REGISTERED, messageBuilder, api);

        return  messageBuilder;
    }


    @Override
    public void addReactions(final Message message) {
        message.addReactions(Emote.EMOTE_TANK, Emote.EMOTE_SUPPORT, Emote.EMOTE_DD, Emote.EMOTE_RESERVIST);
    }

    @Override
    protected boolean onAddReaction(final ReactionAddEvent event, final DiscordApi api) {
        try {
            if (event.getEmoji().equalsEmoji(Emote.EMOTE_TANK)) {
                this.registerTank(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" als Rolle \"Tank " + Emote.EMOTE_TANK + "\" angemeldet!");
            } else if (event.getEmoji().equalsEmoji(Emote.EMOTE_SUPPORT)) {
                this.registerSupport(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" als Rolle \"Healer " + Emote.EMOTE_SUPPORT + "\" angemeldet!");
            } else if (event.getEmoji().equalsEmoji(Emote.EMOTE_DD)) {
                this.registerDD(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" als Rolle \"DD " + Emote.EMOTE_DD + "\" angemeldet!");
            } else if (event.getEmoji().equalsEmoji(Emote.EMOTE_RESERVIST)) {
                this.registerReservist(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" als Rolle \"Reservist " + Emote.EMOTE_RESERVIST + "\" angemeldet!");
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
    protected boolean onRemoveReaction(final ReactionRemoveEvent event, final DiscordApi api) {
        try {
            if (event.getEmoji().equalsEmoji(Emote.EMOTE_TANK)) {
                this.removeTank(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" als Rolle \"Tank " + Emote.EMOTE_TANK + "\" __**abgemeldet**__!");
            } else if (event.getEmoji().equalsEmoji(Emote.EMOTE_SUPPORT)) {
                this.removeSupport(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" als Rolle \"Healer " + Emote.EMOTE_SUPPORT + "\" __**abgemeldet**__!");
            } else if (event.getEmoji().equalsEmoji(Emote.EMOTE_DD)) {
                this.removeDD(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" als Rolle \"DD " + Emote.EMOTE_DD + "\" __**abgemeldet**__!");
            } else if (event.getEmoji().equalsEmoji(Emote.EMOTE_RESERVIST)) {
                this.removeReservist(event.getUser());
                event.getUser().sendMessage("Du hast dich erfolgreich f\u00fcr \"" + this.getName() + "\" als Rolle \"Reservist " + Emote.EMOTE_RESERVIST + "\" __**abgemeldet**__!");
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
            return EVENT_TYPE_RAID;
        }

        @Override
        public GuildEvent newInstance(final NewEventCommand command) throws IOException {
            return new Raid(command);
        }

        @Override
        public GuildEvent newInstance(final UUID uuid) throws IOException {
            return new Raid(uuid);
        }

        @Override
        public List<UUID> listStoredEvents() {
            return getEventList(EVENT_FOLDER_RAID);
        }
    };
}
