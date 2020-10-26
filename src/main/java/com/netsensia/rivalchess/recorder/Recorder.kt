package com.netsensia.rivalchess.recorder

import com.google.gson.Gson
import com.netsensia.rivalchess.recorder.entity.Result
import com.netsensia.rivalchess.recorder.service.OutboundPayload
import com.netsensia.rivalchess.recorder.service.ResultService
import com.netsensia.rivalchess.utils.JmsReceiver
import com.netsensia.rivalchess.utils.pgnHeader
import com.netsensia.rivalchess.vie.model.EngineRanking
import com.netsensia.rivalchess.vie.model.MatchResult
import com.netsensia.rivalchess.vie.model.MatchUpStats
import khttp.post
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component
import kotlin.random.Random
import kotlin.streams.toList

@SpringBootApplication
@Component
class SpringBootConsoleApplication : CommandLineRunner {

    @Autowired
    private lateinit var resultService: ResultService
    private val recordVersion = 4
    private val statsApiEndpoint = System.getenv("STATS_API_ENDPOINT")

    override fun run(vararg args: String) {
        do {
            processQueueMessage()
            sendPayload()
            catchUp()
        } while (true)
    }

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

    private fun processQueueMessage() {
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
    }

    private fun eloCalc(elo1: Int, elo2: Int, result: String): Pair<Int, Int> {
        if (result.equals("1-0")) {
            return Pair(
                    calculate2PlayersRating(elo1, elo2, "+"),
                    calculate2PlayersRating(elo2, elo1, "-")
            )
        }
        if (result.equals("0-1")) {
            return Pair(
                    calculate2PlayersRating(elo1, elo2, "-"),
                    calculate2PlayersRating(elo2, elo1, "+")
            )
        }
        if (result.equals("1/2-1/2")) {
            return Pair(
                    calculate2PlayersRating(elo1, elo2, "="),
                    calculate2PlayersRating(elo2, elo1, "=")
            )
        }
        return Pair(elo1, elo2)
    }

    private fun getRankingsList(matchUpList: List<MatchUpStats>): List<EngineRanking> {
        val matchCount = matchUpList.sumBy { it.cnt }

        val eloMap = mutableMapOf<String, EngineRanking>()

        (0..100000).forEach {
            val r = Random.nextInt(matchCount)
            var pos = 0
            matchUpList.forEach {
                pos += it.cnt
                if (pos > r) {
                    eloMap.putIfAbsent(it.engine1, EngineRanking(it.engine1, 1200, 0))
                    eloMap.putIfAbsent(it.engine2, EngineRanking(it.engine2, 1200, 0))

                    val elo1 = eloMap.get(it.engine1)!!.elo
                    val elo2 = eloMap.get(it.engine2)!!.elo
                    val newElos = eloCalc(elo1, elo2, it.result)

                    eloMap.set(it.engine1, EngineRanking(
                            it.engine1,
                            newElos.first,
                            eloMap.get(it.engine1)!!.played + 1))

                    eloMap.set(it.engine2, EngineRanking(
                            it.engine2,
                            newElos.second,
                            eloMap.get(it.engine2)!!.played + 1))
                }
            }
        }

        return eloMap.values.stream()
                .sorted(Comparator.comparingInt(EngineRanking::elo).reversed())
                .toList()
    }

    private fun sendPayload() {
        val gson = Gson()

        val matchUps = resultService.getMatchStats()

        val matchUpList = matchUps.stream().map {
            MatchUpStats(it.engine1, it.engine2, it.result, it.cnt)
        }.sorted(Comparator.comparing(MatchUpStats::engine1).reversed())
         .toList()

        val payload = gson.toJson(OutboundPayload(matchUpList, getRankingsList(matchUpList)))

        println("Sending payload ${payload}")

        post(
                url = "http://${statsApiEndpoint}/matchUpStats",
                data = payload,
                headers = mapOf(Pair("Content-Type", "application/json"))

        )
    }

    fun calculate2PlayersRating(player1Rating: Int, player2Rating: Int, outcome: String): Int {
        val actualScore: Double

        // winner
        actualScore = if (outcome == "+") {
            1.0
            // draw
        } else if (outcome == "=") {
            0.5
            // lose
        } else if (outcome == "-") {
            0.0
            // invalid outcome
        } else {
            return player1Rating
        }

        // calculate expected outcome
        val exponent = (player2Rating - player1Rating).toDouble() / 400
        val expectedOutcome = 1 / (1 + Math.pow(10.0, exponent))

        // K-factor
        val K = 32

        // calculate new rating
        return Math.round(player1Rating + K * (actualScore - expectedOutcome)).toInt()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(SpringBootConsoleApplication::class.java, *args)
        }
    }
}

