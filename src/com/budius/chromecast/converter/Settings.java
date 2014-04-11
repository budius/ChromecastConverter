package com.budius.chromecast.converter;

/**
 * Created by budius on 06.04.14.
 */
public class Settings {

    static final long K = 1024;

    public static final int QUALITY_SUPER = 1;
    public static final int QUALITY_HIGH = 2;
    public static final int QUALITY_NORMAL = 3;

    public interface Interface {
        public boolean deleteOriginalFileOnSuccessfulConversion();

        public int getQuality();
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
    };


    public static long getMaxVideoBitRate(Interface settings) {
        // TODO: change this to use some proportion with pixels area (e.g. 1920*1080 = 2073600 sqPixels). More pixels needs more bits
        switch (settings.getQuality()) {
            case QUALITY_NORMAL:
                return 777 * K; // equals to fit 2 hours movie in 700 mb
            case QUALITY_SUPER:
                return 3111 * K; // TODO: test with Game of Thrones.S01E01.MKV
            case QUALITY_HIGH:
            default:
                return 1555 * K; // equals to fit 2 hours movie in 1.4 GB
        }
    }

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
        return 64 * K;
    }
}

