package com.example.photos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.movies.R;

public class OpenAlbum extends AppCompatActivity {

    public static final String ALBUM_NAME = "";
    public static final String ALBUM_INDEX = "";
    private int albumIndex;

    private EditText albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("In onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_album);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle(ALBUM_NAME + " Photos");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the fields
        albumName = findViewById(R.id.album_name);

        // see if info was passed in to populate the fields
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            albumIndex = extras.getInt("albumIndex");
            albumName.setText(extras.getString("albumName"));
        }

        // set albumName text to the album name
        albumName.setText(extras.getString(ALBUM_NAME));
        System.out.println("Album name: " + extras.getString(ALBUM_NAME));
        selectImage();
    }

    static final int REQUEST_IMAGE_GET = 1;

    // select an image from the gallery
    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    // get the result of the image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            Uri fullPhotoUri = data.getData();
        }
    }
}