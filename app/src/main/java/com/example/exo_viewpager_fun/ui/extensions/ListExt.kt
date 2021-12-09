package com.example.exo_viewpager_fun.ui.extensions

infix fun <T> List<T>?.elementsReferentiallyEqual(other: List<T>?): Boolean {
    if (this == null || other == null || size != other.size) return false

    for (i in indices) {
        if (this[i] !== other[i]) return false
    }

    return true
}
