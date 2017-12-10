package com.borislavrangelov.hangman.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WordDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Hangman.db";
    private static final int DB_VERSION = 1;
    private final Context context;

    public WordDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    public void drop() {
        this.context.deleteDatabase(DB_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WordRepository.SQL_CREATE_TABLE);
        for (String tableIndex : WordRepository.SQL_TABLE_INDEXES) {
            db.execSQL(tableIndex);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
}
