package com.netsensia.rivalchess.recorder.model

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.Lob

@Entity
class Result(
        val engine1: String,
        val engine2: String,
        val nodes1: Int,
        val nodes2: Int,
        val millis1: Int,
        val millis2: Int,
        @Lob val pgn: String
) : AbstractJpaPersistable<Long>()

interface ResultRepository : JpaRepository<Result, Long> {
}