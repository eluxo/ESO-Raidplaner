package com.github.deityexe.event;

import com.github.deityexe.command.NewEventCommand;

import java.io.IOException;
import java.util.*;

/**
 * This factory is used to generate different types of events based on their name.
 */
public class EventFactory {
    /**
     * List of all event creators.
     */
    private static final List<GuildEvent.Creator> EVENT_CREATOR_LIST;

    static {
        EVENT_CREATOR_LIST = new ArrayList<>();
        EVENT_CREATOR_LIST.add(Raid.CREATOR);
        EVENT_CREATOR_LIST.add(WorlbossRun.CREATOR);
    }

    /**
     * Constructor.
     */
    public EventFactory() {
    }

    /**
     * Creates a new event of the given type.
     *
     * @param command The command to create the event on.
     * @return Instance of the created event. If the event type is unknown, this method returns null.
     * @throws IOException Thrown if event could not be stored.
     */
    public GuildEvent createEvent(NewEventCommand command) throws IOException {
        for (GuildEvent.Creator creator: EVENT_CREATOR_LIST) {
            if (creator.getEventType().equals(command.getType())) {
                return creator.newInstance(command);
            }
        }

        return null;
    }

    /**
     * Creates a list of all available events.
     *
     * @return List of all available events.
     */
    public Map<UUID, GuildEvent> findAllEvents() {
        HashMap<UUID, GuildEvent> rc = new HashMap<>();
        for (GuildEvent.Creator creator: EVENT_CREATOR_LIST) {
            for (UUID uuid: creator.listStoredEvents()) {
                try {
                    rc.put(uuid, creator.newInstance(uuid));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return rc;
    }
}
