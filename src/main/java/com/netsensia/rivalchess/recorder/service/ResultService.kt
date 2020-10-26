package com.netsensia.rivalchess.recorder.service

import com.netsensia.rivalchess.recorder.entity.Result
import com.netsensia.rivalchess.recorder.entity.ResultRepository
import org.springframework.stereotype.Component

@Component
class ResultService(private val repository: ResultRepository) {
    fun save(result: Result) {
        repository.save(result)
    }

    fun getOldVersions(version: Int) = repository.findAllOldVersions(version)

    fun getMatchStats() = repository.getMatchStats()

}
