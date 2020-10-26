package com.netsensia.rivalchess.recorder.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob

@Entity
class Result(
        var engine1: String,
        var engine2: String,
        var nodes1: Int,
        var nodes2: Int,
        var millis1: Int,
        var millis2: Int,
        var result: String,
        var ply_count: Int,
        var opening: String,
        var opening_variation: String,
        @Column(columnDefinition = "integer default 1") var record_version: Int,
        @Lob var pgn: String
) : AbstractJpaPersistable<Long>()

