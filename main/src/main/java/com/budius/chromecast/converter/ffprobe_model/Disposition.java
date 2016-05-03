
package com.budius.chromecast.converter.ffprobe_model;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Disposition {

    @SerializedName("default")
    @Expose
    private long _default;
    @Expose
    private long dub;
    @Expose
    private long original;
    @Expose
    private long comment;
    @Expose
    private long lyrics;
    @Expose
    private long karaoke;
    @Expose
    private long forced;
    @Expose
    private long hearing_impaired;
    @Expose
    private long visual_impaired;
    @Expose
    private long clean_effects;
    @Expose
    private long attached_pic;

    public long getDefault() {
        return _default;
    }

    public void setDefault(long _default) {
        this._default = _default;
    }

    public long getDub() {
        return dub;
    }

    public void setDub(long dub) {
        this.dub = dub;
    }

    public long getOriginal() {
        return original;
    }

    public void setOriginal(long original) {
        this.original = original;
    }

    public long getComment() {
        return comment;
    }

    public void setComment(long comment) {
        this.comment = comment;
    }

    public long getLyrics() {
        return lyrics;
    }

    public void setLyrics(long lyrics) {
        this.lyrics = lyrics;
    }

    public long getKaraoke() {
        return karaoke;
    }

    public void setKaraoke(long karaoke) {
        this.karaoke = karaoke;
    }

    public long getForced() {
        return forced;
    }

    public void setForced(long forced) {
        this.forced = forced;
    }

    public long getHearing_impaired() {
        return hearing_impaired;
    }

    public void setHearing_impaired(long hearing_impaired) {
        this.hearing_impaired = hearing_impaired;
    }

    public long getVisual_impaired() {
        return visual_impaired;
    }

    public void setVisual_impaired(long visual_impaired) {
        this.visual_impaired = visual_impaired;
    }

    public long getClean_effects() {
        return clean_effects;
    }

    public void setClean_effects(long clean_effects) {
        this.clean_effects = clean_effects;
    }

    public long getAttached_pic() {
        return attached_pic;
    }

    public void setAttached_pic(long attached_pic) {
        this.attached_pic = attached_pic;
    }

}
