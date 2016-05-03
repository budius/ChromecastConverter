package com.budius.chromecast.converter;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by budius on 27.04.16.
 */
public class Log {

   public static void init(File folder, boolean debug) {
      instance = new Log(folder);
      Log.debug = debug;
   }

   private static boolean debug = false;
   private static Log instance;

   public static boolean isDebug() {
      return debug;
   }

   public static Log get() {
      return instance;
   }

   private Logger error = Logger.getLogger("CC_error");
   private Logger warn = Logger.getLogger("CC_warn");

   private Log(File folder) {
      setup(warn, folder, "ChromecastConvert_WARNING");
      setup(error, folder, "ChromecastConvert_ERROR");
   }

   private static void setup(Logger logger, File folder, String fileName) {
      try {
         FileHandler fh = new FileHandler(new File(folder, fileName + ".log").getAbsolutePath());
         fh.setFormatter(new SimpleFormatter());
         logger.addHandler(fh);
         logger.setUseParentHandlers(false);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void d(String msg) {
      if (debug) {
         System.out.println(msg);
      }
   }

   public static void i(String msg) {
      System.out.println(msg);
   }

   public static void w(String msg) {
      System.out.println("WARNING: " + msg);
      instance.warn.warning(msg);
   }

   public static void e(String msg) {
      System.out.println("ERROR: " + msg);
      instance.error.severe(msg);
   }

}
