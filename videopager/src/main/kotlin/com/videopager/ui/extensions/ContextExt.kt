package com.videopager.ui.extensions

import android.content.Context
import android.view.LayoutInflater

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)