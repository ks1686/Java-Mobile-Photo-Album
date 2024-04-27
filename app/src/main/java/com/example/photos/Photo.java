package com.example.photos;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Photo implements Serializable {

    private String filepath;
    private List<Map<String, String>> tags;

    public Photo(String filepath, List<Map<String, String>> tags) throws NullPointerException, IllegalArgumentException {
        /*File file = new File(filepath);

        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }*/

        if (filepath.isEmpty()) {
            throw new IllegalArgumentException("filepath cannot be empty");
        }

        /*if (!(filepath.endsWith(".bmp") || filepath.endsWith(".gif") || filepath.endsWith(".jpeg") || filepath.endsWith(".png") || filepath.endsWith(".jpg"))) {
            throw new IllegalArgumentException("File must be a BMP, GIF, JPEG, or PNG file");
        }*/

        if (tags == null) {
            this.tags = new ArrayList<>();
        } else {
            this.tags = tags;
        }

        this.filepath = filepath;
    }

    public Photo(String filepath) {
        this(filepath, new ArrayList<Map<String, String>>());
    }

    public Photo(String filepath, String caption) {
        this(filepath, new ArrayList<Map<String, String>>());
    }

    public void deleteTag(String key, String value) {
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).containsKey(key) && tags.get(i).containsValue(value)) {
                tags.remove(i);
                break;
            }
        }
    }

    public String getFilePath() {
        return filepath;
    }

    public List<Map<String, String>> getTags() {
        return tags;
    }

    public void addTag(String key, String value) throws NullPointerException {
        if (key == null) {
            throw new NullPointerException("key cannot be null");
        } else if (key.isEmpty()) {
            throw new IllegalArgumentException("key cannot be empty");
        }

        if (value == null) {
            throw new NullPointerException("value cannot be null");
        } else if (value.isEmpty()) {
            throw new IllegalArgumentException("value cannot be empty");
        }

        if (!key.equals("person") && !key.equals("location")) {
            throw new IllegalArgumentException("key must be either 'person' or 'location'");
        }

        Map<String, String> tag = new HashMap<>();
        tag.put(key, value);
        tags.add(tag);
    }

    public String toString() {
        return String.format("Photo: %s || Tags: %s", filepath, tags.toString());
    }
}