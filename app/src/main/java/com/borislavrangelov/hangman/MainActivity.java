package com.borislavrangelov.hangman;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.borislavrangelov.hangman.activity.DictionaryActivity;
import com.borislavrangelov.hangman.activity.GameActivity;
import com.borislavrangelov.hangman.storage.WordRepository;
import com.borislavrangelov.hangman.util.ActivityUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newGameButton = (Button) findViewById(R.id.newGameButton);
//        Button statsButton = (Button) findViewById(R.id.statsButton);
        Button dictionaryButton = (Button) findViewById(R.id.dictionaryButton);

        newGameButton.setOnClickListener(ActivityUtil.natigateListener(this, GameActivity.class));
//        statsButton.setOnClickListener(ActivityUtil.natigateListener(this, StatsActivity.class));
        dictionaryButton.setOnClickListener(ActivityUtil.natigateListener(this, DictionaryActivity.class));

        WordRepository repository = new WordRepository(getApplicationContext());
        repository.seed();
        repository.close();
    }
}
