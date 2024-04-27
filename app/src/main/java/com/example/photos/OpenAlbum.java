package com.example.photos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies.R;

public class OpenAlbum extends AppCompatActivity {

    public static final String ALBUM_NAME = "albumName";
    public static final String ALBUM_INDEX = "albumIndex";
    private int albumIndex;

    private EditText albumName;
    private Button deleteAlbumButton;
    private Toolbar myToolbar;
    private Button addPhotoButton;
    private RecyclerView image_list_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("In onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_album);
        myToolbar = findViewById(R.id.my_toolbar);
        deleteAlbumButton = findViewById(R.id.delete_album_button);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addPhotoButton = findViewById(R.id.add_photo_button);
        addPhotoButton.setOnClickListener(view -> selectImage());

        RecyclerView image_list_view = findViewById(R.id.image_list_view);
        image_list_view.setAdapter(new ImageAdapter(this, new ArrayList<>()));


        myToolbar.setNavigationOnClickListener(view -> returnToAlbumsList());

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

        deleteAlbumButton.setOnClickListener(view -> deleteAlbum());

    }

    static final int REQUEST_IMAGE_GET = 1;


    public void returnToAlbumsList(){
        String album_name_string = albumName.getText().toString();

        Bundle bundle = new Bundle();
        bundle.putString(ALBUM_NAME, album_name_string);
        bundle.putInt(ALBUM_INDEX, albumIndex);


        Intent intent = new Intent();
        System.out.println("In returnToAlbumsList, albumIndex is: " + albumIndex);
        System.out.println("In returnToAlbumsList, ALBUM_INDEX is: " + ALBUM_INDEX);
        System.out.println("In returnToAlbumsList, album_name_string is: " + album_name_string);
        System.out.println("In returnToAlbumsList, ALBUM_NAME is: " + ALBUM_NAME);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void deleteAlbum() {
        Intent intent = new Intent();
        intent.putExtra(ALBUM_INDEX, albumIndex);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

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