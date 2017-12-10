package com.borislavrangelov.hangman.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.borislavrangelov.hangman.R;
import com.borislavrangelov.hangman.game.Game;
import com.borislavrangelov.hangman.game.Word;
import com.borislavrangelov.hangman.storage.WordRepository;
import com.borislavrangelov.hangman.util.AbstractTextWatcher;
import com.borislavrangelov.hangman.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private static final String GAME_STATE_FILE = "Hangman.current.db";
    private static final String TAG = Game.class.getSimpleName();
    private Game game;
    private TextView charactersTextView;
    private TextView triesTextView;
    private TextView chosenTextView;
    private EditText chooseEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        charactersTextView = (TextView) findViewById(R.id.charactersTextView);
        triesTextView = (TextView) findViewById(R.id.triesTextView);
        chosenTextView = (TextView) findViewById(R.id.chosenTextView);
        chooseEditText = (EditText) findViewById(R.id.chooseEditText);

        Button newGameButton = (Button) findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initGame();
                refreshDisplay();
            }
        });

        chooseEditText.addTextChangedListener(new AbstractTextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!StringUtils.isEmpty(s)) {
                    choose(s);
                }
            }
        });
    }

    private void choose(CharSequence s) {
        Game.State newState = game.choose(Character.toUpperCase(s.charAt(0)));
        if (newState != Game.State.PLAYING) {
            String message;
            if (newState == Game.State.WON) {
                message = "You guessed '" + new String(game.getAnswer()) + "'!";
            } else {
                message = "Sorry, you couldn't guess the word. :( Try with a new one.";
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        refreshDisplay();
        chooseEditText.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();

        tryRestoreGame();
        if (game == null) {
            initGame();
        }

        refreshDisplay();
    }

    @Override
    protected void onStop() {
        storeGameState();
        super.onStop();
    }

    private void tryRestoreGame() {
        this.game = new WordRepository(this).retrieveGameState();
    }

    private void storeGameState() {
        if (game != null) {
            new WordRepository(this).storeGameState(game);
        }
    }

    private void initGame() {
        game = new Game();
        WordRepository repository = new WordRepository(this);
        // TODO get from settings
        Word random = repository.getRandom(3, 10, "en");
        if (random == null) {
            Toast.makeText(this,
                    "No word found. Try adding some words to the Dictionary.",
                    Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }

        game.init(random.getWord());
    }

    @SuppressLint("DefaultLocale")
    private void refreshDisplay() {
        char[] characters = game.getCharacters();
        charactersTextView.setText(StringUtils.join(" ", characters));
        triesTextView.setText(String.format("Tries: %d", game.getTriesLeft()));
        List<Character> chosen = game.getChosen();
        chosenTextView.setText(String.format("Chosen: %s",
                chosen.isEmpty() ? "None" : StringUtils.join(", ", chosen)));
    }
}
