package com.borislavrangelov.hangman.game;

import android.util.JsonWriter;
import android.util.Log;

import com.borislavrangelov.hangman.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game implements Serializable {
    public static final int MAX_TURNS = 7;
    public static final char HIDDEN_CHAR = '_';
    private static final String TAG = Game.class.getSimpleName();

    private final Object lock = new Object();
    private char[] answer;
    private char[] characters;
    private List<Character> chosen;
    private List<Character> wrong;
    private State state;

    public Game() {
    }

    public static Game deserialize(String string) {
        Game game = new Game();
        for (String property : string.split(";")) {
            String[] values = property.split("=");
            String left = values[0];
            String right = values.length > 1 ? values[1] : "";
            switch (left) {
                case "answer":
                    game.answer = right.toCharArray();
                    break;
                case "characters":
                    game.characters = right.toCharArray();
                    break;
                case "chosen":
                    game.chosen = StringUtils.toCharList(right);
                    break;
                case "wrong":
                    game.wrong = StringUtils.toCharList(right);
                    break;
                case "state":
                    game.state = State.valueOf(right);
                    break;
                default:
                    Log.w(TAG, "deserialize: Unknown property: " + left);
                    break;
            }
        }
        return game;
    }

    public State init(String word) {
        synchronized (lock) {
            this.answer = word.toCharArray();
            this.chosen = new ArrayList<>();
            this.wrong = new ArrayList<>(MAX_TURNS);

            char first = answer[0];
            char last = answer[answer.length - 1];
            characters = new char[answer.length];
            Arrays.fill(characters, HIDDEN_CHAR);

            this.state = State.PLAYING;

            choose(first);
            return choose(last);
        }
    }

    public String serialize() {
        synchronized (lock) {
            return "answer=" + String.valueOf(answer) +
                    ";characters=" + String.valueOf(characters) +
                    ";chosen=" + StringUtils.fromCharList(chosen) +
                    ";wrong=" + StringUtils.fromCharList(wrong) +
                    ";state=" + state.toString();
        }
    }

    private void writeNullableString(JsonWriter writer, char[] value) throws IOException {
        if (answer == null) {
            writer.nullValue();
        } else {
            writer.value(new String(value));
        }
    }

    public State choose(char character) {
        synchronized (lock) {
            return chooseInternal(character);
        }
    }

    private State chooseInternal(char character) {
        // if not playing or character is chosen
        if (state != State.PLAYING || chosen.contains(character)) {
            return state;
        }

        chosen.add(character);

        // find & display matching characters
        boolean isWrong = true;

        for (int i = 0; i < characters.length; i++) {
            if (answer[i] == character) {
                characters[i] = character;
                isWrong = false;
            }
        }

        // if character was not found
        if (isWrong) {
            wrong.add(character);
            if (wrong.size() == MAX_TURNS) {
                characters = Arrays.copyOf(answer, answer.length);
                return this.state = State.LOST;
            }
        }

        // decide if game is finished
        for (int i = 0; i < characters.length; i++) {
            if (characters[i] == HIDDEN_CHAR) {
                return State.PLAYING;
            }
        }

        return this.state = State.WON;
    }

    public State getState() {
        synchronized (lock) {
            return state;
        }
    }

    public char[] getAnswer() {
        synchronized (lock) {
            return answer;
        }
    }

    public char[] getCharacters() {
        synchronized (lock) {
            return characters;
        }
    }

    public List<Character> getChosen() {
        synchronized (lock) {
            return chosen;
        }
    }

    public List<Character> getWrong() {
        synchronized (lock) {
            return wrong;
        }
    }

    public int getTriesLeft() {
        synchronized (lock) {
            return MAX_TURNS - wrong.size();
        }
    }

    public enum State {
        PLAYING, WON, LOST
    }
}
