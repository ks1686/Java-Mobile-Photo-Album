package com.example.photos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class OpenAlbum extends AppCompatActivity {

    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private Context context;
        private List<Photo> photos;

        public ImageAdapter(Context context, List<Photo> photos) {
            this.context = context;
            this.photos = photos;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            Photo photo = photos.get(position);
            holder.imageView.setImageURI(Uri.parse(photo.getFilePath()));
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        public void updatePhotos(List<Photo> newPhotos) {
            photos = newPhotos;
            notifyDataSetChanged();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
            }
        }
    }

    public static final String ALBUM_NAME = "albumName";
    public static final String ALBUM_INDEX = "albumIndex";
    private int albumIndex;

    private EditText albumName;
    private Button deleteAlbumButton;
    private Toolbar myToolbar;
    private Button addPhotoButton;
    private RecyclerView imageListView;
    private ImageAdapter imageAdapter;

    private Button renameAlbumButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_album);
        myToolbar = findViewById(R.id.my_toolbar);
        deleteAlbumButton = findViewById(R.id.delete_album_button);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addPhotoButton = findViewById(R.id.add_photo_button);
        addPhotoButton.setOnClickListener(view -> selectImage());

        renameAlbumButton = findViewById(R.id.rename_album_button);
        renameAlbumButton.setOnClickListener(view -> renameAlbum());


        imageListView = findViewById(R.id.image_list_view);
        int numberOfColumns = 3;
        imageListView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        Album album = Photos.albums.get(albumIndex);
        imageAdapter = new ImageAdapter(this, album.getPhotos());
        imageListView.setAdapter(imageAdapter);

        myToolbar.setNavigationOnClickListener(view -> returnToAlbumsList());

        albumName = findViewById(R.id.album_name);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            albumIndex = extras.getInt("albumIndex");
            albumName.setText(extras.getString("albumName"));
        }

        albumName.setText(extras.getString(ALBUM_NAME));

        deleteAlbumButton.setOnClickListener(view -> deleteAlbum());

        List<Album> albums = Photos.albums;
    }

    static final int REQUEST_IMAGE_GET = 1;

    public void onImageChosen(ActivityResult result) {
        System.out.println("In onImageChosen");
    }


    public void returnToAlbumsList(){
        String album_name_string = albumName.getText().toString();

        Bundle bundle = new Bundle();
        bundle.putString(ALBUM_NAME, album_name_string);
        bundle.putInt(ALBUM_INDEX, albumIndex);

        Intent intent = new Intent();
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

    // method to rename the album
    public void renameAlbum() {
        String albumNameString = albumName.getText().toString();
        Photos.albums.get(albumIndex).setAlbumName(albumNameString);
        saveAlbumsToFile();

        //toast to show that the album has been renamed
        Toast.makeText(this, "Album renamed", Toast.LENGTH_SHORT).show();
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra("albumIndex", albumIndex);
        intent.putExtra("albumName", albumName.getText().toString());
        startActivityForResult(intent, REQUEST_IMAGE_GET);
    }
    private void saveAlbumsToFile() {
        JSONArray albums = new JSONArray();
        for (Album album : Photos.albums) {
            JSONObject albumJson = new JSONObject();
            try {
                albumJson.put("albumName", album.getAlbumName());
                JSONArray photos = new JSONArray();
                for (Photo photo : album.getPhotos()) {
                    photos.put(photo.getFilePath());
                }
                albumJson.put("photos", photos);
                albums.put(albumJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String albumsString = albums.toString();
        File file = new File(getFilesDir(), "albums.json");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(albumsString.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method to save the updated album with the new photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            // grant uri permissions
            getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Uri uri = data.getData();
            // get the string filepath for the single photo
            String filePath = uri.toString();
            // if filepath not null, add to the album
            if (filePath != null) {
                Photos.albums.get(albumIndex).addPhoto(new Photo(filePath));
                saveAlbumsToFile();
                imageAdapter.updatePhotos(Photos.albums.get(albumIndex).getPhotos());
            } else {
                Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show();
            }

            // print all albums and their photos
            for (Album album : Photos.albums) {
                System.out.println(album.getAlbumName());
                for (Photo photo : album.getPhotos()) {
                    System.out.println(photo.getFilePath());
                }
            }
        }
    }
}