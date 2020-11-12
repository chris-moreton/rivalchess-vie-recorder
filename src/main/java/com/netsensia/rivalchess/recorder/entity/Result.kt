package com.netsensia.rivalchess.recorder.entity

import javax.persistence.*

@Entity
@Table(indexes = [Index(name="group_index", columnList="engine1,engine2,result")])
class Result(
        @Column(columnDefinition = "varchar(20)")
        var engine1: String,
        @Column(columnDefinition = "varchar(20)")
        var engine2: String,
        var nodes1: Int,
        var nodes2: Int,
        var millis1: Int,
        var millis2: Int,
        @Column(columnDefinition = "varchar(7)")
        var result: String,
        var ply_count: Int,
        var opening: String,
        var opening_variation: String,
        @Column(columnDefinition = "integer default 1") var record_version: Int,
        @Lob var pgn: String
) : AbstractJpaPersistable<Long>()

