package com.example.android.illon;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class Lot implements Serializable {
    private int id;
    private String name;
    private String about;
    private int min_value;
    private int value;
    private Date start_time;    //Date = util.Date
    private int id_winner;
    private String imagePath = null;
    private ArrayList<ProxyBitmap> images = null;

    public Lot(int id, String name, String about, int min_value, int value, Date start_time, int id_winner) {
        this.id = id;
        this.name = name;
        this.about = about;
        this.min_value = min_value;
        this.value = value;
        this.start_time = start_time;
        this.id_winner = id_winner;
    }

    public Lot(int id, String name, String about, int min_value, int value, Date start_time, int id_winner, String path) {
        this.id = id;
        this.name = name;
        this.about = about;
        this.min_value = min_value;
        this.value = value;
        this.start_time = start_time;
        this.id_winner = id_winner;
        imagePath = path;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public int getMin_value() {
        return min_value;
    }

    public void setMin_value(int min_value) {
        this.min_value = min_value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public int getId_winner() {
        return id_winner;
    }

    public void setId_winner(int id_user) {
        this.id_winner = id_user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lot lot = (Lot) o;
        return id == lot.id &&
                min_value == lot.min_value &&
                value == lot.value &&
                id_winner == lot.id_winner &&
                Objects.equals(name, lot.name) &&
                Objects.equals(about, lot.about) &&
                Objects.equals(start_time, lot.start_time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, about, min_value, value, start_time, id_winner);
    }

    @Override
    public String toString() {
        return "Lot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", about='" + about + '\'' +
                ", min_value=" + min_value +
                ", value=" + value +
                ", start_time=" + start_time +
                ", id_user=" + id_winner +
                '}';
    }

    public void setImages(ArrayList<Bitmap> images) {
        this.images = new ArrayList<>();
        for (int i=0;i<images.size();i++) {
            this.images.add(new ProxyBitmap(images.get(i)));
        }
    }

    public ArrayList<Bitmap> getImages() {
        ArrayList<Bitmap> imgs = new ArrayList<>();
        for(int i=0;i<images.size();i++) {
            imgs.add(images.get(i).getBitmap());
        }
        return imgs;
    }

}
