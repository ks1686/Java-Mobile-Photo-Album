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
            this.photos = newPhotos;
            notifyDataSetChanged();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("Photo clicked");
                    }
                });
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
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }
    private void saveAlbumsToFile() {
        List<Album> albums = Photos.albums;
        File file = new File(getFilesDir(), "albums.json");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            JSONArray jsonArray = new JSONArray();
            for (Album album : albums) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("albumName", album.getAlbumName());
                JSONArray photosJsonArray = new JSONArray();
                for (Photo photo : album.getPhotos()) {
                    JSONObject photoJsonObject = new JSONObject();
                    photoJsonObject.put("name", photo.getFilePath());
                    photosJsonArray.put(photoJsonObject);
                }
                jsonObject.put("photos", photosJsonArray);
                jsonArray.put(jsonObject);
            }
            fos.write(jsonArray.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK && data != null) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            Uri fullPhotoUri = data.getData();
            // add this photo to albums
            Photo newPhoto = new Photo(fullPhotoUri.toString());
            Photos.albums.get(albumIndex).addPhoto(newPhoto);
            imageAdapter.updatePhotos(Photos.albums.get(albumIndex).getPhotos());
            saveAlbumsToFile();

            // print all albums
            for (Album album : Photos.albums) {
                System.out.println(album.toString());
            }
        }
    }
}