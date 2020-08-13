package com.github.deityexe.event;

import com.github.deityexe.command.NewEventCommand;
import com.github.deityexe.util.DateUtil;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared event class providing the basic file handling interfaces to load and store event configurations.
 */
public abstract class GuildEvent {
    /**
     * The class logger.
     */
    private static final Logger logger = Logger.getLogger(GuildEvent.class.getName());

    /**
     * Offset for child class arguments.
     */
    protected static final int ARG_OFFSET = NewEventCommand.MINIMUM_ARGUMENT_COUNT - 1;

    /**
     * Name field for the event.
     */
    private static final String PROP_NAME = "name";

    /**
     * Date field for the event.
     */
    private static final String PROP_DATE = "date";

    /**
     * UUID field of the event.
     */
    private static final String PROP_UUID = "uuid";

    /**
     * ID of the message.
     */
    private static final String PROP_MESSAGE_ID = "messageId";

    /**
     * Property holding the optional event description.
     */
    private static final String PROP_DESCRIPTION = "eventDescription";

    /**
     * Server the message has been posted on.
     */
    private static final String PROP_SERVER_ID = "serverId";

    /**
     * Channel the message has been posted on.
     */
    private static final String PROP_CHANNEL_ID = "channelId";

    /**
     * Channel the message has been posted on.
     */
    private static final String PROP_ORGANIZER = "organizer";

    /**
     * Class shall be set modified whenever something has changed.
     */
    private boolean modified = true;

    /**
     * The event properties.
     */
    private final Properties properties = new Properties();

    /**
     * File object on the events file.
     */
    private final File eventFile;

    /**
     * List of allowed emotes.
     */
    private String allowedEmotes[] = new String[] {};

    /**
     * Loads an event from the folder of event.
     *
     * @param eventFolder The folder to load the event from.
     * @param eventId The id of the event.
     */
    public GuildEvent(final File eventFolder, final UUID eventId) throws IOException {
        logger.info(String.format("loading %s from %s", this.getEventType(), eventId.toString()));
        this.eventFile = fileFromUuid(eventFolder, eventId);
        this.load();
    }

    /**
     * Getter for the type of the event.
     *
     * @return Type name of the event.
     */
    public String getTypeName() {
        return this.getEventType();
    }

    /**
     * Constructor.
     *
     * @param eventFolder The folder for all the event files.
     * @param command Command used to create the event.
     * @throws IOException Thrown when the the event cannot be saved.
     */
    public GuildEvent(final File eventFolder, NewEventCommand command) throws IOException {
        logger.info(String.format("creating %s named %s from command", command.getType(), command.getName()));
        final UUID uuid = UUID.randomUUID();
        this.setProperty(PROP_UUID, uuid.toString());

        this.setName(command.getName());
        this.setDateFromString(command.getDate(), command.getTime());
        this.setOrganizer(command.getOrganizer());
        this.eventFile = fileFromUuid(eventFolder, uuid);
    }

    /**
     * Constructs the file name from the given UUID.
     *
     * @param eventFolder Folder for this type of event.
     * @param uuid The UUID to create the file name on.
     * @return The file object for the given UUID.
     */
    private static File fileFromUuid(final File eventFolder, final UUID uuid) {
        return new File(eventFolder, uuid.toString());
    }

    /**
     * Getter for the event type.
     *
     * @return Event type.
     */
    protected abstract String getEventType();

    /**
     * Sets the event name.
     *
     * @param name The event name.
     */
    private void setName(final String name) {
        this.setProperty(PROP_NAME, name);
    }

    /**
     * Sets the event date.
     *
     * @param date The event date.
     */
    private void setDate(final Calendar date) {
        this.setLong(PROP_DATE, date.getTimeInMillis());
    }

    /**
     * Updates the date based on the given string.
     *
     * @param date The new date of the event.
     * @param time The new time of the event.
     */
    public void setDateFromString(final String date, final String time) {
        this.setDate(this.dateUtil.dateFromStrings(date, time));
    }

    /**
     * Sets the name of the person organizing the event.
     *
     * @param organizer The person organizing the event.
     */
    public void setOrganizer(final String organizer) {
        this.setString(PROP_ORGANIZER, organizer);
    }

