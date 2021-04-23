package com.inkkal.example

import android.util.Patterns

/**
 * Created by Abderrahim El imame on 11/17/20.
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

fun String.isValidUrl(): Boolean {
    return Patterns.WEB_URL.matcher(this).matches()
}
