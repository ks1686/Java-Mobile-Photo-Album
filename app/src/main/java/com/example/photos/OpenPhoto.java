package com.example.photos;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import com.example.movies.R;

import java.util.ArrayList;
import java.util.List;

public class OpenPhoto extends AppCompatActivity {

    public static String PHOTO_FILEPATH = "photoFilepath";
    public static String ALBUM_INDEX = "albumIndex";
    private String photoFilepath = "";
    public int albumIndex;

    private Toolbar displayPhotoToolbar;
    private ImageView photoView;
    private Button prevPhotoButton;
    private Button nextPhotoButton;
    private Button editTagsButton;
    private Button moveButton;
    private Button deleteButton;
    private Button backButton;

    private NestedScrollView tagsScrollView;
    private ConstraintLayout tagsLinearLayout;
    private TextView tagsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_photo);

        // Initialize your UI components

        //! bug, after moving a photo, if you press the back button in the toolbar, it will not go back to the album view
        displayPhotoToolbar = findViewById(R.id.display_photo_toolbar);
        displayPhotoToolbar.setTitle("");

        photoView = findViewById(R.id.photo_view);
        prevPhotoButton = findViewById(R.id.prev_photo_button);
        nextPhotoButton = findViewById(R.id.next_photo_button);
        editTagsButton = findViewById(R.id.edit_tags_button);
        moveButton = findViewById(R.id.move_button);
        deleteButton = findViewById(R.id.delete_button);
        tagsTextView = findViewById(R.id.tags_textView);
        backButton = findViewById(R.id.back_button);

        // Set the toolbar as the action bar for the activity
        setSupportActionBar(displayPhotoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                this.photoFilepath = bundle.getString(PHOTO_FILEPATH);
                this.albumIndex = bundle.getInt(ALBUM_INDEX);
            }
        }

        // display the given photo in the photoView
        System.out.println("photoFilepath: " + photoFilepath);
        displayPhoto(photoFilepath);

        // set listeners for each of the buttons
        prevPhotoButton.setOnClickListener(view -> prevPhoto());
        nextPhotoButton.setOnClickListener(view -> nextPhoto());
        // editTagsButton.setOnClickListener(view -> editTags());
        moveButton.setOnClickListener(view -> movePhoto());
        deleteButton.setOnClickListener(view -> deletePhoto());
        backButton.setOnClickListener(view -> backToAlbum());


    }

    @Override
    public boolean onSupportNavigateUp() {
        // go back to main activity
        Intent intent = new Intent(this, Photos.class);
        startActivity(intent);
        return true;
    }

    public void nextPhoto() {
        // get the next photo from the album
        // display the next photo in the photoView
        System.out.println("nextPhoto");
        int nextPhotoIndex = albumIndex + 1;
        if (nextPhotoIndex >= Photos.albums.size()) {
            nextPhotoIndex = 0;
        }

        Album album = Photos.albums.get(nextPhotoIndex);
        Photo nextPhoto = album.getPhotos().get(0);
        displayPhoto(nextPhoto.getFilePath());
    }


    public void movePhoto() {
        // create a pop up dialog (AlertDialog) to select the album to move the photo to
        // move the photo to the selected album
        // go back to the album view
        System.out.println("movePhoto");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an album to move the photo to");

        // create a list of albums to select from
        String[] albumNames = new String[Photos.albums.size()];
        for (int i = 0; i < Photos.albums.size(); i++) {
            albumNames[i] = Photos.albums.get(i).getAlbumName();
        }

        builder.setItems(albumNames, (dialog, which) -> {
            // make sure the album selected is not the current album

            // move the photo to the selected album
            Album album = Photos.albums.get(which);

            Photo photoToAdd = new Photo(photoFilepath);
            try {
                album.addPhoto(photoToAdd);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Photo already exists in the album", Toast.LENGTH_SHORT).show();
                return;
            }

            // delete the photo from the current album
            Album currentAlbum = Photos.albums.get(albumIndex);
            System.out.println("(before) current album size: " + currentAlbum.getSize());
            for (int i = 0; i < currentAlbum.getSize(); i++) {
                if (currentAlbum.getPhotos().get(i).getFilePath().equals(photoFilepath)) {
                    // remove item at index i
                    currentAlbum.removePhoto(currentAlbum.getPhotos().get(i));
                }
            }
            // print current album size
            System.out.println("(afteR) current album size: " + currentAlbum.getSize());
            // finish();
            backToAlbum();
            // it works when you do backToAlbum, but not finish()
            // problem: back button only works properly when you do finish(). same with deletePhoto().
        });
        builder.create().show();

    }

    public void deletePhoto() {
        // delete the photo from the album
        // go back to the album view
        Album album = Photos.albums.get(albumIndex);
        // go through album photos and find the photo to delete
        for (int i = 0; i < album.getSize(); i++) {
            if (album.getPhotos().get(i).getFilePath().equals(photoFilepath)) {
                // get photo with index i
                Photo photo = album.getPhotos().get(i);
                album.removePhoto(photo);
                break;
            }
        }
        finish();
    }


    public void prevPhoto() {
        // get the previous photo from the album
        // display the previous photo in the photoView
        System.out.println("prevPhoto");
        int prevPhotoIndex = albumIndex - 1;
        if (prevPhotoIndex < 0) {
            prevPhotoIndex = Photos.albums.size() - 1;
        }

        Album album = Photos.albums.get(prevPhotoIndex);
        Photo prevPhoto = album.getPhotos().get(album.getSize() - 1);
        displayPhoto(prevPhoto.getFilePath());
    }

    public void backToAlbum() {
        // go back to the album view
        Intent intent = new Intent(this, OpenAlbum.class);
        Bundle bundle = new Bundle();
        bundle.putInt(OpenAlbum.ALBUM_INDEX, albumIndex);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();

    }

    public void displayPhoto(String photoFilepath) {
        photoView.setImageURI(Uri.parse(photoFilepath));
    }

}