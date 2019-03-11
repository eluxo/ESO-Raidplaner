package com.github.deityexe;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.github.deityexe.Main.roleEmote;

class Raid {
    private String name;
    private Calendar date;
    private int tanks, supports, dds;
    private File raidfile;
    private long raidmessageID;
    private List<Long> registeredTanks = new ArrayList<>();
    private List<Long> registeredSupports = new ArrayList<>();
    private List<Long> registeredDDs = new ArrayList<>();


    /**
     * Construct Raid Object from arguments
     * @param args 1 = name, 2 = date, 3 = time, 4 = amount tanks, 5 = amount supports, 6 = amount DD
     */
    Raid(String[] args) {
        try {
            this.name = args[1];
            //create Calendar
            String[] date = args[2].split("\\.");
            String[] time = args[3].split(":");
            this.date = Calendar.getInstance();
            //noinspection MagicConstant
            this.date.set(Integer.valueOf(date[2]),Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]), Integer.valueOf(time[0]), Integer.valueOf(time[1]));

            this.tanks = Integer.valueOf(args[4]);
            this.supports = Integer.valueOf(args[5]);
            this.dds = Integer.valueOf(args[6]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        //create Raidfile
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File jarDir = jarFile.getParentFile();
            File raidFolder = new File(jarDir, "raids");
            if(!raidFolder.exists()) {
                if (!raidFolder.mkdir()) {
                    System.out.println("Creating raids folder failed!");
                }
            }
            this.raidfile = new File(raidFolder,this.name + ".raid");
            if(!this.raidfile.exists()) {
                if(!this.raidfile.createNewFile()) {
                    System.out.println("Creating raid file failed!");
                }
            }

            //write raid info into file
            try (FileWriter fw = new FileWriter(raidfile); BufferedWriter bufferedWriter = new BufferedWriter(fw)) {
                bufferedWriter.write("name = \"" + this.name + "\"\n");
                bufferedWriter.write("date = " + (this.date != null ? this.date.getTimeInMillis() : 0) + "\n");
                bufferedWriter.write("tanks = " + this.tanks + "\n");
                bufferedWriter.write("supports = " + this.supports + "\n");
                bufferedWriter.write("dds = " + this.dds + "\n");
                bufferedWriter.write("messageID = \n");
                bufferedWriter.write("registeredTanks = \n");
                bufferedWriter.write("registeredSupports = \n");
                bufferedWriter.write("registeredDDs = \n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs Raid Object from file
     * @param rfile Raid file in raids folder
     */
    Raid(File rfile) {
        try (FileReader fr = new FileReader(rfile); BufferedReader bufferedReader = new BufferedReader(fr)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                if(line.startsWith("name")) {
                    this.name = line.split("\"")[1];
                }
                if (line.startsWith("date")) {
                    this.date = Calendar.getInstance();
                    try {
                        this.date.setTimeInMillis(Long.valueOf(line.split("=")[1].replace(" ", "")));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (line.startsWith("tanks")) {
                    try {
                        this.tanks = Integer.valueOf(line.split("=")[1].replace(" ", ""));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (line.startsWith("supports")) {
                    try {
                        this.supports = Integer.valueOf(line.split("=")[1].replace(" ", ""));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (line.startsWith("dds")) {
                    try {
                        this.dds = Integer.valueOf(line.split("=")[1].replace(" ", ""));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (line.startsWith("messageID")) {
                    this.raidmessageID = Long.valueOf(line.split("=")[1].replace(" ",""));
                }
                if (line.startsWith("registeredTanks")) {
                    String[] ids = line.split("#"); //starts at 1
                    for (int i = 1; i < ids.length; i++) {
                        this.registeredTanks.add(Long.valueOf(ids[i]));
                    }
                }
                if (line.startsWith("registeredSupports")) {
                    String[] ids = line.split("#"); //starts at 1
                    for (int i = 1; i < ids.length; i++) {
                        this.registeredSupports.add(Long.valueOf(ids[i]));
                    }
                }
                if (line.startsWith("registeredDDs")) {
                    String[] ids = line.split("#"); //starts at 1
                    for (int i = 1; i < ids.length; i++) {
                        this.registeredDDs.add(Long.valueOf(ids[i]));
                    }
                }
                line = bufferedReader.readLine();
            }
            this.raidfile = rfile;
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    void setRaidmessageID(Message raidmessage) {
        this.raidmessageID = raidmessage.getId();

        try {
            List<String> lines = Files.readAllLines(raidfile.toPath());     //dirty solution
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("messageID")) {
                    lines.set(i,"messageID = " + this.raidmessageID);
                }
            }
            Files.write(raidfile.toPath(),lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void registerTank(User usr) {
        if (!this.registeredTanks.contains(usr.getId())) {
            this.registeredTanks.add(usr.getId());
            this.updateTankListFile();
        }
    }

    void removeTank(User usr) {
        if (this.registeredTanks.contains(usr.getId())) {
            this.registeredTanks.remove(usr.getId());
            this.updateTankListFile();
        }
    }

    private void updateTankListFile() {
        try {
            List<String> lines = Files.readAllLines(raidfile.toPath());     //dirty solution
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("registeredTanks")) {
                    StringBuilder line = new StringBuilder("registeredTanks = #");
                    for (long id : this.registeredTanks) {
                        line.append(id).append("#");
                    }
                    lines.set(i, line.toString());
                }
            }
            Files.write(raidfile.toPath(),lines);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void registerSupport(User usr) {
        if (!this.registeredSupports.contains(usr.getId())) {
            this.registeredSupports.add(usr.getId());
            this.updateSupportListFile();
        }
    }

    void removeSupport(User usr) {
        if (this.registeredSupports.contains(usr.getId())) {
            this.registeredSupports.remove(usr.getId());
            this.updateSupportListFile();
        }
    }

    private void updateSupportListFile() {
        try {
            List<String> lines = Files.readAllLines(raidfile.toPath());     //dirty solution
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("registeredSupports")) {
                    StringBuilder line = new StringBuilder("registeredSupports = #");
                    for (long id : this.registeredSupports) {
                        line.append(id).append("#");
                    }
                    lines.set(i, line.toString());
                }
            }
            Files.write(raidfile.toPath(),lines);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void registerDD(User usr) {
        if (!this.registeredDDs.contains(usr.getId())) {
            this.registeredDDs.add(usr.getId());
            this.updateDDListFile();
        }
    }

    void removeDD(User usr) {
        if (this.registeredDDs.contains(usr.getId())) {
            this.registeredDDs.remove(usr.getId());
            this.updateDDListFile();
        }
    }

    private void updateDDListFile() {
        try {
            List<String> lines = Files.readAllLines(raidfile.toPath());     //dirty solution
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("registeredDDs")) {
                    StringBuilder line = new StringBuilder("registeredDDs = #");
                    for (long id : this.registeredDDs) {
                        line.append(id).append("#");
                    }
                    lines.set(i, line.toString());
                }
            }
            Files.write(raidfile.toPath(),lines);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<Long> getRegisteredTanks() {
        return registeredTanks;
    }

    List<Long> getRegisteredSupports() {
        return registeredSupports;
    }

    List<Long> getRegisteredDDs() {
        return registeredDDs;
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
                        .setDescription("Gesuchte Rollen:\n" + this.tanks + " Tanks " + roleEmote[0] +", " + this.supports + " Healer " + roleEmote[1] + ", " + this.dds + " DDs " + roleEmote[2] + ".");
    }

    MessageBuilder getUserList(DiscordApi api) {
        MessageBuilder msgbuilder = new MessageBuilder().append("__**Angemeldete Spieler f\u00fcr \"" + this.name + "\":**__\n\n");
        //add Tanks
        msgbuilder.append("**Tanks " + roleEmote[0] + "**\n");
        for (long usrID : this.registeredTanks) {
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
        for (long usrID : this.registeredSupports) {
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
        for (long usrID : this.registeredDDs) {
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
        return this.raidfile.delete();
    }

}