    /**
     * Getter for the event configuration file.
     *
     * @return The event configuration file.
     */
    protected File getEventFile() {
        return this.eventFile;
    }

    /**
     * Reads a long value from the configuration properties.
     *
     * @param name The name of the property to be read.
     * @param fallback The default value to be returned, if the property could not be found.
     * @return The property value.
     */
    protected long getLong(final String name, final long fallback) {
        try {
            return Long.parseLong(this.getProperty(name, Long.toString(fallback)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return fallback;
        }
    }

    /**
     * Sets a long value.
     *
     * @param name Name of the value.
     * @param value The value to be stored.
     */
    protected void setLong(final String name, final long value) {
        this.setProperty(name, Long.toString(value));
    }

    /**
     * Reads a int value from the configuration properties.
     *
     * @param name The name of the property to be read.
     * @param fallback The default value to be returned, if the property could not be found.
     * @return The property value.
     */
    protected int getInt(final String name, final int fallback) {
        try {
            return Integer.parseInt(this.getProperty(name, Integer.toString(fallback)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return fallback;
        }
    }

    /**
     * Sets an integer value.
     *
     * @param name The name of the value.
     * @param value The value.
     */
    protected void setInt(final String name, final int value) {
        this.setProperty(name, Integer.toString(value));
    }

    /**
     * Reads a long value from the configuration properties.
     *
     * @param name The name of the property to be read.
     * @return The property value.
     */
    protected List<Long> getLongList(final String name) {
        final String value = this.getProperty(name);
        final List<Long> rc = new ArrayList<>();
        if (value.isEmpty()) {
            return rc;
        }

        for (final String entry: value.split("#")) {
            rc.add(Long.parseLong(entry));
        }
        return rc;
    }

    /**
     * Sets a value to a list of long entries.
     *
     * @param name The name of the field to be set.
     * @param entries The list of entries to be stored.
     */
    protected void setLongList(final String name, final List<Long> entries) {
        final List<String> stringList = new ArrayList<>();
        for (final Long entry: entries) {
            stringList.add(entry.toString());
        }
        this.setProperty(name, String.join("#", stringList));
    }


    /**
     * Sets the given string value.
     *
     * @param name Name of the value to be set.
     * @param value The value to be set.
     */
    protected void setString(final String name, final String value) {
        this.setProperty(name, value);
    }

    /**
     * Getter for the event name.
     *
     * @return Name of the event.
     */
    public String getName() {
        return this.getProperty(PROP_NAME);
    }

    /**
     * Getter for the account name of the organizer.
     *
     * @return Account name of the organizer.
     */
    public String getOrganizer() {
        return this.getProperty(PROP_ORGANIZER, null);
    }

    /**
     * Returns a property value.
     *
     * @param name The properties name.
     * @return The properties value.
     */
    private String getProperty(final String name) {
        return this.properties.getProperty(name);
    }

    /**
     * Returns a property value.
     *
     * @param name The properties name.
     * @param fallback The default value.
     * @return The properties value.
     */
    private String getProperty(String name, String fallback) {
        return this.properties.getProperty(name, fallback);
    }

    /**
     * Sets a property value.
     *
     * @param name The name of the value to be set.
     * @param value The value to be stored.
     */
    private void setProperty(String name, String value) {
        logger.info(String.format("%s: setting %s -> %s", this.getUuid(), name, value));
        if (!this.modified) {
            logger.info(String.format("%s: marking %s modified", this.getUuid(), this.getName()));
            this.modified = true;
        }
        this.properties.setProperty(name, value);
    }

    /**
     * Getter for the UUID of the event.
     *
     * @return UUID of the event.
     */
    public UUID getUuid() {
        final String uuid = this.getProperty(PROP_UUID);
        if (uuid == null) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        return UUID.fromString(this.getProperty(PROP_UUID));
    }

    /**
     * Loads the event content from file.
     *
     * @throws IOException Thrown when reading the file fails.
     */
    public void load() throws IOException {
        FileInputStream fis = new FileInputStream(this.getEventFile());
        this.properties.load(fis);
    }

    /**
     * Writes the event content to file.
     *
     * @throws IOException Thrown whan writing fails.
     */
    public void store() throws IOException {
        if (!this.modified) {
            logger.info(String.format("%s: data has not been modified. nothing to save here.", this.getUuid()));
            return;
        }

        this.createEventFolder();
        FileOutputStream fos = new FileOutputStream(this.getEventFile());
        this.properties.store(fos, "");

        this.modified = false;
        logger.info(String.format("%s: marking %s unmodified", this.getUuid(), this.getName()));
    }

    /**
     * Deletes the event.
     *
     * @returns True, if the file has been deleted.
     */
    public boolean delete() {
        return this.getEventFile().delete();
    }

    /**
     * Changes the event message ID.
     *
     * The message ID is the ID of the message associated with the event. This is what is shown on the discord client.
     *
     * @param messageId The new message id.
     */
    public void setMessageId(final Long messageId) {
        this.setLong(PROP_MESSAGE_ID, messageId);
    }

    /**
     * Stores the ID of the server the message has been posted on.
     *
     * @param serverId Id of the server the message has been posted on.
     */
    public void setServerId(final Long serverId) {
        this.setLong(PROP_SERVER_ID, serverId);
    }

    /**
     * Setter for the ID where the message has been created on.
     *
     * @param channelId Id of the channel of the message.
     */
    public void setChannelId(final Long channelId) {
        this.setLong(PROP_CHANNEL_ID, channelId);
    }

    /**
     * Getter for the server ID.
     *
     * @return The server ID or zero, if undefined.
     */
    public Long getServerId() {
        return this.getLong(PROP_SERVER_ID, 0);
    }

    /**
     * Getter for the channel ID.
     *
     * @return The channel ID.
     */
    public Long getChannelId() {
        return this.getLong(PROP_CHANNEL_ID, 0);
    }

    /**
     * Getter for the message ID.
     *
     * The message ID is the frontend part of the event presented to the user.
     *
     * @return The message ID.
     */
    public Long getMessageId() {
        return this.getLong(PROP_MESSAGE_ID, 0);
    }

    /**
     * Creates the folder where the event is stored in.
     *
     * @throws IOException Thrown whenever the folder could not be created.
     */
    protected void createEventFolder() throws IOException {
        final File eventFolder = this.eventFile.getParentFile();
        if(!eventFolder.exists()) {
            if (!eventFolder.mkdir()) {
                throw new IOException("Creating the output folder has failed.");
            }
        }
    }

    /**
     * Registers a participant on the given list.
     *
     * @param user The user to be registered.
     * @param list Name of the list.
     * @throws IOException This is thrown when writing the file fails.
     */
    protected void registerParticipant(final User user, final String list) throws IOException {
        final List<Long> participants = this.getLongList(list);
        final Long userId = user.getId();
        logger.info(String.format("%s: register %d as on %s", this.getUuid(), userId, list));
        if (!participants.contains(userId)) {
            participants.add(userId);
            this.setLongList(list, participants);
            this.store();
        }
    }

    /**
     * Unregisters a participant from the given list.
     *
     * @param user The user to be unregistered.
     * @param list Name of the list.
     * @throws IOException An IOException is thrown on writing errors.
     */
    protected void unregisterParticipant(final User user, final String list) throws IOException {
        final List<Long> participants = this.getLongList(list);
        final Long userId = user.getId();
        logger.info(String.format("%s: unregister %d as on %s", this.getUuid(), userId, list));
        if (participants.contains(userId)) {
            participants.remove(userId);
            this.setLongList(list, participants);
            this.store();
        }
    }

    /**
     * Returns a time representation of the event date.
     *
     * @return Time of the event.
     */
    public String getTime() {
        return this.dateUtil.getTime(this.getTimestamp());
    }

    /**
     * Gets the date as formatted string.
     *
     * @return Date as format string.
     */
    public String getDate() {
        return this.dateUtil.getDate(this.getTimestamp());
    }

    /**
     * Getter for the timestamp of the event.
     *
     * @return Timestamp of the event.
     */
    public long getTimestamp() {
        return this.getLong(PROP_DATE, 0);
    }

    /**
     * Retrieves the calendar object for the event.
     *
     * @return The calendar object for the event.
     */
    public Calendar getCalendar() {
        return this.dateUtil.calendarFromTimestamp(this.getTimestamp());
    }

    /**
     * Getter for the event description.
     *
     * @return Description of the event.
     */
    protected String getDescription() {
        final String description = this.getProperty(PROP_DESCRIPTION, "");
        if (description.isEmpty()) {
            return "";
        }
        return description + "\n";
    }

    /**
     * Updates the description.
     *
     * @param description New description.
     */
    protected void setDescription(String description) {
        this.setString(PROP_DESCRIPTION, description);
    }

    /**
     * Retrieves a list of all the registered event in the given folder.
     *
     * @param eventFolder Folder for the event type.
     * @return List of all registered event in the given folder.
     */
    protected static List<UUID> getEventList(File eventFolder) {
        if (!eventFolder.exists()) {
            return new ArrayList<>();
        }

        final File[] eventFiles = eventFolder.listFiles();
        if (eventFiles == null) {
            return new ArrayList<>();
        }

        final List<UUID> rc = new ArrayList<>(eventFiles.length);
        for (File file : eventFiles) {
            final UUID uuid = UUID.fromString(file.getName());
            rc.add(uuid);
        }
        return rc;
    }

    /**
     * Gets the rendered embed frame.
     *
     * @return Builder for the rendered embed frame.
     */
    public abstract EmbedBuilder getEmbed();

    /**
     * Message builder for the user list of the event.
     *
     * @param api The {@link DiscordApi} instance that will be used to send the message back.
     * @return The {@link MessageBuilder} instace for rendering the message content.
     */
    public abstract MessageBuilder getUserList(DiscordApi api);

    /**
     * Adds the default reactions to the message.
     *
     * @param message The message to add the default reactions on.
     */
    public abstract void addReactions(Message message);

    /**
     * Called to deliver a remote callback event to the implementation class.
     *
     * This method calls onAddReaction on the child class, if the added reaction is allowed for the message. It also
     * makes sure that not allowed reactions are always removed and it ensures to remove itself from the reactions
     * as soon as someone else has reacted to the message.
     *
     * @param event The event properties.
     * @param api Instance of the discord API.
     * @return True, if event has been handled by the class.
     */
    public boolean dispatchEvent(ReactionAddEvent event, DiscordApi api) {
        if (event.getMessageId() != this.getMessageId() || event.getUser() == api.getYourself()) {
            return false;
        }

        if (!this.isAllowedEmote(event.getEmoji())) {
            try {
                final Message message = event.requestMessage().get();
                message.removeReactionByEmoji(event.getEmoji()).join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }

        try {
            event.requestReaction().get().ifPresent(reaction -> {
                final int count = reaction.getCount();
                final boolean containsYou = reaction.containsYou();

                if (containsYou && count > 1) {
                    reaction.removeYourself();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return this.onAddReaction(event, api);
    }

    /**
     * Called to deliver a remote callback event to the implementation class.
     *
     * This method calls the onRemoveReaction method of the child class, if a allowed reaction is removed. It also
     * keeps track of readding a reaction whenever the counter reaches zero.
     *
     * @param event The event properties.
     * @param api Instance of the discord API.
     * @return True, if event has been handled by the class.
     */
    public boolean dispatchEvent(ReactionRemoveEvent event, DiscordApi api) {
        final String fmt = event.getEmoji().asUnicodeEmoji().get();
        if (event.getMessageId() != this.getMessageId() || event.getUser() == api.getYourself()) {
            return false;
        }

        if (!this.isAllowedEmote(event.getEmoji())) {
            return true; // if someone removes a remote that is not allowed, we ignore the action completely
        }

        try {
            final Message message = event.requestMessage().get();
            int count = event.requestCount().get();
            if (count < 1) {
                message.addReactions(event.getEmoji());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return this.onRemoveReaction(event, api);
    }

    /**
     * Writes a list of users to the message builder provided.
     *
     * @param name Name of the list as stored in the properties.
     * @param messageBuilder The message builder to write to.
     * @param api The discord api to access the remote service.
     */
    protected void renderUserList(String name, MessageBuilder messageBuilder, DiscordApi api) {
        for (long userId : this.getLongList(name)) {
                final String nickname = this.resolveName(userId, api);
                messageBuilder.append(String.format("- %s\n", nickname));
        }
    }

    private String resolveName(long userId, DiscordApi api) {
        try {
            final User user = api.getUserById(userId).get();
            do {
                final long serverId = this.getServerId();
                if (serverId == 0) {
                    logger.info("no server id found");
                    continue;
                }

                Optional<Server> optionalServer = api.getServerById(serverId);
                if (!optionalServer.isPresent()) {
                    logger.info(String.format("no server with id %d found", serverId));
                    continue;
                }

                Optional<String> optionalNicknname = optionalServer.get().getNickname(user);
                if (optionalNicknname.isPresent()) {
                    return optionalNicknname.get();
                }
                logger.info(String.format("no nickname present for %d", userId));
            } while(false);
            return user.getName();
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("error retrieving name for %d", userId), e);
            return "*[unknown]*";
        }
    }

    /**
     *
     * @param api
     * @param userId
     * @return
     */
    protected String getUserNick(DiscordApi api, long userId) {
        final long serverId = this.getServerId();
        if (serverId == 0) {
            try {
                return api.getUserById(userId).get().getName();
            } catch (Exception e) {
                logger.log(Level.SEVERE, String.format("%s: failed to retrieve user id", this.getUuid()), e);
            }
        }

        return null;
    }

    /**
     * Checks, if the given emote is allowed.
     *
     * @param emote The emote to be checked.
     * @return True, if the given emote is allowed on the message.
     */
    protected boolean isAllowedEmote(Emoji emote) {
        for (String allowed: allowedEmotes) {
            if (emote.equalsEmoji(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the allowed emotes on the event.
     *
     * @param allowedEmotes Allowed emotes on the event.
     */
    protected void setAllowedEmotes(String[] allowedEmotes) {
        this.allowedEmotes = allowedEmotes;
    }

    /**
     * Assembles the event title.
     *
     * @return The event title.
     */
    protected String getTitle() {
        return String.format("%s: %s (%s, %s)", this.getPrefix(), this.getName(), this.getDate(), this.getTime());
    }

    /**
     * Prefix for the event title.
     *
     * The prefix should define the type of the event.
     *
     * @return The prefix of the event.
     */
    protected String getPrefix() {
        final String type = this.getEventType();
        return type.substring(0, 1).toUpperCase() + type.substring(1);
    }

    /**
     * Called whenever a reaction is added from the associated message.
     *
     * @param event The remote event.
     * @param api The discord api.
     * @return True, if event has been handled.
     */
    protected abstract boolean onAddReaction(ReactionAddEvent event, DiscordApi api);

    /**
     * Called whenever a reaction is removed from the associated message.
     *
     * @param event The remote event.
     * @param api The discord api.
     * @return True, if event has been handled.
     */
    protected abstract boolean onRemoveReaction(ReactionRemoveEvent event, DiscordApi api);

    /**
     * Getter for a string property.
     *
     * @param name The name of the string to get.
     * @return The string value.
     */
    protected String getString(String name) {
        return this.getProperty(name);
    }

    /**
     * Creator for the given type of event.
     */
    static public abstract class Creator {
        /**
         * Returns the type name of the event.
         *
         * @return Type name of the event.
         */
        public abstract String getEventType();

        /**
         * Creates a new command instance based on the arguments supplied.
         *
         * @param command The command containing the arguments.
         * @return The created event instance of the given type.
         * @throws IOException Thrown whenever storing the event data is not possible.
         */
        public abstract GuildEvent newInstance(NewEventCommand command) throws IOException;

        /**
         * Creates a new event instance based on the UUID of the event.
         *
         * @param uuid The UUID of the event.
         * @return Instance of the event.
         * @throws IOException Thrown when reading the event is not possible.
         */
        public abstract GuildEvent newInstance(UUID uuid) throws IOException;

        /**
         * Returns a list of all stored events of the type.
         *
         * @return List of stored events.
         */
        public abstract List<UUID> listStoredEvents();
    }

    /**
     * Comparator to sort guild events based on their date.
     */
    public static Comparator<GuildEvent> compareEventTime = new Comparator<GuildEvent>() {
        @Override
        public int compare(GuildEvent event1, GuildEvent event2) {
            return Long.compare(event1.getTimestamp(), event2.getTimestamp());
        }
    };

    /**
     * Util for date conversion.
     */
    protected final DateUtil dateUtil = new DateUtil();
}
