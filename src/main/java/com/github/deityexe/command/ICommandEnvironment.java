package com.github.deityexe.command;

import com.github.deityexe.event.GuildEvent;
import org.javacord.api.DiscordApi;

import java.util.List;

/**
 * Command environment for the execution of a command.
 */
public interface ICommandEnvironment {
    /**
     * Getter for the guild events.
     *
     * @return List of guild events.
     */
    List<GuildEvent> getGuildEvents();

    /**
     * Getter for the discord API.
     *
     * @return The discord API.
     */
    DiscordApi getDiscordApi();
}
