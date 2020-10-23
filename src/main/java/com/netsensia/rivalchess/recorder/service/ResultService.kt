package com.netsensia.rivalchess.recorder.service

import com.netsensia.rivalchess.recorder.model.Result
import com.netsensia.rivalchess.recorder.model.ResultRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class ResultService(private val repository: ResultRepository) {
    fun save(result: Result) {
        repository.save(result)
    }

    fun getOldVersions(version: Int) = repository.findAllOldVersions(version)

    fun getMatchStats() = repository.getMatchStats()

}
