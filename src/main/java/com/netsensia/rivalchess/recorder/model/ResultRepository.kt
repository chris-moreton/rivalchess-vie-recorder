package com.netsensia.rivalchess.recorder.model

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MatchUp {
    val engine1: String
    val engine2: String
    val result: String
    val cnt: Int
}

interface ResultRepository : JpaRepository<Result, Long> {
    @Query("SELECT r FROM Result r WHERE r.record_version < :version")
    fun findAllOldVersions(version: Int): Collection<Result?>?

    @Query("SELECT r.engine1 as engine1, r.engine2 as engine2, r.result as result, COUNT(*) as cnt FROM result r GROUP BY engine1, engine2, result ORDER BY engine1, engine2, result", nativeQuery = true)
    fun getMatchStats(): List<MatchUp>
}