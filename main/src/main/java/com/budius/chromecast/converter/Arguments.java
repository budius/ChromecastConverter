package com.budius.chromecast.converter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.List;

/**
 * Created by budius on 27.04.16.
 */
public class Arguments {

   public static final Options OPTIONS;

   private static String fromArray(List<String> list) {
      return list.toString().replace("[", "").replace("]", "");
   }

   static {
      OPTIONS = new Options();
      OPTIONS.addOption("i", "input", true, "Input file or folder");
      OPTIONS.addOption("o", "output", true, "Output folder (optional)");
      OPTIONS.addOption("q", "quality", true, "Quality: " + fromArray(Settings.QUALITY) + ". Default is high (optional)");
      OPTIONS.addOption("s", "speed", true, "Speed: " + fromArray(Settings.SPEED) + ". Default is slow (optional)");
      OPTIONS.addOption("d", "delete", false, "Delete the original file upon successful conversion");
      OPTIONS.addOption("f", "force", false, "Force conversion (even if input codecs are correct)");
      OPTIONS.addOption("p", "pi", false, "Uses h264_omx to allow conversion on the Raspberry Pi");
      OPTIONS.addOption(null, "DEBUG", false, "Debug mode with more logs");
      OPTIONS.addOption(null, "onlySubtitles", false, "Do no convert anything, simply extract subtitles");
   }

   private Settings settings;
   private boolean debug;

   public Settings getSettings() {
      return settings;
   }

   public boolean isDebug() {
      return debug;
   }

   public Arguments(String[] args) {

      try {
         CommandLineParser parser = new DefaultParser();
         CommandLine cmd = parser.parse(OPTIONS, args);
         String inputString = cmd.getOptionValue("i", null);
         String outputString = cmd.getOptionValue("o", null);
         String qualityString = cmd.getOptionValue("q", Settings.DEFAULT_QUALITY);
         String speedString = cmd.getOptionValue("s", Settings.DEFAULT_SPEED);
         boolean delete = cmd.hasOption("d");
         boolean force = cmd.hasOption("f");
         boolean pi = cmd.hasOption("p");
         boolean onlySubtitles = cmd.hasOption("onlySubtitles");
         debug = cmd.hasOption("DEBUG");

         // test valid input
         if (inputString == null) {
            System.out.println("Input File or Folder cannot be empty");
            return;
         }

         File inputFile = new File(inputString);
         if (!inputFile.exists()) {
            System.out.println("Input File or Folder cannot be empty");
            return;
         }

         // test valid output
         File outputFile = null;
         if (outputString != null) {
            outputFile = new File(outputString);
            if (outputFile.exists()) {
               if (!outputFile.isDirectory()) {
                  System.out.println("Output must be a folder");
                  return;
               }
            } else {
               outputFile.mkdirs();
            }
         } else {
            if (inputFile.isDirectory()) {
               outputFile = inputFile;
            } else {
               outputFile = inputFile.getParentFile();
            }
         }

         // test valid quality
         int quality;
         if (Settings.QUALITY.contains(qualityString)) {
            quality = Settings.I_ARRAY_QUALITY[Settings.QUALITY.indexOf(qualityString)];
         } else {
            try {
               quality = Integer.parseInt(qualityString);
            } catch (NumberFormatException nfe) {
               System.out.println("Invalid quality argument. " + nfe.toString());
               return;
            }
         }

         // test valid speed
         if (!Settings.SPEED.contains(speedString)) {
            System.out.println("Invalid speed argument");
            return;
         }

         settings = new Settings(inputFile, outputFile, speedString, quality, delete, force, pi, onlySubtitles);

      } catch (ParseException e) {
         e.printStackTrace();
         return;
      }
   }
}
