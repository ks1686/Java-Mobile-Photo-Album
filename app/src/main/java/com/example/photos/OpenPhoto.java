package com.example.photos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import com.example.movies.R;

public class OpenPhoto extends AppCompatActivity {

    private Toolbar displayPhotoToolbar;
    private ImageView photoView;
    private Button prevPhotoButton;
    private Button nextPhotoButton;
    private Button editTagsButton;
    private Button moveButton;
    private NestedScrollView tagsScrollView;
    private ConstraintLayout tagsLinearLayout;
    private TextView tagsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_photo);

        // Initialize your UI components
        displayPhotoToolbar = findViewById(R.id.display_photo_toolbar);
        photoView = findViewById(R.id.photo_view);
        prevPhotoButton = findViewById(R.id.prev_photo_button);
        nextPhotoButton = findViewById(R.id.next_photo_button);
        editTagsButton = findViewById(R.id.edit_tags_button);
        moveButton = findViewById(R.id.move_button);
        tagsTextView = findViewById(R.id.tags_textView);

        // TODO: Setup listeners here
    }
}