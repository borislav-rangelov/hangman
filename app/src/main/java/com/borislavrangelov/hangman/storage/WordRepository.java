package com.borislavrangelov.hangman.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.borislavrangelov.hangman.game.Game;
import com.borislavrangelov.hangman.game.Word;
import com.borislavrangelov.hangman.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class WordRepository {
    private static final String TAG = WordRepository.class.getSimpleName();
    private static final String GAME_STATE_FILE = "Hangman.current.db";
    private final WordDbHelper helper;
    private final Context context;
    private int defaultPageSize = 10;

    public WordRepository(Context context) {
        this.context = context;
        helper = new WordDbHelper(context);
    }

    public Game retrieveGameState() {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(GAME_STATE_FILE);
            StringBuilder fileContent = new StringBuilder();

            byte[] buffer = new byte[1024];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }

            return Game.deserialize(fileContent.toString());
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            Log.e(TAG, "tryRestoreGame: ", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "tryRestoreGame: ", e);
                }
            }
        }
        return null;
    }

    public void storeGameState(Game game) {
        String serialization = game.serialize();
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter writer = null;
        try {
            fileOutputStream = context.openFileOutput(GAME_STATE_FILE, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(fileOutputStream);
            writer.append(serialization);

        } catch (IOException e) {
            Log.e(TAG, "storeGameState: ", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "storeGameState: ", e);
            }
        }
    }

    public void dropAll() {
        helper.drop();
    }

    public Word insert(String word, String lang) {
        ContentValues values = new ContentValues();
        values.put(WordEntry.COLUMN_WORD, word.toUpperCase());
        values.put(WordEntry.COLUMN_SIZE, word.length());
        values.put(WordEntry.COLUMN_LANG, lang);

        SQLiteDatabase database = null;
        try {
            database = helper.getWritableDatabase();
            long id = database.insertOrThrow(WordEntry.TABLE_NAME, null, values);
            return new Word(id, word, lang);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        } finally {
            if (database != null)
                database.close();
        }
    }

    public Word getRandom(int minSize, int maxSize, String lang) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            if (minSize < 0) {
                minSize = 0;
            }
            if (maxSize < 0) {
                maxSize = 0;
            }
            if (minSize > maxSize) {
                minSize = maxSize;
            }
            String table = WordEntry.TABLE_NAME;
            String[] returnCols = {WordEntry._ID, WordEntry.COLUMN_WORD, WordEntry.COLUMN_LANG};
            String selection = minSize + maxSize > 0 ? String.format("%1$s=? AND %2$s>=? AND %2$s<=?",
                    WordEntry.COLUMN_LANG, WordEntry.COLUMN_SIZE) : null;
            String[] args = minSize + maxSize > 0 ?
                    new String[]{lang, String.valueOf(minSize), String.valueOf(maxSize)} : null;
            String orderBy = "RANDOM()";
            String limit = "1";

            database = helper.getReadableDatabase();
            cursor = database.query(table, returnCols, selection, args, null, null, orderBy, limit);
            if (cursor != null && cursor.moveToFirst()) {
                return getWord(cursor);
            }
            return null;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
            if (database != null)
                database.close();
        }
    }

    @NonNull
    private Word getWord(Cursor cursor) {
        return new Word(
                cursor.getLong(cursor.getColumnIndex(WordEntry._ID)),
                cursor.getString(cursor.getColumnIndex(WordEntry.COLUMN_WORD)),
                cursor.getString(cursor.getColumnIndex(WordEntry.COLUMN_LANG))
        );
    }

    public List<Word> getPage(String search, String lang, int page, int size) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            if (size <= 0) {
                size = defaultPageSize;
            }
            if (page < 0) {
                page = 0;
            }

            String table = WordEntry.TABLE_NAME;
            String[] returnCols = {WordEntry._ID, WordEntry.COLUMN_WORD, WordEntry.COLUMN_LANG};
            List<String> selection = new ArrayList<>(2);
            List<String> args = new ArrayList<>(2);

            if (StringUtils.hasText(lang)) {
                selection.add(WordEntry.COLUMN_LANG + " LIKE ?");
                args.add(lang);
            }

            if (StringUtils.hasText(search)) {
                selection.add(WordEntry.COLUMN_WORD + " LIKE %?%");
                args.add(search);
            }

            String orderBy = WordEntry.COLUMN_WORD + " ASC";
            String limit = (page * size) + "," + size;

            database = helper.getReadableDatabase();
            String selectionStr = selection.isEmpty() ? null : StringUtils.join(" AND ", selection);
            String[] argsArray = args.isEmpty() ? null : args.toArray(new String[args.size()]);

            cursor = database.query(table, returnCols, selectionStr, argsArray, null,
                    null, orderBy, limit);

            List<Word> list = new ArrayList<>(size);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    list.add(getWord(cursor));
                } while (cursor.moveToNext());
            }
            return list;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
            if (database != null)
                database.close();
        }
    }

    public long count(String search, String lang) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {

            List<String> selection = new ArrayList<>(2);
            List<String> args = new ArrayList<>(2);

            if (StringUtils.hasText(lang)) {
                selection.add(WordEntry.COLUMN_LANG + " LIKE ?");
                args.add(lang);
            }

            if (StringUtils.hasText(search)) {
                selection.add(WordEntry.COLUMN_WORD + " LIKE %?%");
                args.add(search);
            }

            String table = WordEntry.TABLE_NAME;
            String[] returnCols = new String[]{"COUNT(*)"};
            String selectionStr = selection.isEmpty() ? null : StringUtils.join(" AND ", selection);
            String[] argsArray = args.isEmpty() ? null : args.toArray(new String[args.size()]);

            database = helper.getReadableDatabase();
            cursor = database.query(table, returnCols, selectionStr, argsArray, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
            if (database != null)
                database.close();
        }
    }

    public int remove(long id) {
        SQLiteDatabase database = null;
        try {

            database = helper.getReadableDatabase();
            return database.delete(WordEntry.TABLE_NAME, WordEntry._ID + " = ?",
                    new String[]{String.valueOf(id)});

        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        } finally {
            if (database != null)
                database.close();
        }
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        if (defaultPageSize <= 0) {
            throw new IllegalArgumentException("Page size cannot be less than 1");
        }
        this.defaultPageSize = defaultPageSize;
    }

    public void close() {
        helper.close();
    }

    public void seed() {
        if (count(null, null) == 0) {
            Log.i(getClass().getSimpleName(), "Seeding 100 words...");
            String wordString = "believe,lock,abrasive,remarkable,growth,basket,craven,vest,clean,expand,fetch,halting," +
                    "daily,rhetorical,force,sticky,hushed,school,belong,petite,roomy,cute,skirt,lake," +
                    "insect,duck,tail,aquatic,purple,strong,innate,dock,surprise,fluttering,interest," +
                    "house,first,milky,obey,present,fantastic,receptive,manage,irate,nest,person," +
                    "cooperative,tent,quarrelsome,whistle,effect,statuesque,yoke,rain,stupendous,walk," +
                    "subsequent,cynical,secretive,rhyme,five,breathe,race,jeans,automatic,fire,one," +
                    "drunk,chase,tacky,matter,appear,belief,different,cat,dress,stop,depressed," +
                    "abortive,committee,passenger,murky,tiresome,ajar,train,tax,cold,animal,elite,sleep," +
                    "cuddly,annoy,probable,sick,pan,makeshift,breakable,trap,tick,respect";
            String[] words = wordString.split(",");
            for (String word : words) {
                insert(word, "en");
            }
        }
    }

    static final String SQL_CREATE_TABLE = String.format(
            "CREATE TABLE %s (" +
                    "`%s` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`%s` VARCHAR(255) NOT NULL, " +
                    "`%s` INTEGER NOT NULL, " +
                    "`%s` VARCHAR(255) NOT NULL)",
            WordEntry.TABLE_NAME,
            WordEntry._ID,
            WordEntry.COLUMN_WORD,
            WordEntry.COLUMN_SIZE,
            WordEntry.COLUMN_LANG
    );

    static final String[] SQL_TABLE_INDEXES = new String[]{
            String.format(
                    "CREATE UNIQUE INDEX IF NOT EXISTS %s_%s ON %s (%s)",
                    WordEntry.TABLE_NAME,
                    WordEntry.COLUMN_WORD,
                    WordEntry.TABLE_NAME,
                    WordEntry.COLUMN_WORD
            ),
            String.format(
                    "CREATE INDEX IF NOT EXISTS %s_%s ON %s (%s)",
                    WordEntry.TABLE_NAME,
                    WordEntry.COLUMN_SIZE,
                    WordEntry.TABLE_NAME,
                    WordEntry.COLUMN_SIZE
            ),
            String.format(
                    "CREATE INDEX IF NOT EXISTS %s_%s ON %s (%s)",
                    WordEntry.TABLE_NAME,
                    WordEntry.COLUMN_LANG,
                    WordEntry.TABLE_NAME,
                    WordEntry.COLUMN_LANG
            )
    };

    private static class WordEntry implements BaseColumns {
        static final String TABLE_NAME = "words";
        static final String COLUMN_WORD = "word";
        static final String COLUMN_SIZE = "size";
        static final String COLUMN_LANG = "lang";
    }
}
