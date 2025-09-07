package com.example.sudokugame.levelselection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokugame.GameActivity;
import com.example.sudokugame.R;
import com.google.android.material.button.MaterialButton;

public class LevelSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);

        MaterialButton buttonEasy = findViewById(R.id.buttonEasy);
        MaterialButton buttonMedium = findViewById(R.id.buttonMedium);
        MaterialButton buttonHard = findViewById(R.id.buttonHard);

        buttonEasy.setOnClickListener(v -> {
            Toast.makeText(this, "Easy Selected", Toast.LENGTH_SHORT).show();
            startGame("EASY");
        });

        buttonMedium.setOnClickListener(v -> {
            Toast.makeText(this, "Medium Selected", Toast.LENGTH_SHORT).show();
            startGame("MEDIUM");
        });

        buttonHard.setOnClickListener(v -> {
            Toast.makeText(this, "Hard Selected", Toast.LENGTH_SHORT).show();
            startGame("HARD");
        });
    }

    private void startGame(String difficulty) {
        Intent intent = new Intent(LevelSelectionActivity.this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
        startActivity(intent);
    }
}
