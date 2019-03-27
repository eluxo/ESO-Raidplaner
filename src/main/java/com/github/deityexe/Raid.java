package com.github.deityexe;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import javax.management.Descriptor;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.Properties;

import static com.github.deityexe.Main.roleEmote;

class Raid {
    private static final String PROP_NAME = "name";
    private static final String PROP_DATE = "date";
    private static final String PROP_TANKS_REGISTERED = "tanksRegistered";
    private static final String PROP_TANKS_REQUIRED = "tanksRequired";
    private static final String PROP_DDS_REGISTERED = "ddsRegistered";
    private static final String PROP_DDS_REQUIRED = "ddsRequired";
    private static final String PROP_SUPPORTS_REGISTERED = "supportsRegistered";
    private static final String PROP_SUPPORTS_REQUIRED = "supportsRequired";
    private static final String PROP_RAID_MESSAGE_ID = "raidMessageId";
    private static final String PROP_UUID = "uuid";

    private static final File RAID_FOLDER = new File("raids");


    private final File raidFile;
    private String name = "";
    private Calendar date = Calendar.getInstance();
    private long raidmessageID = 0;
    private UUID uuid;
    private int tanksRequired;
    private int ddsRequired;
    private int supportsRequired;
    private List<Long> tanksRegistered = new ArrayList<>();
    private List<Long> supportsRegistered = new ArrayList<>();
    private List<Long> ddsRegistered = new ArrayList<>();

    /**
     * Holding the raid configuration properties. This is used to load and store the raid configuration.
     */
    private Properties raidProperties = new Properties();

