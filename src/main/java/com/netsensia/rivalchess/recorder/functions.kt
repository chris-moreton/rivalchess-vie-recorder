package com.netsensia.rivalchess.recorder

import com.netsensia.rivalchess.vie.model.EngineRanking
import com.netsensia.rivalchess.vie.model.MatchUpStats
import kotlin.random.Random
import kotlin.streams.toList

fun eloCalc(elo1: Int, elo2: Int, result: String): Pair<Int, Int> {
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

fun getRankingsList(matchUpList: List<MatchUpStats>): List<EngineRanking> {
    val matchCount = matchUpList.sumBy { it.cnt }

    val randomCeil = ((matchCount / 100000) + 1) * 100000
    val eloMap = mutableMapOf<String, EngineRanking>()

    val random = Random(1)
    (0..100000).forEach outer@{
        val r = random.nextInt(randomCeil)
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
                return@outer
            }
        }
    }

    println("Returning sorted rankings")
    return eloMap.values.stream()
            .sorted(Comparator.comparingInt(EngineRanking::elo).reversed())
            .toList()
}
