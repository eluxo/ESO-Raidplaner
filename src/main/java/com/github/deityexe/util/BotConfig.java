package com.github.deityexe.util;

import com.github.deityexe.Main;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration file for the bot.
 *
 * This provides access to the values from the bot.properties file.
 */
public class BotConfig {
    /**
     * Class logger.
     */
    private static final Logger logger = Logger.getLogger(BotConfig.class.getName());

    private static final String PROP_TOKEN = "token";
    private static final String PROP_SERVER_ID = "serverId";
    private static final String PROP_CHANNEL_ID = "channelId";
    private static final String PROP_MANAGER_ROLE_ID = "managerRole";
    private static final String PROP_ADMIN_ID = "adminId";

    /**
     * Holds the content of the configuration file.
     */
    private Properties properties = new Properties();

    /**
     * Static instance of the bot configuration.
     */
    private static final BotConfig instance = new BotConfig();

    /**
     * Getter for the singleton instance of the bot configuration.
     *
     * @return Singleton instance of the bot configuration.
     */
    public static BotConfig getInstance() {
        return instance;
    }

    /**
     * Constructor. This loads the configuration.
     */
    private BotConfig() {
        try {
            logger.info("locate configuration file");
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File jarDir = jarFile.getParentFile();
            File propertiesFile = new File(jarDir, "bot.properties");

            logger.info("loading configuration from " + propertiesFile.getAbsolutePath());
            FileInputStream fis = new FileInputStream(propertiesFile);
            this.properties.load(fis);
        } catch (Exception e) {
            logger.warning("failed to load configuration.");
        }
    }

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
     * Retrieves the authentification token for the bot.
     *
     * @return The authentification token for the bot.
     */
    public String getBotToken() {
        return this.getProperty(PROP_TOKEN);
    }

    /**
     * Getter for the server id.
     *
     * @return Server id.
     */
    public Long getServerId() {
        return this.getLong(PROP_SERVER_ID, 0);
    }

    /**
     * Getter for the role of the managers.
     *
     * @return ID of the mangagers role.
     */
    public Long getManagerRoleId() {
        return this.getLong(PROP_MANAGER_ROLE_ID, 0);
    }

    /**
     * Getter for the ID of the bot admin.
     *
     * @return ID of the bot admin.
     */
    public Long getAdminId() {
        return this.getLong(PROP_ADMIN_ID, 0);
    }

    /**
     * ID of the channel the bot runs on.
     *
     * @return ID of the channel this bot is running on.
     */
    public Long getChannelId() {
        return this.getLong(PROP_CHANNEL_ID, 0);
    }
}
