package com.borislavrangelov.hangman.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringUtils {
    private StringUtils() {}

    public static boolean isEmpty(CharSequence value) {
        return value == null || value.length() == 0;
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String join(String glue, Collection<?> strings) {
        if (glue == null) {
            throw new IllegalArgumentException("glue is null");
        }
        if (strings == null) {
            throw new IllegalArgumentException("strings is null");
        }
        if (strings.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int last = strings.size() - 1;
        int i = 0;
        for (Object value : strings) {
            sb.append(value);
            if (i != last) {
                sb.append(glue);
                i++;
            }
        }
        return sb.toString();
    }

    public static String join(String glue, char[] chars) {
        if (glue == null) {
            throw new IllegalArgumentException("glue is null");
        }
        if (chars == null) {
            throw new IllegalArgumentException("chars is null");
        }
        if (chars.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        int last = chars.length - 1;
        int i = 0;
        for (char val : chars) {
            sb.append(val);
            if (i != last) {
                sb.append(glue);
                i++;
            }
        }
        return sb.toString();
    }

    public static String fromCharList(List<Character> characters) {
        if (characters == null) {
            throw new IllegalArgumentException("characters is null");
        }
        char[] result = new char[characters.size()];
        int i = 0;
        for (Character character : characters) {
            result[i++] = character;
        }
        return new String(result);
    }

    public static List<Character> toCharList(String value) {
        ArrayList<Character> list = new ArrayList<>();
        for (char c : value.toCharArray()) {
            list.add(c);
        }
        return list;
    }
}
