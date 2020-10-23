package com.netsensia.rivalchess.recorder.model.stats

data class MatchUp (
    val whiteEngine: String,
    val blackEngine: String,
    val result: Int,
    val resultCount: Int
)