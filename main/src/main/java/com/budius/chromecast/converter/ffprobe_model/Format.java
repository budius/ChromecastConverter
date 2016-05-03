
package com.budius.chromecast.converter.ffprobe_model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Format {

    @Expose
    private String filename;
    @Expose
    private long nb_streams;
    @Expose
    private long nb_programs;
    @Expose
    private String format_name;
    @Expose
    private String format_long_name;
    @Expose
    private String start_time;
    @Expose
    private String duration;
    @Expose
    private String size;
    @Expose
    private String bit_rate;
    @Expose
    private long probe_score;
    @Expose
    private Tags_ tags;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getNb_streams() {
        return nb_streams;
    }

    public void setNb_streams(long nb_streams) {
        this.nb_streams = nb_streams;
    }

    public long getNb_programs() {
        return nb_programs;
    }

    public void setNb_programs(long nb_programs) {
        this.nb_programs = nb_programs;
    }

    public String getFormat_name() {
        return format_name;
    }

    public void setFormat_name(String format_name) {
        this.format_name = format_name;
    }

    public String getFormat_long_name() {
        return format_long_name;
    }

    public void setFormat_long_name(String format_long_name) {
        this.format_long_name = format_long_name;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getBit_rate() {
        return bit_rate;
    }

    public void setBit_rate(String bit_rate) {
        this.bit_rate = bit_rate;
    }

    public long getProbe_score() {
        return probe_score;
    }

    public void setProbe_score(long probe_score) {
        this.probe_score = probe_score;
    }

    public Tags_ getTags() {
        return tags;
    }

    public void setTags(Tags_ tags) {
        this.tags = tags;
    }

}
