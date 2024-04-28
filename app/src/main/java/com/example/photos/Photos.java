package com.example.photos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.movies.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Photos extends AppCompatActivity implements Serializable {

    public static final String storeDir = "data";
    public static final String storeFile = "data.dat";
    private static final long serialVersionUID = 1L;
    private transient ListView listView;
    public static List<Album> albums;
    private transient ActivityResultLauncher<Intent> startForAlbumOpen;
    private transient ActivityResultLauncher<Intent> cancelForAlbumOpen;

    public List<String> getAlbumNames() {
        List<String> albumNames = new ArrayList<>();
        for (Album album : Photos.albums) {
            albumNames.add(album.getAlbumName());
        }
        return albumNames;
    }

    private void saveAlbumsToFile() {
        File file = new File(getFilesDir(), "albums.json");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            JSONArray jsonArray = new JSONArray();
            for (Album album : Photos.albums) {
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

    private List<Album> loadAlbums() {
        Photos.albums = new ArrayList<>(); // otherwise, when creating a new album,
        // it will raise an error because it will try to create an album with the same name
        List<Album> albumsTemp = new ArrayList<>();
        File file = new File(getFilesDir(), "albums.json");
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Album album = new Album(jsonObject.getString("albumName"));
                JSONArray photosJsonArray = jsonObject.getJSONArray("photos");
                for (int j = 0; j < photosJsonArray.length(); j++) {
                    JSONObject photoJsonObject = photosJsonArray.getJSONObject(j);
                    album.addPhoto(new Photo(photoJsonObject.getString("name")));
                }
                albumsTemp.add(album);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return albumsTemp;
    }

    // method to request permission for image` access using READ_MEDIA_IMAGE
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    1);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums_list);


        Toolbar myToolbar = findViewById(R.id.albums_toolbar);
        setSupportActionBar(myToolbar);

        // request permission for photos access
        requestPermission();

        FloatingActionButton createAlbumButton = findViewById(R.id.create_album_button);
        createAlbumButton.setOnClickListener(view -> createAlbum());

        // temp: delete albums.json for debugging
        File file = new File(getFilesDir(), "albums.json");
        file.delete();

        albums = loadAlbums();
        saveAlbumsToFile();
        // albums.json will store the list of albums and their photos

        listView = findViewById(R.id.albums_list);
        listView.setAdapter(
                new ArrayAdapter<>(this, R.layout.album, getAlbumNames()));

        listView.setOnItemClickListener((list, view, pos, id) -> showAlbum(pos));

        // register add/edit activities in onCreate
        // registration must be done before creation is completed
        registerActivities();
    }

    // register the activities
    public void registerActivities() {
        startForAlbumOpen = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::applyAlbumEdit);
        cancelForAlbumOpen = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::applyAlbumEdit);
    }

    private void applyAlbumEdit(ActivityResult result) {
        System.out.println("In applyAlbumEdit. Result code is: " + result.getResultCode() + ". and Albums is: " + Photos.albums);
        if (result.getResultCode() == Activity.RESULT_OK) {
            // get the album index and album name
            Intent data = result.getData();
            // parse OpenAlbum.ALBUM_INDEX as a string
            int albumIndex = data.getIntExtra(OpenAlbum.ALBUM_INDEX, -1);
            String albumName = data.getStringExtra(OpenAlbum.ALBUM_NAME);
            System.out.println("In applyAlbumEdit, albumIndex is: " + albumIndex);
            System.out.println("In applyAlbumEdit, albumName is: " + albumName);

            // update the album name
            Photos.albums.get(albumIndex).setAlbumName(albumName);
            listView.setAdapter(new ArrayAdapter<>(Photos.this, R.layout.album, getAlbumNames()));
            saveAlbumsToFile();

            // print all images in the album
            for (Photo photo : Photos.albums.get(albumIndex).getPhotos()) {
                System.out.println(photo.getFilePath());
            }

        } else if (result.getResultCode() == Activity.RESULT_CANCELED){
            // delete the album
            Intent data = result.getData();
            int albumIndex = data.getIntExtra(OpenAlbum.ALBUM_INDEX, -1);
            Photos.albums.remove(albumIndex);
            listView.setAdapter(new ArrayAdapter<>(Photos.this, R.layout.album, getAlbumNames()));
            saveAlbumsToFile();
        }
    }

    private void showAlbum(int pos) {
        // create an intent to open the album
        Intent intent = new Intent(this, OpenAlbum.class);
        intent.putExtra("albumIndex", pos);
        intent.putExtra("albumName", Photos.albums.get(pos).getAlbumName());
        startForAlbumOpen.launch(intent);
    }

    private void createAlbum() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Album");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            String albumName = input.getText().toString();

            // check if album name is empty or already exists
            if (albumName.isEmpty()) {
                Toast.makeText(Photos.this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Album album : Photos.albums) {
                if (album.getAlbumName().equals(albumName)) {
                    Toast.makeText(Photos.this, "Album already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Photos.albums.add(new Album(albumName));
            listView.setAdapter(new ArrayAdapter<>(Photos.this, R.layout.album, getAlbumNames()));
            saveAlbumsToFile();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            createAlbum();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // on pause save the albums to the file
    @Override
    protected void onPause() {
        super.onPause();
        saveAlbumsToFile();
    }

    // on resume load the albums from the file
    @Override
    protected void onResume() {
        super.onResume();
        albums = loadAlbums();
        listView.setAdapter(new ArrayAdapter<>(Photos.this, R.layout.album, getAlbumNames()));
    }

    // on destroy save the albums to the file
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveAlbumsToFile();
    }

    // on restart load the albums from the file
    @Override
    protected void onRestart() {
        super.onRestart();
        albums = loadAlbums();
        listView.setAdapter(new ArrayAdapter<>(Photos.this, R.layout.album, getAlbumNames()));
    }

    // on stop save the albums to the file
    @Override
    protected void onStop() {
        super.onStop();
        saveAlbumsToFile();
    }

}