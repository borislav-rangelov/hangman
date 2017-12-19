package com.borislavrangelov.hangman.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.borislavrangelov.hangman.R;
import com.borislavrangelov.hangman.game.Word;
import com.borislavrangelov.hangman.storage.WordRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DictionaryActivity extends AppCompatActivity {

    public static final int SIZE = 20;

    private WordRepository repository;
    private List<Word> words = new ArrayList<>(SIZE);
    private int count = 0;
    private int wordCount = 0;
    private int lastItem;

    @Override
    protected void onDestroy() {
        if (repository != null) {
            repository.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        ListView wordsListView = (ListView) findViewById(R.id.wordsListView);
        ImageButton addImageButton = (ImageButton) findViewById(R.id.addImageButton);

        repository = new WordRepository(this);
        count = (int) repository.count(null, null);
        maybeGetNextPage();
        final ArrayAdapter<Word> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, words);
        wordsListView.setAdapter(adapter);
        wordsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount - 1 == lastItem) {
                    if (maybeGetNextPage()) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        wordsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (id >= words.size()) {
                    return false;
                }
                final Word word = words.get((int) id);

                new AlertDialog.Builder(DictionaryActivity.this)
                        .setTitle("Delete Word")
                        .setMessage("Are you sure you want to delete '" + word.getWord() + "'?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int removed = repository.remove(word.getId());
                                words.remove(word);
                                wordCount = words.size();
                                count -= removed;
                                refreshLastItemAndPage();
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
                return false;
            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DictionaryActivity.this)
                        .setTitle("Add Word");

                final EditText editText = new EditText(DictionaryActivity.this);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                builder.setView(editText);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String word = editText.getText().toString().toUpperCase().trim();
                        if (word.length() == 0) {
                            return;
                        }
                        if (word.length() > 200) {
                            Toast.makeText(DictionaryActivity.this, "Word is too long...", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (!Pattern.compile("^[A-Z\\- ']+$").matcher(word).matches()) {
                            Toast.makeText(DictionaryActivity.this, "Word '" + word + "' should contain only A-Z, '-' and ' '.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Set<Character> set = new HashSet<>();

                        for (char c : word.toCharArray()) {
                            if (c >= 'A' && c <= 'Z')
                                set.add(c);
                        }
                        if (set.size() < 3) {
                            Toast.makeText(DictionaryActivity.this, "Words should contain at least 3 unique letters.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        try {
                            repository.insert(word, "en");
                        } catch (Exception e) {
                            Toast.makeText(DictionaryActivity.this, "Word '" + word + "' already exists...", Toast.LENGTH_LONG).show();
                            return;
                        }
                        count++;
                        int size = words.size();
                        words.clear();
                        words.addAll(repository.getPage(null, null, 0, size));
                        wordCount = words.size();
                        refreshLastItemAndPage();
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
            }
        });
    }

    private boolean maybeGetNextPage() {
        if (words.size() == count) return false;
        words.addAll(repository.getPage(null, null, wordCount / SIZE, SIZE));
        wordCount = words.size();
        refreshLastItemAndPage();
        return true;
    }

    private void refreshLastItemAndPage() {
        lastItem = wordCount == 0 ? -1 : wordCount - 1;
    }
}
