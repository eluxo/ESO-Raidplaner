package com.github.deityexe;

import java.io.*;

class Properties {

    private File configFile;
    private String prefix = "!";
    private String token;

    Properties() {
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File jarDir = jarFile.getParentFile();
            File propertiesFile = new File(jarDir, "bot.properties");
            //create File and set values
            if (propertiesFile.createNewFile()) {
                FileWriter fw = new FileWriter(propertiesFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fw);
                bufferedWriter.write("token = \"\"\nprefix = \"!\"\n");
                bufferedWriter.close();
                fw.close();
            }
            configFile = propertiesFile;

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
