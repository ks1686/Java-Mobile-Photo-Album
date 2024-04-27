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

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
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
        }
    }
}