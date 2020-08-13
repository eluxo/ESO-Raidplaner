package com.github.deityexe.command;

import com.github.deityexe.DeliverableError;
import com.github.deityexe.event.GuildEvent;
import com.github.deityexe.util.DateUtil;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Calendar;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.logging.Logger;

/**
 * Command to generate the new MOTD containing the different events for the next seven days starting at the given
 * date.
 */
public class GenerateMOTD extends CommandMessage {
    /**
     * Class logger.
     */
    private static final Logger logger = Logger.getLogger(GenerateMOTD.class.getName());

    /**
     * Number of required arguments.
     */
    private static final int ARGUMENT_COUNT = 1;

    /**
     * Field containing the start date for the MOTD entry.
     */
    private static final int ARG_START_DATE = 0;

    /**
     * Duration of the MOTD period in seconds.
     */
    private static final long PERIOD_DURATION = 7 * 24 * 60 * 60;

    /**
     * Constructor.
     *
     * @param command The command parameters.
     */
    public GenerateMOTD(CommandMessage command) {
        super(command);
        if (this.getArgs().length < ARGUMENT_COUNT) {
            throw new DeliverableError("Zu wenig Parameter angegeben.");
        }
    }

    /**
     * Getter for the start date of the MOTD entry.
     *
     * @return Start date for the MOTD entry.
     */
    public String getStartDate() {
        return this.arg(ARG_START_DATE);
    }

    @Override
    public void execute() {
        logger.info(String.format("creating MOTD beginning at %s", this.getStartDate()));

        final Calendar startDate = dateUtil.dateFromString(this.getStartDate());

        final long startTS = startDate.getTimeInMillis();
        final long endTS = startTS + (PERIOD_DURATION * 1000);
        final Calendar endDate = (Calendar) dateUtil.calendarFromTimestamp(endTS);

        logger.info(String.format("collect events between %s %s and %s %s",
                this.dateUtil.getDate(startDate), this.dateUtil.getTime(startDate),
                this.dateUtil.getDate(endDate),   this.dateUtil.getTime(endDate)));

        final ICommandEnvironment env = this.getCommandEnvironment();
        final Collection<GuildEvent> guildEvents = env.getGuildEvents();
        final PriorityQueue<GuildEvent> motdEvents = new PriorityQueue<>(1, GuildEvent.compareEventTime);

        for (GuildEvent event: guildEvents) {
            final long timestamp = event.getTimestamp();
            if (timestamp >= startTS && timestamp < endTS) {
                motdEvents.add(event);
            }
        }

        final MessageCreateEvent event = this.getMessageCreateEvent();
        if (motdEvents.size() == 0) {
            logger.info("no events found in the given period");
            event.getChannel().sendMessage(this.noEventsMotd(startDate, endDate));
        } else {
            logger.info(String.format("%d events in given period", motdEvents.size()));
            event.getChannel().sendMessage(this.motdEvents(startDate, endDate, motdEvents));
        }
    }


    private void writeHeader(final StringBuilder builder, final Calendar startDate, final Calendar endDate) {
        builder.append("```");
        builder.append("-------------------------------------------------------------------------------------\n");
        builder.append(String.format("|c00FF00Regelmäßige Gildenevents (%s - %s)|r\n",
                dateUtil.getDate(startDate), dateUtil.getDate(endDate)));
    }

    private void writeFooter(final StringBuilder builder) {
        builder.append("\n");
        builder.append("Bitte 15 Minuten vor Raidbeginn im TS einfinden!\n");
        builder.append("-------------------------------------------------------------------------------------\n");
        builder.append("```");
    }

    private String motdEvents(final Calendar startDate, final Calendar endDate, final Collection<GuildEvent> events) {
        final StringBuilder builder = new StringBuilder();
        this.writeHeader(builder, startDate, endDate);
        for (GuildEvent event: events) {
            final Calendar date = event.getCalendar();
            final String organizer = event.getOrganizer();
            builder.append(String.format("%s %s - %s - %s", this.dateUtil.getDowShort(date),
                    this.dateUtil.getDateShort(date), this.capitalize(event.getTypeName()), event.getName()));
            if (organizer != null) {
                builder.append(String.format(" (%s)\n", organizer));
            } else {
                builder.append("\n");
            }
        }
        this.writeFooter(builder);
        return builder.toString();
    }

    /**
     * Just convert the first character of the given string to an upper character.
     *
     * @param text The word to be capitalized.
     * @return The capitalized word.
     */
    private String capitalize(final String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private String noEventsMotd(final Calendar startDate, final Calendar endDate) {
        final StringBuilder builder = new StringBuilder();
        this.writeHeader(builder, startDate, endDate);
        builder.append("Noch keine Events für den angegebenen Zeitraum vorhanden.");
        this.writeFooter(builder);
        return builder.toString();
    }

    /**
     * Date utility.
     */
    private final DateUtil dateUtil = new DateUtil();
}