    /**
     * Construct Raid Object from arguments
     * @param args 1 = name, 2 = date, 3 = time, 4 = amount tanks, 5 = amount supports, 6 = amount DD
     */
    Raid(String[] args) throws IOException, NumberFormatException {
        this.uuid = UUID.randomUUID();
        this.raidFile = this.createRaidFileName();

        this.name = args[1];
        //create Calendar
        String[] date = args[2].split("\\.");
        String[] time = args[3].split(":");
        this.date = Calendar.getInstance();
        //noinspection MagicConstant
        this.date.set(Integer.valueOf(date[2]),Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]), Integer.valueOf(time[0]), Integer.valueOf(time[1]));
        this.tanksRequired = Integer.parseInt(args[4]);
        this.supportsRequired = Integer.parseInt(args[5]);
        this.ddsRequired = Integer.parseInt(args[6]);

        if (raidFile.exists()) {
            throw new IOException("File already exists. Creation failed.");
        }

        this.storeRaid();
    }

    /**
     * Creates the raid file name.
     *
     * @return The raid file name.
     */
    private File createRaidFileName() {
        return new File(RAID_FOLDER, this.uuid.toString());
    }

    /**
     * Constructs Raid Object from file
     * @param raidFile Raid file in raids folder
     * @throws IOException Raised when the raid could not be loaded from file.
     */
    Raid(File raidFile) throws IOException {
        this.raidFile = raidFile;
        this.loadRaid();
    }


    /**
     * This stores the raid to the configuration file.
     *
     * @throws IOException Thrown when the raid could not be stored.
     */
    private void storeRaid() throws IOException {
        this.raidProperties.setProperty(PROP_NAME, this.name);
        this.raidProperties.setProperty(PROP_DATE, Long.toString(this.date.getTimeInMillis()));
        this.raidProperties.setProperty(PROP_RAID_MESSAGE_ID, Long.toString(this.raidmessageID));
        this.raidProperties.setProperty(PROP_TANKS_REGISTERED, this.joinIdList(this.tanksRegistered));
        this.raidProperties.setProperty(PROP_DDS_REGISTERED, this.joinIdList(this.ddsRegistered));
        this.raidProperties.setProperty(PROP_SUPPORTS_REGISTERED, this.joinIdList(this.supportsRegistered));
        this.raidProperties.setProperty(PROP_UUID, this.uuid.toString());
        this.raidProperties.setProperty(PROP_TANKS_REQUIRED, Long.toString(this.tanksRequired));
        this.raidProperties.setProperty(PROP_DDS_REQUIRED, Long.toString(this.ddsRequired));
        this.raidProperties.setProperty(PROP_SUPPORTS_REQUIRED, Long.toString(this.supportsRequired));

        this.createRaidFolder();
        this.raidProperties.store(new FileOutputStream(this.raidFile), "");
    }

    /**
     * Creates the raid folder, if it does not exist yet.
     *
     * @throws IOException Thrown, if the output folder could not be created.
     */
    private void createRaidFolder() throws IOException {
        if(!RAID_FOLDER.exists()) {
            if (!RAID_FOLDER.mkdir()) {
                throw new IOException("Creating the output folder has failed.");
            }
        }
    }

    /**
     * Reloads the raid object from file.
     *
     * @throws IOException Thrown when the raid file could not be loaded.
     */
    private void loadRaid() throws IOException {
        this.raidProperties = new Properties();
        this.raidProperties.load(new FileReader(raidFile));

        this.name = this.raidProperties.getProperty(PROP_NAME);
        this.date = Calendar.getInstance();
        this.date.setTimeInMillis(Long.valueOf(this.raidProperties.getProperty(PROP_DATE)));
        this.tanksRegistered = this.splitIdList(this.raidProperties.getProperty(PROP_TANKS_REGISTERED));
        this.tanksRequired = Integer.parseInt(this.raidProperties.getProperty(PROP_TANKS_REQUIRED));
        this.ddsRegistered = this.splitIdList(this.raidProperties.getProperty(PROP_DDS_REGISTERED));
        this.ddsRequired = Integer.parseInt(this.raidProperties.getProperty(PROP_SUPPORTS_REQUIRED));
        this.supportsRegistered = this.splitIdList(this.raidProperties.getProperty(PROP_SUPPORTS_REGISTERED));
        this.supportsRequired = Integer.parseInt(this.raidProperties.getProperty(PROP_DDS_REQUIRED));
        this.raidmessageID = Long.valueOf(this.raidProperties.getProperty(PROP_RAID_MESSAGE_ID));
        this.uuid = UUID.fromString(this.raidProperties.getProperty(PROP_UUID));
    }

    private List<Long> splitIdList(final String entries) {
        List<Long> rc = new ArrayList<>();
        for (String entry: entries.split("#")) {
            rc.add(Long.parseLong(entry));
        }
        return rc;
    }

    private String joinIdList(final List<Long> entries) {
        List<String> stringList = new ArrayList<>();
        for (Long entry: entries) {
            stringList.add(entry.toString());
        }
        return String.join("#", stringList);
    }

    String getName() {
        return name;
    }

    Calendar getDate() {
        return date;
    }

    Long getRaidmessageID() {
        return raidmessageID;
    }

    void setRaidmessageID(Message raidmessage) throws IOException {
        this.raidmessageID = raidmessage.getId();
        this.storeRaid();
    }

    void registerTank(User usr) throws IOException {
        if (!this.tanksRegistered.contains(usr.getId())) {
            this.tanksRegistered.add(usr.getId());
            this.storeRaid();
        }
    }

    void removeTank(User usr) throws IOException {
        if (this.tanksRegistered.contains(usr.getId())) {
            this.tanksRegistered.remove(usr.getId());
            this.storeRaid();
        }
    }

    void registerSupport(User usr) throws IOException {
        if (!this.supportsRegistered.contains(usr.getId())) {
            this.supportsRegistered.add(usr.getId());
            this.storeRaid();
        }
    }

    void removeSupport(User usr) throws IOException {
        if (this.supportsRegistered.contains(usr.getId())) {
            this.supportsRegistered.remove(usr.getId());
            this.storeRaid();
        }
    }

    void registerDD(User usr) throws IOException {
        if (!this.ddsRegistered.contains(usr.getId())) {
            this.ddsRegistered.add(usr.getId());
            this.storeRaid();
        }
    }

    void removeDD(User usr) throws IOException {
        if (this.ddsRegistered.contains(usr.getId())) {
            this.ddsRegistered.remove(usr.getId());
            this.storeRaid();
        }
    }

    String getTime() {
        if (this.date.get((Calendar.MINUTE)) < 10) {
            return this.date.get(Calendar.HOUR_OF_DAY) + ":0" + this.date.get(Calendar.MINUTE);
        } else {
            return this.date.get(Calendar.HOUR_OF_DAY) + ":" + this.date.get(Calendar.MINUTE);
        }
    }

    EmbedBuilder getEmbed() {
        return new EmbedBuilder()
                        .setColor(Color.white)
                        .setTitle(this.name + " (" + this.date.get(Calendar.DATE) + "." + (this.date.get(Calendar.MONTH) + 1) + "." + this.date.get(Calendar.YEAR) + ", " + this.getTime() + ")")
                        .setDescription("Gesuchte Rollen:\n" + this.tanksRequired + " Tanks " + roleEmote[0] +", " + this.supportsRequired + " Healer " + roleEmote[1] + ", " + this.ddsRequired + " DDs " + roleEmote[2] + ".");
    }

    MessageBuilder getUserList(DiscordApi api) {
        MessageBuilder msgbuilder = new MessageBuilder().append("__**Angemeldete Spieler f\u00fcr \"" + this.name + "\":**__\n\n");
        //add Tanks
        msgbuilder.append("**Tanks " + roleEmote[0] + "**\n");
        for (long usrID : this.tanksRegistered) {
            try {
                msgbuilder.append("- " + api.getUserById(usrID).get().getName() + "\n");
            } catch (Exception e) {
                msgbuilder.append("- *[deleted]*\n");
                e.printStackTrace();
            }
        }
        msgbuilder.appendNewLine();
        //add Healer
        msgbuilder.append("**Healer " + roleEmote[1] + "**\n");
        for (long usrID : this.supportsRegistered) {
            try {
                msgbuilder.append("- " + api.getUserById(usrID).get().getName() + "\n");
            } catch (Exception e) {
                msgbuilder.append("- *Missing User*\n");
                e.printStackTrace();
            }
        }
        msgbuilder.appendNewLine();
        //add DD
        msgbuilder.append("**DDs " + roleEmote[2] + "**\n");
        for (long usrID : this.ddsRegistered) {
            try {
                msgbuilder.append("- " + api.getUserById(usrID).get().getName() + "\n");
            } catch (Exception e) {
                msgbuilder.append("- *Missing User*\n");
                e.printStackTrace();
            }
        }
        return  msgbuilder;
    }

    boolean deleteRaid() {
        return this.raidFile.delete();
    }

    /**
     * Returns the folder to be used to store the raids.
     *
     * @return Folder to store the raids in.
     */
    public static final File getRaidFolder() {
        return RAID_FOLDER;
    }

    /**
     * Constructs the list of raids currently registered.
     *
     * @return List of registered raids.
     */
    public static List<Raid> getRaidList() {
        List<Raid> rc = new ArrayList<>();
        File raidFolder = getRaidFolder();
        File[] raidFiles = raidFolder.listFiles();
        if (!raidFolder.exists() || raidFiles == null) {
            return rc;
        }

        for(File f : raidFiles){
            try {
                rc.add(new Raid(f));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return rc;
    }

}
