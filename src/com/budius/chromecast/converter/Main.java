package com.budius.chromecast.converter;

import org.apache.commons.cli.*;

/**
 * Created by budius on 05.04.14.
 */
public class Main {


    public static final String VERSION = "Chromecast Converter - V1.1.1";


    public static void main(String[] args) {

        System.out.println(VERSION);

        if (args == null || args.length == 0 ||
                args.length == 1 && (
                        args[0].equals("-h") || args[0].equals("-help")
                )) {

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", ArgsSettings.getOptions());

            System.exit(0);
            return;
        }

        // with arguments, let's process them
        ArgsSettings s = ArgsSettings.build(args);
        if (s == null) {
            System.exit(1);
            return;
        }

        settings = s;

        ExecutionControl ec = new ExecutionControl(s.getInput(), s.getOutput(), null);
        ec.run();
    }

    private static Settings.Interface settings;

    public static Settings.Interface getSettings() {
        if (settings == null) return Settings.getDefaultSettings();
        return settings;
    }

    public static void setSettings(Settings.Interface settings) {
        Main.settings = settings;
    }

}

