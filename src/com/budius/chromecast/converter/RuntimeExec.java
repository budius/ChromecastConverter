package com.budius.chromecast.converter;

import java.io.File;
import java.io.IOException;

/**
 * Created by budius on 3/12/14.
 */
public class RuntimeExec {

    private final String[] CMD;
    private final File folder;
    private StringBuilder inputSb = null;
    private StringBuilder errSb = null;
    private int logType;

    public static final int DEBUG = 1;
    public static final int VERBOSE = 2;
    public static final int STRING_RESPONSE = 4;

    public RuntimeExec(String[] cmd, File folder, int logType) {
        CMD = cmd;
        this.folder = folder;
        this.logType = logType;
        if ((logType & STRING_RESPONSE) > 0) {
            inputSb = new StringBuilder();
            errSb = new StringBuilder();
        }
    }

    public boolean execute() {
        try {
            Runtime rt = Runtime.getRuntime();
            Process p;
            if (folder == null)
                p = rt.exec(CMD);
            else
                p = rt.exec(CMD, null, folder);

            StreamGobbler input = new StreamGobbler("input", p.getInputStream(), inputListener);
            StreamGobbler error = new StreamGobbler("error", p.getErrorStream(), errListener);

            input.start();
            error.start();

            try {
                int val = p.waitFor();
                boolean success = val == 0;

                input.join();
                error.join();
                p.destroy();

                Log.v("Runtime execution finished with " + (success ? "success" : "fail " + val));
                return success;
            } catch (InterruptedException e) {
                Log.e("InterruptedException. " + e.getMessage());
            }
        } catch (IOException e) {
            Log.e("IOException. " + e.getMessage());
        }
        return false;
    }

    public String getResponse() {
        return inputSb.toString();
    }

    public String getError() {
        return errSb.toString();
    }

    private StreamGobbler.OnLineListener inputListener = new StreamGobbler.OnLineListener() {
        @Override
        public void onLine(String line) {

            if ((logType & DEBUG) > 0) {
                System.out.println(line);
                Log.d(line);
            }
            if ((logType & VERBOSE) > 0) {
                Log.v(line);
            }
            if ((logType & STRING_RESPONSE) > 0) {
                if (inputSb != null)
                    inputSb.append(line);
            }

        }
    };

    private StreamGobbler.OnLineListener errListener = new StreamGobbler.OnLineListener() {
        @Override
        public void onLine(String line) {

            if ((logType & DEBUG) > 0) {
                System.out.println(line);
                Log.d(line);
            }
            if ((logType & VERBOSE) > 0) {
                Log.v(line);
            }
            if ((logType & STRING_RESPONSE) > 0) {
                if (errSb != null)
                    errSb.append(line);
            }

        }
    };

}
