package com.github.deityexe;

import java.io.*;

public class Config {

    private File configFile;
    private String prefix;

    public Config() {
        try {
            File jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File iniFile = new File(jarDir, "config.ini");
            //create File and set values
            if (iniFile.createNewFile()) {
                FileWriter fw = new FileWriter(iniFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fw);
                bufferedWriter.write("prefix = \"!\"\n");
                bufferedWriter.close();
                fw.close();
            }
            configFile = iniFile;

            FileReader fr = new FileReader(configFile);
            BufferedReader bufferedReader = new BufferedReader(fr);
            //get settings
            String line = bufferedReader.readLine();
            while (line != null) {
                if (line.startsWith("prefix")) {
                    prefix = line.split("\"")[1];
                }
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getPrefix() {
        return prefix;
    }

}
