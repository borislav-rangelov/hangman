package com.borislavrangelov.hangman.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

public class ActivityUtil {
    private ActivityUtil() {}

    public static View.OnClickListener natigateListener(final Context context, final Class<? extends Activity> activityType) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, activityType));
            }
        };
    }
}
