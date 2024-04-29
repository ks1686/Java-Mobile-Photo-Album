package com.example.photos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import java.util.Map;

import android.text.TextWatcher;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;


public class Photos extends AppCompatActivity implements Serializable {

    public static final String storeDir = "data";
    public static final String storeFile = "data.dat";
    private static final long serialVersionUID = 1L;
    private transient ListView listView;
    public static List<Album> albums;
    private transient ActivityResultLauncher<Intent> startForAlbumOpen;
    private transient ActivityResultLauncher<Intent> cancelForAlbumOpen;
    private EditText searchBar;

    public List<String> getAlbumNames() {
        List<String> albumNames = new ArrayList<>();
        for (Album album : Photos.albums) {
            albumNames.add(album.getAlbumName());
        }
        return albumNames;
    }

    public static void saveAlbumsToFile(Context context) {
        System.out.println("In saveAlbumsToFile. Albums is: " + Photos.albums);
        File file = new File(context.getFilesDir(), "albums.json");
        System.out.println("Saving to the following path: " + context.getFilesDir() + "/albums.json");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            JSONArray jsonArray = new JSONArray();
            for (Album album : Photos.albums) {
                if (album.isTempAlbum) {
                    continue; // don't save any temporary albums
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("albumName", album.getAlbumName());
                JSONArray photosJsonArray = new JSONArray();
                for (Photo photo : album.getPhotos()) {
                    JSONObject photoJsonObject = new JSONObject();
                    photoJsonObject.put("name", photo.getFilePath());

                    // Add tags to the photoJsonObject
                    JSONArray tagsJsonArray = new JSONArray();
                    List<Map<String, String>> tags = photo.getTags();
                    for (Map<String, String> tag : tags) {
                        JSONObject tagJsonObject = new JSONObject();
                        for (Map.Entry<String, String> entry : tag.entrySet()) {
                            tagJsonObject.put("key", entry.getKey());
                            tagJsonObject.put("value", entry.getValue());
                        }
                        tagsJsonArray.put(tagJsonObject);
                    }
                    photoJsonObject.put("tags", tagsJsonArray);
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

        // now print out the contents of /data/user/0/com.example.photos/files/albums.json
        String path = context.getFilesDir() + "/albums.json";
        try {
            FileInputStream fis = new FileInputStream(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder stringBuilder = new StringBuilder();
            String line2;
            while ((line2 = bufferedReader.readLine()) != null) {
                stringBuilder.append(line2);
            }
            System.out.println("Contents of albums.json: " + stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private List<Album> loadAlbums() {
        Photos.albums = new ArrayList<>();
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
                    String photoFilePath = photoJsonObject.getString("name");
                    Photo photo = new Photo(photoFilePath);

                    // Load the tags for photos
                    if (photoJsonObject.has("tags")) {
                        JSONArray tagsJsonArray = photoJsonObject.getJSONArray("tags");
                        for (int k = 0; k < tagsJsonArray.length(); k++) {
                            JSONObject tagJsonObject = tagsJsonArray.getJSONObject(k);
                            String key = tagJsonObject.getString("key");
                            String value = tagJsonObject.getString("value");
                            photo.addTag(key, value);
                        }
                    }
                    album.addPhoto(photo);
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

        // make it go to main activity when back button is pressed
        myToolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        // request permission for photos access
        requestPermission();

        FloatingActionButton createAlbumButton = findViewById(R.id.create_album_button);
        createAlbumButton.setOnClickListener(view -> createAlbum());

        searchBar = findViewById(R.id.search_bar);
        // set the hint for the search bar
        searchBar.setHint("Search (enter tag values to search for photos)");
        searchBar.setText("");
        // when the user types a query and presses enter, print out the query

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event != null &&
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        // the user is done typing.
                        String query = searchBar.getText().toString();
                        if (query.isEmpty()) {
                            return true;
                        }
                        System.out.println("Query is: " + query);
                        try {
                            Album searchResults = searchPhotos(query);
                            System.out.println("Search results: " + searchResults);

                            // if search results aren't empty, open the album
                            if (searchResults.getSize() > 0) {
                                // add to Photos.albums, set album as temp
                                Photos.albums.add(searchResults);
                                searchResults.isTempAlbum = true;
                                Intent intent = new Intent(Photos.this, OpenAlbum.class);
                                intent.putExtra("albumIndex", -1);
                                intent.putExtra("albumName", "Search Results");
                                intent.putExtra("searchResults", searchResults);
                                startForAlbumOpen.launch(intent);
                            } else {
                                // make a toast, no results found
                                Toast.makeText(Photos.this, "No results found (add instructions for legal query later)", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IllegalArgumentException e) {
                            // make a toast, not a valid query
                            Toast.makeText(Photos.this, "Not a valid query (add instructions for legal query later)", Toast.LENGTH_SHORT).show();
                        }
                        return true; // consume.
                    }
                }
                return false; // pass on to other listeners.
            }
        });

        // temp: delete albums.json for debugging. CAREFUL: this is called everytime onCreate() is called
        // File file = new File(getFilesDir(), "albums.json");
        // file.delete();

        // if the size of the albums is 0, then load the albums from the file
        if (Photos.albums == null || Photos.albums.size() == 0) {
            Photos.albums = loadAlbums();
            saveAlbumsToFile(this);
        }
        // albums.json will store the list of albums and their photos
        // remove any temp albums from the list of albums
        for (Album album : Photos.albums) {
            if (album.isTempAlbum) {
                Photos.albums.remove(album);
            }
        }

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
    public Album searchPhotos(String tag) {
        Album retAlbum = new Album("Search Results");

        // go through every album, and do search() on every album to get a list of photo that matches.
        // then, add the photos to retAlbum
        for (Album album : Photos.albums) {
            List<Photo> photos = album.search(tag);
            for (Photo photo : photos) {
                retAlbum.addPhoto(photo);
            }
        }
        return retAlbum;
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

            // print all images in the album
            for (Photo photo : Photos.albums.get(albumIndex).getPhotos()) {
                System.out.println(photo.getFilePath());
            }

        } else if (result.getResultCode() == 2){
            // delete the album
            int albumIndex = result.getData().getIntExtra(OpenAlbum.ALBUM_INDEX, -1);
            Photos.albums.remove(albumIndex);
            listView.setAdapter(new ArrayAdapter<>(Photos.this, R.layout.album, getAlbumNames()));



        }
        saveAlbumsToFile(this);
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
            saveAlbumsToFile(this);
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
        saveAlbumsToFile(this);
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
        saveAlbumsToFile(this);
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
        saveAlbumsToFile(this);
    }

    // override onBackPress
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // kill the app, not just go back to the previous activity
        finishAffinity();
    }

}