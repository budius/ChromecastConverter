package com.budius.chromecast.converter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log utils
 */
public class Log {

    private static final int MAX_LOG = 666;

    private static final String SEPARATOR = " - ";
    private static final String PROCESSING_1 = "size=";
    private static final String PROCESSING_2 = "frame=";

    public static ObservableList<String> verbose = FXCollections.observableArrayList();
    public static ObservableList<String> debug = FXCollections.observableArrayList();
    private static String lastDebug = "";

    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    private static LogListener listener;

    private static synchronized String getTime() {
        return format.format(new Date());
    }

    private static String getText(String text) {
        return getTime() + SEPARATOR + text;
    }

    public void setListener(LogListener listener) {
        this.listener = listener;
    }

    public interface LogListener {
        public void v(String v);

        public void e(String e);

        public void d(String d);
    }

    public static void clear() {
        verbose.clear();
        debug.clear();
    }

    public static void v(final String v) {
        System.out.println(v);
        if(listener != null)
            listener.v(v);
        Platform.runLater(new Runnable() {
                              @Override
                              public void run() {


                                  if (v.startsWith(PROCESSING_1) || v.startsWith(PROCESSING_2)) {

                                      // do the verbose
                                      if (verbose.size() > 0) {
                                          if ((v.startsWith(PROCESSING_1) && verbose.get(0).startsWith(PROCESSING_1)) ||
                                                  (v.startsWith(PROCESSING_2) && verbose.get(0).startsWith(PROCESSING_2))) {
                                              verbose.remove(0);
                                          }
                                      }

                                      // do the log
                                      if ((v.startsWith(PROCESSING_1) && lastDebug.startsWith(PROCESSING_1)) ||
                                              (v.startsWith(PROCESSING_2) && lastDebug.startsWith(PROCESSING_2))) {
                                          debug.remove(0);
                                      }
                                      lastDebug = v;
                                      debug.add(0, getText(v));
                                  }

                                  verbose.add(0, v);
                                  if (verbose.size() > MAX_LOG)
                                      verbose.remove(verbose.size() - 1);
                              }
                          }
        );
    }

    public static void e(final String s) {
        System.err.println("[ERROR] " + s);
        d("[ERROR] " + s);
    }

    public static void d(final String d) {
        System.out.println(d);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                lastDebug = d;
                debug.add(0, getText(d));
                if (debug.size() > MAX_LOG)
                    debug.remove(debug.size() - 1);
            }
        });
    }

    private static final String logFileName = "ChromecastConverterError.log";
    private static File logFile;

    public static void setFileLogPath(File folder) {
        if (folder == null || !folder.isDirectory())
            return;
        if (!folder.exists())
            folder.mkdirs();
        logFile = new File(folder, logFileName);
    }

    public static void fileLog(String s) {
        if (logFile == null || s == null)
            return;
        try {
            FileWriter fw = new FileWriter(logFile, true);
            fw.append(getText(s));
            fw.append("\n");
            fw.flush();
            fw.close();
        } catch (IOException e) {
        }
    }

}
