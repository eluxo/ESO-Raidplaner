package com.github.deityexe;

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

public class Raid {
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
     * @param args 1 = name, 2 = date, 3 = amount tanks, 4 = amount supports, 5 = amount DD
     */
    public Raid (String[] args) {
        try {
            this.name = args[1];
            //create Calendar
            String[] date = args[2].split("\\.");
            this.date = Calendar.getInstance();
            this.date.set(Integer.valueOf(date[2]),Integer.valueOf(date[1]) - 1, Integer.valueOf(date[0]));

            this.tanks = Integer.valueOf(args[3]);
            this.supports = Integer.valueOf(args[4]);
            this.dds = Integer.valueOf(args[5]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        //create Raidfile
        try {
            File jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File raidFolder = new File(jarDir, "raids");
            if(!raidFolder.exists()) {
                if (!raidFolder.mkdir()) {
                    System.out.println("Creating Raid folder failed!");
                }
            }
            this.raidfile = new File(raidFolder,this.name + ".raid");
            this.raidfile.createNewFile();
            //write raid info into file
            try (FileWriter fw = new FileWriter(raidfile); BufferedWriter bufferedWriter = new BufferedWriter(fw)) {
                bufferedWriter.write("name = \"" + this.name + "\"\n");
                bufferedWriter.write("date = " + this.date.getTimeInMillis() + "\n");
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
    public Raid (File rfile) {
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

    public String getName() {
        return name;
    }

    public Calendar getDate() {
        return date;
    }

    public int getTanks() {
        return tanks;
    }

    public int getSupports() {
        return supports;
    }

    public int getDds() {
        return dds;
    }

    public Long getRaidmessageID() {
        return raidmessageID;
    }

    public void setRaidmessageID(Message raidmessage) {
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

    public void registerTank(User usr) {
        if (!this.registeredTanks.contains(usr.getId())) {
            this.registeredTanks.add(usr.getId());

            try {
                List<String> lines = Files.readAllLines(raidfile.toPath());     //dirty solution
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).startsWith("registeredTanks")) {
                        String line = "registeredTanks = #";
                        for (long id : this.registeredTanks) {
                            line = line + id + "#";
                        }
                        lines.set(i,line);
                    }
                }
                Files.write(raidfile.toPath(),lines);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerSupport(User usr) {
        if (!this.registeredSupports.contains(usr.getId())) {
            this.registeredSupports.add(usr.getId());

            try {
                List<String> lines = Files.readAllLines(raidfile.toPath());     //dirty solution
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).startsWith("registeredSupports")) {
                        String line = "registeredSupports = #";
                        for (long id : this.registeredSupports) {
                            line = line + id + "#";
                        }
                        lines.set(i,line);
                    }
                }
                Files.write(raidfile.toPath(),lines);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerDD(User usr) {
        if (!this.registeredDDs.contains(usr.getId())) {
            this.registeredDDs.add(usr.getId());

            try {
                List<String> lines = Files.readAllLines(raidfile.toPath());     //dirty solution
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).startsWith("registeredDDs")) {
                        String line = "registeredDDs = #";
                        for (long id : this.registeredDDs) {
                            line = line + id + "#";
                        }
                        lines.set(i,line);
                    }
                }
                Files.write(raidfile.toPath(),lines);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Long> getRegisteredTanks() {
        return registeredTanks;
    }

    public List<Long> getRegisteredSupports() {
        return registeredSupports;
    }

    public List<Long> getRegisteredDDs() {
        return registeredDDs;
    }

    public EmbedBuilder getEmbed() {
        return new EmbedBuilder()
                        .setColor(Color.white)
                        .setTitle(this.getName() + " (" + this.getDate().get(Calendar.DATE) + "." + (this.getDate().get(Calendar.MONTH) + 1) + "." + this.getDate().get(Calendar.YEAR) + ")")
                        .setDescription("Gesuchte Rollen:\n" + this.getTanks() + " Tanks " + roleEmote[0] +", " + this.getSupports() + " Healer " + roleEmote[1] + ", " + this.getDds() + " DDs " + roleEmote[2] + ".");
    }

}
