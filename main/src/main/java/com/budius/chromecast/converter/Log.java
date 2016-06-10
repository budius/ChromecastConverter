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

   private File folder;
   private Logger error;
   private Logger warn;

   private Log(File folder) {
      this.folder = folder;
   }

   // static =======================================================================================
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
      if (instance.warn == null) {
         instance.warn = setup(instance.folder, "ChromecastConvert_WARNING");
      }
      if (instance.warn != null) {
         instance.warn.warning(msg);
      }
   }

   public static void e(String msg) {
      System.out.println("ERROR: " + msg);
      if (instance.error == null) {
         instance.error = setup(instance.folder, "ChromecastConvert_ERROR");
      }
      if (instance.error != null) {
         instance.error.severe(msg);
      }
   }

   private static Logger setup(File folder, String fileName) {
      try {
         FileHandler fh = new FileHandler(new File(folder, fileName + ".log").getAbsolutePath());
         fh.setFormatter(new SimpleFormatter());
         Logger logger = Logger.getLogger(fileName);
         logger.addHandler(fh);
         logger.setUseParentHandlers(false);
         return logger;
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

}
