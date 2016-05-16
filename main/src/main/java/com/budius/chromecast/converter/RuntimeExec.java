package com.budius.chromecast.converter;

import java.io.File;
import java.io.IOException;

/**
 * Created by budius on 3/12/14.
 */
public class RuntimeExec {

   private final String[] cmd;
   private final File folder;
   private int logType;

   private Gobbler inputGobbler;
   private Gobbler errorGobbler;

   public static final int LOG = 1;
   public static final int FFMPEG = 2; // Fancy log logic
   public static final int STRING_RESPONSE = 3;

   public static String get(String[] cmd) {
      RuntimeExec rt = new RuntimeExec(cmd, null, RuntimeExec.STRING_RESPONSE);
      if (rt.execute()) {
         return rt.getResponse();
      } else {
         return null;
      }
   }

   public RuntimeExec(String[] cmd, File folder, int logType) {
      this.cmd = cmd;
      this.folder = folder;
      this.logType = logType;

      inputGobbler = new Gobbler(logType);
      errorGobbler = new Gobbler(logType);
   }

   public boolean execute() {

      Log.i(" ");
      Log.i("Executing: " + Utils.cmdToString(cmd));


      boolean success = false;
      long startTime = System.currentTimeMillis();

      try {
         Runtime rt = Runtime.getRuntime();
         Process p;
         if (folder == null)
            p = rt.exec(cmd);
         else
            p = rt.exec(cmd, null, folder);

         StreamGobbler input = new StreamGobbler("input", p.getInputStream(), inputGobbler);
         StreamGobbler error = new StreamGobbler("error", p.getErrorStream(), errorGobbler);

         input.start();
         error.start();

         try {
            int val = p.waitFor();
            success = (val == 0);

            input.join();
            error.join();
            p.destroy();

            if (logType == FFMPEG) {
               System.out.print("\n");
            }

         } catch (InterruptedException e) {
            Log.d("InterruptedException. " + e.getMessage());
         }
      } catch (IOException e) {
         Log.d("IOException. " + e.getMessage());
      }

      Log.i(String.format("Execution finished with %s in %s seconds", (success ? "success" : "fail"), Long.toString((System.currentTimeMillis() - startTime) / 1000)));

      return success;
   }

   public String getResponse() {
      return inputGobbler.getResponse();
   }

   public String getError() {
      return errorGobbler.getResponse();
   }

   private static class Gobbler implements StreamGobbler.OnLineListener {

      private final int logType;
      private final StringBuilder response;

      private Gobbler(int logType) {
         this.logType = logType;
         if (this.logType == STRING_RESPONSE) {
            response = new StringBuilder();
         } else {
            response = null;
         }
      }

      @Override public void onLine(String line) {
         switch (logType) {
            case LOG:
               Log.d(line);
               break;
            case FFMPEG:
               logFFmpeg(line);
               break;
            case STRING_RESPONSE:
               if (response != null) {
                  response.append(line);
               }
               break;
         }
      }

      String getResponse() {
         return response == null ? null : response.toString();
      }

      private int last_msg_length;

      private void logFFmpeg(String msg) {
         if (Log.isDebug()) {
            Log.d(msg);
            return;
         }

         if ((msg.startsWith("frame="))) {
            System.out.print("\r" + msg + space(last_msg_length - msg.length() + 3));
            last_msg_length = msg.length();
         } else {
            //  System.out.print("\n" + msg);
            last_msg_length = 0;
         }
      }
   }

   private static String space(int length) {
      if (length <= 0) return "   ";
      StringBuilder outputBuffer = new StringBuilder(length);
      for (int i = 0; i < length; i++) {
         outputBuffer.append(" ");
      }
      return outputBuffer.toString();
   }

}
