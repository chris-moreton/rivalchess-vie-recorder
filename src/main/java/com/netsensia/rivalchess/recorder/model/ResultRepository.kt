package com.netsensia.rivalchess.recorder.model

import com.netsensia.rivalchess.recorder.model.stats.MatchUp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ResultRepository : JpaRepository<Result, Long> {
    @Query("SELECT r FROM Result r WHERE r.record_version < :version")
    fun findAllOldVersions(version: Int): Collection<Result?>?

    @Query( "select engine1, engine2, result, count(*) from result" +
            "order by engine1, engine2", nativeQuery=true)
    fun getMatchStats(): Collection<MatchUp?>?
}