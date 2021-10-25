package com.ts.bindid.example.java.ui.main.token;

import android.content.Context;

/**
 * Created by Ran Stone on 21/06/2021.
 */
public class TokenItem {
    String name;
    String value;

    TokenItem(Context context, int id, String value){
        this.name = context.getString(id);
        this.value = value;
    }

}
