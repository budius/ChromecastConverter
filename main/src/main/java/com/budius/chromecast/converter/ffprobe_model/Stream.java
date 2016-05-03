
package com.budius.chromecast.converter.ffprobe_model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;

@Generated("org.jsonschema2pojo")
public class Stream {

    @Expose
    private long index;
    @Expose
    private String codec_name;
    @Expose
    private String codec_long_name;
    @Expose
    private String profile;
    @Expose
    private String codec_type;
    @Expose
    private String codec_time_base;
    @Expose
    private String codec_tag_string;
    @Expose
    private String codec_tag;
    @Expose
    private long width;
    @Expose
    private long height;
    @Expose
    private long has_b_frames;
    @Expose
    private String sample_aspect_ratio;
    @Expose
    private String display_aspect_ratio;
    @Expose
    private String pix_fmt;
    @Expose
    private long level;
    @Expose
    private String r_frame_rate;
    @Expose
    private String avg_frame_rate;
    @Expose
    private String time_base;
    @Expose
    private long start_pts;
    @Expose
    private String start_time;
    @Expose
    private String bit_rate;
    @Expose
    private Disposition disposition;
    @Expose
    private Tags tags;
    @Expose
    private String sample_fmt;
    @Expose
    private String sample_rate;
    @Expose
    private long channels;
    @Expose
    private String channel_layout;
    @Expose
    private long bits_per_sample;
    @Expose
    private long duration_ts;
    @Expose
    private String duration;

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public String getCodec_name() {
        return codec_name;
    }

    public void setCodec_name(String codec_name) {
        this.codec_name = codec_name;
    }

    public String getCodec_long_name() {
        return codec_long_name;
    }

    public void setCodec_long_name(String codec_long_name) {
        this.codec_long_name = codec_long_name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getCodec_type() {
        return codec_type;
    }

    public void setCodec_type(String codec_type) {
        this.codec_type = codec_type;
    }

    public String getCodec_time_base() {
        return codec_time_base;
    }

    public void setCodec_time_base(String codec_time_base) {
        this.codec_time_base = codec_time_base;
    }

    public String getCodec_tag_string() {
        return codec_tag_string;
    }

    public void setCodec_tag_string(String codec_tag_string) {
        this.codec_tag_string = codec_tag_string;
    }

    public String getCodec_tag() {
        return codec_tag;
    }

    public void setCodec_tag(String codec_tag) {
        this.codec_tag = codec_tag;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getHas_b_frames() {
        return has_b_frames;
    }

    public void setHas_b_frames(long has_b_frames) {
        this.has_b_frames = has_b_frames;
    }

    public String getSample_aspect_ratio() {
        return sample_aspect_ratio;
    }

    public void setSample_aspect_ratio(String sample_aspect_ratio) {
        this.sample_aspect_ratio = sample_aspect_ratio;
    }

    public String getDisplay_aspect_ratio() {
        return display_aspect_ratio;
    }

    public void setDisplay_aspect_ratio(String display_aspect_ratio) {
        this.display_aspect_ratio = display_aspect_ratio;
    }

    public String getPix_fmt() {
        return pix_fmt;
    }

    public void setPix_fmt(String pix_fmt) {
        this.pix_fmt = pix_fmt;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
    }

    public String getR_frame_rate() {
        return r_frame_rate;
    }

    public void setR_frame_rate(String r_frame_rate) {
        this.r_frame_rate = r_frame_rate;
    }

    public String getAvg_frame_rate() {
        return avg_frame_rate;
    }

    public void setAvg_frame_rate(String avg_frame_rate) {
        this.avg_frame_rate = avg_frame_rate;
    }

    public String getTime_base() {
        return time_base;
    }

    public void setTime_base(String time_base) {
        this.time_base = time_base;
    }

    public long getStart_pts() {
        return start_pts;
    }

    public void setStart_pts(long start_pts) {
        this.start_pts = start_pts;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getBit_rate() {
        return bit_rate;
    }

    public void setBit_rate(String bit_rate) {
        this.bit_rate = bit_rate;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public void setDisposition(Disposition disposition) {
        this.disposition = disposition;
    }

    public Tags getTags() {
        return tags;
    }

    public void setTags(Tags tags) {
        this.tags = tags;
    }

    public String getSample_fmt() {
        return sample_fmt;
    }

    public void setSample_fmt(String sample_fmt) {
        this.sample_fmt = sample_fmt;
    }

    public String getSample_rate() {
        return sample_rate;
    }

    public void setSample_rate(String sample_rate) {
        this.sample_rate = sample_rate;
    }

    public long getChannels() {
        return channels;
    }

    public void setChannels(long channels) {
        this.channels = channels;
    }

    public String getChannel_layout() {
        return channel_layout;
    }

    public void setChannel_layout(String channel_layout) {
        this.channel_layout = channel_layout;
    }

    public long getBits_per_sample() {
        return bits_per_sample;
    }

    public void setBits_per_sample(long bits_per_sample) {
        this.bits_per_sample = bits_per_sample;
    }

    public long getDuration_ts() {
        return duration_ts;
    }

    public void setDuration_ts(long duration_ts) {
        this.duration_ts = duration_ts;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

}
