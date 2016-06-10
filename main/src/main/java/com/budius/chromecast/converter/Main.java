package com.budius.chromecast.converter;

import org.apache.commons.cli.HelpFormatter;

/**
 * Parse the parameters into settings and send for execution
 */
public class Main {

   public static final String VERSION = "Chromecast Converter - V2.1.0";

   public static void main(String[] args) {

      System.out.println(VERSION);

      // parse arguments
      Arguments arguments = new Arguments(args);
      Settings settings = arguments.getSettings();
      if (settings == null) {

         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("./cc_converter <opts>", Arguments.OPTIONS);

         System.exit(0);
         return;
      }

      System.out.println(String.format(
         "Converting %s into %s with %s speed and quality %s. %s",
         settings.input.getAbsolutePath(),
         settings.output.getAbsolutePath(),
         settings.speed,
         settings.quality,
         settings.delete ? "Delete upon successful conversion." : "Do not delete anything."
      ));

      // Log internally is keeping a self-reference, it's logger, can be static
      Log.init(settings.input.isDirectory() ? settings.input : settings.input.getParentFile(), arguments.isDebug());

      // execute
      new Execution(settings).run();

      System.exit(0);

   }
}
