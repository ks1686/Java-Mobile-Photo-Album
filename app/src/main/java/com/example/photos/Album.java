package com.example.photos;

// Java imports

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an album of photos. An album has a name and a list of photos. An
 * album can be created with a name and a list of photos. An album can have
 * photos added to it, removed from it, and moved to another album. An album can
 * be searched for photos based on tags or dates. An album can have its name
 * changed. An album can have its start and end dates retrieved.
 *
 * @author jacobjude
 */
public class Album implements Serializable {
    private String albumName;
    private List<Photo> photos;
    public boolean isTempAlbum = false;

    /**
     * Creates an album with the given name and an empty list of photos.
     *
     * @param albumName the name of the album
     * @throws NullPointerException     if albumName is null
     * @throws IllegalArgumentException if albumName is empty
     */
    public Album(String albumName) throws NullPointerException, IllegalArgumentException {
        this(albumName, new ArrayList<>());
    }

    /**
     * Creates an album with the given name and list of photos.
     *
     * @param albumName the name of the album
     * @param photos    the list of photos in the album
     * @throws NullPointerException     if albumName or photos is null
     * @throws IllegalArgumentException if albumName is empty
     */
    public Album(String albumName, List<Photo> photos) throws NullPointerException, IllegalArgumentException {
        if (albumName == null) {
            throw new NullPointerException("albumName cannot be null");
        } else if (albumName.isEmpty()) {
            throw new IllegalArgumentException("albumName cannot be empty");
        }
        if (photos == null) {
            throw new NullPointerException("photos cannot be null");
        }
        this.albumName = albumName;
        setAlbumName(albumName); // throws error if album with same name already exists
        this.photos = photos;
        this.isTempAlbum = false; // by default, must set to true manually
    }

    /**
     * Adds a photo to the album.
     *
     * @param photo the photo to add
     */
    public void addPhoto(Photo photo) throws NullPointerException, IllegalArgumentException {
        // check if photo is null or photo is already in the album. throw error
        if (photo == null) {
            throw new NullPointerException("photo cannot be null");
        }

        if (this.photos.contains(photo)) {
            throw new IllegalArgumentException("Photo already exists in the album");
        }

        this.photos.add(photo); // may need to catch an exception here?
    }

    /**
     * Adds a photo to the album.
     *
     * @param filepath the filepath of the photo to add
     */
    public void addPhoto(String filepath) {
        this.photos.add(new Photo(filepath)); // may need to catch an exception here?
    }

    /**
     * Removes a photo from the album.
     *
     * @param photo the photo to remove
     */
    public void removePhoto(Photo photo) {
        this.photos.remove(photo);
    }

    /**
     * Gets a photo from the album.
     *
     * @return arrayList of photos
     */
    public List<Photo> getPhotos() {
        if (this.photos == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(this.photos);
    }

    /**
     * get the name of the album
     *
     * @return the name of the album
     */
    public String getAlbumName() {
        return albumName;
    }

    /**f
     * set the name of the album
     *
     * @param albumName the name of the album
     * @throws NullPointerException     if albumName is null
     * @throws IllegalArgumentException if albumName is empty
     */
    public void setAlbumName(String albumName) throws NullPointerException, IllegalArgumentException {
        if (albumName == null) {
            throw new NullPointerException("albumName cannot be null");
        } else if (albumName.isEmpty()) {
            throw new IllegalArgumentException("albumName cannot be empty");
        }

        // check if there is another album with the same name
        // potential issue: looks through static list of albums (but works)
        for (Album album : Photos.albums) {
            if (album.getAlbumName().equals(albumName) && !album.equals(this)) {
                throw new IllegalArgumentException("Album with the same name already exists");
            }
        }
        this.albumName = albumName;
    }

    /**
     * get the size of the album
     *
     * @return the size of the album
     */
    public int getSize() {
        return this.photos.size();
    }

    /**
     * toString method for the album
     *
     * @return the string representation of the album
     */
    public String toString() {
        // get the toString of all the photos in the album and album name
        String result = "";
        for (Photo photo : this.photos) {
            result += photo.toString() + "\n";
        }
        return "Album: " + this.albumName + "\nPhotos:\n" + result;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Album)) {
            return false;
        }

        Album album = (Album) o;

        return album.getAlbumName().equals(this.albumName) && album.getPhotos().equals(this.photos);
    }

    /**
     * search for photos in the album based on a query
     *
     * @param query the query to search for
     * @return the list of photos that match the query
     * @throws IllegalArgumentException if the query is invalid
     */
    public List<Photo> search(String query) {
        List<Photo> result = new ArrayList<>();


        // tags can look like "tagname=tagvalue" and can have conjunctions or
        // disjuctions
        // ex. person=John AND location=New York
        // ex. person=John OR location=New York
        // no need to handle more than 1 conjunction or disjunction

        // split the query by " AND " or " OR "
        String[] parts = query.strip().split(" AND | OR ");

        // if the query is a single tag
        if (parts.length == 1) {
            // check if its of the form "tagname=tagvalue"

            String[] tag = query.split("=");
            if (tag.length != 2) {
                throw new IllegalArgumentException("Invalid query");
            }

            if (!(query.strip().contains("=") || tag.length != 2 || tag[0].isEmpty() || tag[1].isEmpty())) {
                throw new IllegalArgumentException("Invalid query");
            }

            if (tag[0].contains(" ") || tag[1].contains(" ")) {
                throw new IllegalArgumentException("Invalid query");
            }
            for (Photo photo : this.photos) {
                for (Map<String, String> currentTag : photo.getTags()) {
                    if (currentTag.containsKey(tag[0]) && currentTag.get(tag[0]).startsWith(tag[1])) {
                        result.add(photo);
                    }
                }
            }
            return result;
        } else if (parts.length == 2) {
            // if the query is a disjunction
            if (query.contains(" OR ")) {
                String[] tag1 = parts[0].split("=");
                String[] tag2 = parts[1].split("=");
                if (tag1[0].contains(" ") || tag1[1].contains(" ")) {
                    throw new IllegalArgumentException("Invalid query");
                }
                if (tag2[0].contains(" ") || tag2[1].contains(" ")) {
                    throw new IllegalArgumentException("Invalid query");
                }
                for (Photo photo : this.photos) {
                    boolean found1 = false;
                    boolean found2 = false;
                    for (Map<String, String> tag : photo.getTags()) {
                        if (tag.containsKey(tag1[0]) && tag.get(tag1[0]).startsWith(tag1[1])) {
                            found1 = true;
                        }
                        if (tag.containsKey(tag2[0]) && tag.get(tag2[0]).startsWith(tag2[1])) {
                            found2 = true;
                        }
                    }
                    if (found1 || found2) {
                        result.add(photo);
                    }
                }
                return result;
            } else if (query.contains(" AND ")) {
                // if the query is a conjunction
                String[] tag1 = parts[0].split("=");
                String[] tag2 = parts[1].split("=");
                for (Photo photo : this.photos) {
                    boolean found1 = false;
                    boolean found2 = false;
                    for (Map<String, String> tag : photo.getTags()) {
                        if (tag.containsKey(tag1[0]) && tag.get(tag1[0]).startsWith(tag1[1])) {
                            found1 = true;
                        }
                        if (tag.containsKey(tag2[0]) && tag.get(tag2[0]).startsWith(tag2[1])) {
                            found2 = true;
                        }
                    }
                    if (found1 && found2) {
                        result.add(photo);
                    }
                }
                return result;
            }

        }

        throw new IllegalArgumentException("Invalid query");
    }

}