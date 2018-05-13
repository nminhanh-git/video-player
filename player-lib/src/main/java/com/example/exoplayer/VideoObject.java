package com.example.exoplayer;

public class VideoObject {
    private String name;
    private String duration;
    private String date;
    private String path;
    private String size;

    public VideoObject() {
    }

    public VideoObject(String name, String duration, String date, String path, String size) {
        this.name = name;
        this.duration = duration;
        this.date = date;
        this.path = path;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
