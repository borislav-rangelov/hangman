package com.borislavrangelov.hangman.game;

public class Word {
    private long id;
    private String word;
    private String lang;

    public Word() {
    }

    public Word(long id, String word, String lang) {
        this.id = id;
        this.word = word;
        this.lang = lang;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    @Override
    public String toString() {
        return word;
    }
}
