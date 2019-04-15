package com.github.deityexe.command;

import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;

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
}
