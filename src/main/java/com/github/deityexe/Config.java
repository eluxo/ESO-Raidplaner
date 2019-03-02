package com.github.deityexe;

import java.io.*;

class Config {

    private File configFile;
    private String prefix = "!";
    private String token;

    Config() {
        try {
            File jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File iniFile = new File(jarDir, "config.ini");
            //create File and set values
            if (iniFile.createNewFile()) {
                FileWriter fw = new FileWriter(iniFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fw);
                bufferedWriter.write("prefix = \"!\"\ntoken = \"\"\n");
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
                    String[] split = line.split("\"");
                    if (split.length > 1) {
                        prefix = line.split("\"")[1];
                    }
                }
                if (line.startsWith("token")) {
                    String[] split = line.split("\"");
                    if (split.length > 1) {
                        token = line.split("\"")[1];
                    }
                }
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    String getPrefix() {
        return prefix;
    }

    String getToken() {
        return token;
    }

}
