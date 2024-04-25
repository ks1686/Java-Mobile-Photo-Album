package com.example.photos;

// Java imports

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a photo. A photo has a file path, a caption, and a list of tags.
 * A photo can be created with a file path, a caption, and a list of tags. A photo can have its caption changed.
 * A photo can have tags added to it and removed from it. A photo can have its file
 * path, caption, and tags retrieved. A photo can have a tag removed based on its key.
 * A photo can have a tag removed based on its key and value.
 * A photo can be created with just a file path. A photo can be created with a file path and a caption.
 * A photo can be created with a file path and a list of tags. A photo can have a tag added to it.
 * A photo can have its caption set. A photo can have a tag removed based on its key and value.
 *
 * @author jacobjude
 * @author ks1686
 */

public class Photo implements Serializable {

    private final String filepath;
    private final List<Map<String, String>> tags;

    /**
     * Creates a photo with a file path, a caption, and a list of tags.
     * The file path must be a valid file path to a BMP, GIF, JPEG, or PNG file.
     * The caption must not be null. The tags must not be null.
     * The date of the photo is set to the last modified date of the file.
     * The date is set to the last modified date of the file.
     * The milliseconds, seconds, hour, and minute are set to 0.
     *
     * @param filepath the file path of the photo
     * @param caption  the caption of the photo
     * @param tags     the tags of the photo
     * @throws NullPointerException     if the caption is null
     * @throws IllegalArgumentException if the file path is empty
     * @throws IllegalArgumentException if the file path does not end with .bmp, .gif, .jpeg, or .png
     * @throws IllegalArgumentException if the file does not exist
     */
    public Photo(String filepath, String caption, List<Map<String, String>> tags) throws NullPointerException, IllegalArgumentException {
        File file = new File(filepath);

        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }

        if (filepath.isEmpty()) {
            throw new IllegalArgumentException("filepath cannot be empty");
        }

        if (!(filepath.endsWith(".bmp") || filepath.endsWith(".gif") || filepath.endsWith(".jpeg") || filepath.endsWith(".png") || filepath.endsWith(".jpg"))) {
            throw new IllegalArgumentException("File must be a BMP, GIF, JPEG, or PNG file");
        }

        if (caption == null) {
            throw new NullPointerException("caption cannot be null");
        }

        if (tags == null) {
            this.tags = new ArrayList<>();
        } else {
            this.tags = tags;
        }


        this.filepath = filepath;
    }

    /**
     * Creates a photo with just a file path.
     *
     * @param filepath the file path of the photo
     */
    public Photo(String filepath) {
        this(filepath, "", new ArrayList<>());
    }

    /**
     * Creates a photo with a file path and a caption.
     *
     * @param filepath the file path of the photo
     */
    public Photo(String filepath, String caption) {
        this(filepath, caption, new ArrayList<>());
    }

    /**
     * create a photo with just a file path and a list of tags
     *
     * @param filepath the file path of the photo
     * @param tags     the tags of the photo
     */
    public Photo(String filepath, List<Map<String, String>> tags) {
        this(filepath, "", tags);
    }

    /**
     * delete a tag from the photo based on the key
     *
     * @param key   the key of the tag to be deleted
     * @param value the value of the tag to be deleted
     */
    public void deleteTag(String key, String value) {
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).containsKey(key) && tags.get(i).containsValue(value)) {
                tags.remove(i);
                break;
            }
        }
    }

    /**
     * get the file path of the photo
     *
     * @return the file path of the photo
     */
    public String getFilePath() {
        return filepath;
    }


    /**
     * get the tags of the photo
     *
     * @return the tags of the photo
     */
    public List<Map<String, String>> getTags() {
        return tags;
    }

    /**
     * add a tag to the photo
     *
     * @param key   the key of the tag
     * @param value the value of the tag
     * @throws NullPointerException     if the key is null
     * @throws IllegalArgumentException if the key is empty
     * @throws NullPointerException     if the value is null
     * @throws IllegalArgumentException if the value is empty
     */
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

        Map<String, String> tag = Map.of(key, value);
        tags.add(tag);
    }

    /**
     * toString method for the photo
     *
     * @return a string representation of the photo
     */
    public String toString() {
        return String.format("Photo: %s || Tags: %s", filepath, tags.toString());
    }
}