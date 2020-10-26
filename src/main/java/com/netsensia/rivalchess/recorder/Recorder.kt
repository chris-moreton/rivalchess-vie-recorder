package com.netsensia.rivalchess.recorder

import com.google.gson.Gson
import com.netsensia.rivalchess.recorder.entity.Result
import com.netsensia.rivalchess.recorder.service.OutboundPayload
import com.netsensia.rivalchess.recorder.service.ResultService
import com.netsensia.rivalchess.utils.JmsReceiver
import com.netsensia.rivalchess.utils.pgnHeader
import com.netsensia.rivalchess.vie.model.MatchResult
import com.netsensia.rivalchess.vie.model.MatchUpStats
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component
import kotlin.streams.toList

@SpringBootApplication
@Component
class SpringBootConsoleApplication : CommandLineRunner {

    @Autowired
    private lateinit var resultService: ResultService
    private val recordVersion = 4
    private val statsApiEndpoint = System.getenv("STATS_API_ENDPOINT")

    private fun catchUp() {
        val results = resultService.getOldVersions(recordVersion)
        if (results != null) {
            println("Old version count: ${results.size}")
            results.parallelStream().forEach { result ->
                if (result != null) {
                    val pgn = result.pgn
                    result.ply_count = pgnHeader(pgn, "PlyCount").toInt()
                    result.result = pgnHeader(pgn, "Result")
                    result.opening = pgnHeader(pgn, "Opening")
                    result.opening_variation = pgnHeader(pgn, "Variation")
                    result.record_version = recordVersion
                    resultService.save(result)
                }
            }
        }
    }

    override fun run(vararg args: String) {
        do {
            val gson = Gson()
            val message = JmsReceiver.receive("MatchResulted")
            val matchResult = gson.fromJson(message, MatchResult::class.java)
            val entity = Result(
                    engine1 = matchResult.engineSettings.engine1.version,
                    engine2 = matchResult.engineSettings.engine2.version,
                    nodes1 = matchResult.engineSettings.engine1.maxNodes,
                    nodes2 = matchResult.engineSettings.engine2.maxNodes,
                    millis1 = matchResult.engineSettings.engine1.maxMillis,
                    millis2 = matchResult.engineSettings.engine2.maxMillis,
                    result = pgnHeader(matchResult.pgn, "Result"),
                    ply_count = pgnHeader(matchResult.pgn, "PlyCount").toInt(),
                    opening = pgnHeader(matchResult.pgn, "Opening"),
                    opening_variation = pgnHeader(matchResult.pgn, "Variation"),
                    record_version = recordVersion,
                    pgn = matchResult.pgn
            )
            resultService.save(entity)
            val matchUps = resultService.getMatchStats()

            val matchUpList = matchUps.stream().map {
                MatchUpStats(it.engine1, it.engine2, it.result, it.cnt)
            }.toList()

            val payload = gson.toJson(OutboundPayload(matchUpList))

            println("Sending payload ${payload}")

            khttp.post(
                    url = "http://${statsApiEndpoint}/matchUpStats",
                    data = payload,
                    headers = mapOf(Pair("Content-Type", "application/json"))

            )
            println("Running Catch Up")
            catchUp()
        } while (true)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(SpringBootConsoleApplication::class.java, *args)
        }
    }
}

