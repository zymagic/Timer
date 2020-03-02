package com.zy.timer

fun safe(block: () -> Unit) {
    try {
        block.invoke()
    } catch (t: Throwable) {
        // ignore
    }
}