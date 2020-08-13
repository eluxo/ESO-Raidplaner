package com.github.deityexe.command;

import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.user.User;

import java.util.Collection;
import java.util.UUID;

/**
 * Command environment for the execution of a command.
 */
public interface ICommandEnvironment {
    /**
     * Getter for the guild events.
     *
     * @return List of guild events.
     */
    Collection<GuildEvent> getGuildEvents();

    /**
     * Getter for the discord API.
     *
     * @return The discord API.
     */
    DiscordApi getDiscordApi();

    /**
     * Searches an event by its name.
     *
     * @param name The name of the event.
     * @return Returns the event with the given name.
     */
    GuildEvent eventByName(final String name);

    /**
     * Seaches an event by its UUID.
     *
     * @param uuid The UUID to lookup.
     * @return The event with the given UUID.
     */
    GuildEvent eventByUuid(final UUID uuid);

    /**
     * Adds the given guild event to the environment.
     *
     * @param guildEvent The event to be added.
     */
    void addEvent(GuildEvent guildEvent);

    /**
     * Removes the event from the list of events.
     *
     * @param guildEvent The event to be removed.
     */
    void removeEvent(GuildEvent guildEvent);

    /**
     * Retrieve the server object.
     *
     * @return The server object.
     */
    Server getServer();

    /**
     * Retrieves the channel the bot is using.
     *
     * @return The channel of the bot.
     */
    Channel getChannel();

    /**
     * Retrieves the channel the bot is using as text channel.
     *
     * @return The text channel of the bot.
     */
    TextChannel getTextChannel();

    /**
     * Checks, if the given user has the manager role.
     *
     * @param user The user to check for the manager role.
     * @return True, if the user has the manager role.
     */
    boolean isManager(User user);

    /**
     * Checks, if the user has the admin role.
     *
     * @param user The user to check for the admin role.
     * @return True, if user has the admin role.
     */
    boolean isAdmin(User user);
}
