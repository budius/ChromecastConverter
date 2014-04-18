package com.budius.chromecast.converter;

/**
 * Created by budius on 06.04.14.
 */
public class Settings {

    private static final long K = 1024;

    public static final String[] ARRAY_SPEED = {
            "ultrafast",
            "superfast",
            "veryfast",
            "faster",
            "fast",
            "medium",
            "slow",
            "slower",
            "veryslow"};

    public static final String[] ARRAY_QUALITY = {
            "super", "high", "normal", "same_size"};

    public static final int SPEED_ULTRA_FAST = 0;
    public static final int SPEED_SUPER_FAST = 1;
    public static final int SPEED_VERY_FAST = 2;
    public static final int SPEED_FASTER = 3;
    public static final int SPEED_FAST = 4;
    public static final int SPEED_MEDIUM = 5;
    public static final int SPEED_SLOW = 6;
    public static final int SPEED_SLOWER = 7;
    public static final int SPEED_VERY_SLOW = 8;

    public static final int QUALITY_SUPER = 0;
    public static final int QUALITY_HIGH = 1;
    public static final int QUALITY_NORMAL = 2;
    public static final int QUALITY_SAME_FILE_SIZE = 3;

    public interface Interface {
        public boolean deleteOriginalFileOnSuccessfulConversion();

        public int getQuality();

        public int getSpeed();
    }

    public static Interface getDefaultSettings() {
        return DEFAULT_SETTINGS;
    }

    private static final Interface DEFAULT_SETTINGS = new Interface() {
        @Override
        public boolean deleteOriginalFileOnSuccessfulConversion() {
            return true;
        }

        @Override
        public int getQuality() {
            return Settings.QUALITY_HIGH;
        }

        @Override
        public int getSpeed() {
            return Settings.SPEED_MEDIUM;
        }
    };

    public static long getMaxPerChannelAudioBitRate(Interface settings) {
        switch (settings.getQuality()) {
            case QUALITY_NORMAL:
                return 96 * K; // 192k stereo
            case QUALITY_SUPER:
                return 256 * K; // 512K stereo
            case QUALITY_HIGH:
            default:
                return 192 * K; // 384K stereo
        }
    }

    public static long getDefaultPerChannelAudioBitRate() {
        return 96 * K; // 192k stereo
    }
}

