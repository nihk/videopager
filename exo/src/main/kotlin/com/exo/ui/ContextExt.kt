package com.exo.ui

import android.content.Context
import android.view.LayoutInflater

internal val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)
