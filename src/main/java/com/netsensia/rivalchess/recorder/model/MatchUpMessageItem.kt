package com.netsensia.rivalchess.recorder.model

data class MatchUpMessageItem (
    val engine1: String,
    val engine2: String,
    val result: String,
    val cnt: Int
)
