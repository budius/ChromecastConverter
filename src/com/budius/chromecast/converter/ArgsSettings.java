package com.budius.chromecast.converter;

import org.apache.commons.cli.*;

import java.io.File;
import java.util.Locale;

/**
 * Created by budius on 06.04.14.
 */
public class ArgsSettings implements Settings.Interface {

    private CommandLine cmd;
    private static final Options OPTIONS;

    private static final String Q_HIGH = "HIGH";
    private static final String Q_SUPER = "SUPER";
    private static final String Q_NORMAL = "NORMAL";

    static {
        OPTIONS = new Options();
        OPTIONS.addOption("i", true, "Input File or Folder");
        OPTIONS.addOption("o", true, "Output Folder (if different)");
        OPTIONS.addOption("d", false, "Add this flag to delete the original file upon successful conversion");
        OPTIONS.addOption("q", true, "Quality: SUPER, HIGH, NORMAL. Default is HIGH");
    }

    public static ArgsSettings build(String[] args) {
        ArgsSettings a = new ArgsSettings();
        try {
            a.init(args);
            return a;
        } catch (Exception e) {
            System.err.println("Could not parse args. " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private File input, output;

    public File getInput() {
        return input;
    }

    public File getOutput() {
        return output;
    }

    private void init(String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();
        cmd = parser.parse(OPTIONS, args);
        input = getInputFile();
        output = getOutputFolder();
    }

    private File getInputFile() {
        String filename = cmd.getOptionValue("i", null);
        if (filename == null)
            throw new IllegalArgumentException("-i must be a valid file or folder path");
        File f = new File(filename);
        if (!f.exists())
            throw new IllegalArgumentException("-i must be a valid file or folder path");
        return f;
    }

    private File getOutputFolder() {
        if (cmd.hasOption("o")) {
            File f = new File(cmd.getOptionValue("o"));
            if (f.exists() && f.isDirectory())
                return f;
            else throw new IllegalArgumentException("-o must be a valid folder path");
        } else {
            File f = getInputFile();
            return f.isDirectory() ? f : f.getParentFile();
        }
    }

    @Override
    public boolean deleteOriginalFileOnSuccessfulConversion() {
        return cmd.hasOption("d");
    }

    @Override
    public int getQuality() {
        if (cmd.hasOption("q")) {
            String q = cmd.getOptionValue("q");
            if (Q_HIGH.equals(q.toUpperCase(Locale.ENGLISH))) return Settings.QUALITY_HIGH;
            if (Q_SUPER.equals(q.toUpperCase(Locale.ENGLISH))) return Settings.QUALITY_SUPER;
            if (Q_NORMAL.equals(q.toUpperCase(Locale.ENGLISH))) return Settings.QUALITY_NORMAL;
            return Settings.getDefaultSettings().getQuality();
        } else {
            return Settings.getDefaultSettings().getQuality();
        }
    }

    public static Options getOptions() {
        return OPTIONS;
    }
}
