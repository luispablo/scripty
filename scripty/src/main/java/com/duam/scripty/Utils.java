package com.duam.scripty;

import android.widget.EditText;

/**
 * Created by luispablo on 19/06/14.
 */
public class Utils {

    public static boolean isEmpty(EditText edit) {
        return edit.getText().length() == 0;
    }
}
