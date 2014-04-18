package com.budius.chromecast.converter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by budius on 06.04.14.
 */
public class ArgsSettings implements Settings.Interface {

    private CommandLine cmd;
    private static final Options OPTIONS;


    private static final List<String> QUALITY = Arrays.asList(Settings.ARRAY_QUALITY);
    private static final List<String> SPEED = Arrays.asList(Settings.ARRAY_SPEED);

    private static String fromArray(String[] array) {
        return Arrays.toString(array).replace("[", "").replace("]", "");
    }

    static {
        OPTIONS = new Options();
        OPTIONS.addOption("i", true, "Input File or Folder");
        OPTIONS.addOption("o", true, "Output Folder (if different)");
        OPTIONS.addOption("d", false, "Add this flag to delete the original file upon successful conversion");
        OPTIONS.addOption("q", true, "Quality: " + fromArray(Settings.ARRAY_QUALITY) + ". Default is high");
        OPTIONS.addOption("s", true, "Speed: " + fromArray(Settings.ARRAY_SPEED) + ". Default is slow");
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
            String q = cmd.getOptionValue("q").toLowerCase();
            if (QUALITY.contains(q)) {
                return QUALITY.indexOf(q);
            }
        }
        return Settings.getDefaultSettings().getQuality();
    }

    @Override
    public int getSpeed() {
        if (cmd.hasOption("s")) {
            String s = cmd.getOptionValue("s").toLowerCase();
            if (SPEED.contains(s)) {
                return SPEED.indexOf(s);
            }
        }
        return Settings.getDefaultSettings().getSpeed();
    }

    public static Options getOptions() {
        return OPTIONS;
    }
}
