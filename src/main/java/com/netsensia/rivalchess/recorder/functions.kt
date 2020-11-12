package com.netsensia.rivalchess.recorder

import com.netsensia.rivalchess.recorder.entity.MatchUp
import com.netsensia.rivalchess.vie.model.EngineRanking
import com.netsensia.rivalchess.vie.model.MatchUpStats
import com.netsensia.rivalchess.vie.model.MatchUpStatsConsolidated
import java.util.*
import kotlin.Comparator
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

fun getMatchUpList(matchUps: List<MatchUp>): List<MatchUpStats> {
    val matchUpList = matchUps.stream().map {
        MatchUpStats(it.engine1, it.engine2, it.result, it.cnt)
    }.sorted(Comparator.comparing(MatchUpStats::engine1).reversed()).toList()
    return matchUpList
}

fun getRankingsListWithVieRanking(
        rankingsList: List<EngineRanking>,
        matchUpListConsolidated: List<MatchUpStatsConsolidated>): List<EngineRanking> {

    val updatedRankingsList = mutableMapOf<String, EngineRanking>()

    matchUpListConsolidated.forEach { matchUpConsolidated ->
        updatedRankingsList.putIfAbsent(matchUpConsolidated.engine1,
                rankingsList.filter { it.name == matchUpConsolidated.engine1 }.first())
        val currentRanking = updatedRankingsList.get(matchUpConsolidated.engine1)
        if (matchUpConsolidated.engine1Wins > matchUpConsolidated.engine2Wins) {
            val winPercent =
                    (matchUpConsolidated.engine1Wins.toDouble() /
                            (matchUpConsolidated.engine1Wins + matchUpConsolidated.engine2Wins).toDouble()) * 100.0

            val points = (winPercent - 50).toInt()

            updatedRankingsList.put(matchUpConsolidated.engine1,
                    EngineRanking(currentRanking!!.name,
                            currentRanking!!.elo,
                            currentRanking!!.played,
                            currentRanking!!.vie + points)
            )
        }
    }
    return updatedRankingsList.values.toList().sortedBy { it.vie }.reversed()
}

fun getRankingsList(matchUpStatsList: List<MatchUpStats>): List<EngineRanking> {

    val eloMap = mutableMapOf<String, EngineRanking>()
    val resultsMap = mutableMapOf<Int, MatchUpStats>()
    val random = Random(1)
    matchUpStatsList.forEach { matchUpStats ->
        (0 until matchUpStats.cnt).forEach {
            resultsMap.put(random.nextInt(), matchUpStats)
        }
    }

    val resultsKeys = listOf(*(resultsMap.keys.sorted().toTypedArray()))

    resultsKeys.forEach {
        val result = resultsMap[it]!!

        eloMap.putIfAbsent(result.engine1, EngineRanking(result.engine1, 1200, 0, 0))
        eloMap.putIfAbsent(result.engine2, EngineRanking(result.engine2, 1200, 0, 0))

        val elo1 = eloMap.get(result.engine1)!!.elo
        val elo2 = eloMap.get(result.engine2)!!.elo
        val newElos = eloCalc(elo1, elo2, result.result)

        eloMap.set(result.engine1, EngineRanking(
                result.engine1,
                newElos.first,
                eloMap.get(result.engine1)!!.played + 1,
                eloMap.get(result.engine1)!!.vie)
        )

        eloMap.set(result.engine2, EngineRanking(
                result.engine2,
                newElos.second,
                eloMap.get(result.engine2)!!.played + 1,
                eloMap.get(result.engine2)!!.vie)
        )
    }

    println("Returning sorted rankings")
    return eloMap.values.stream()
            .sorted(Comparator.comparing(EngineRanking::name).reversed())
            .toList()
}

fun engine1Inc(result: String) = if (result.equals("1-0")) 1 else 0
fun engine2Inc(result: String) = if (result.equals("0-1")) 1 else 0
fun drawInc(result: String) = if (result.equals("1/2-1/2")) 1 else 0
fun engine1AsWhiteInc(whiteEngine: String, thisEngine: String) = if (whiteEngine.equals(thisEngine)) 1 else 0

fun reverseResult(result: String) =
        when (result) {
            "1-0" -> "0-1"
            "0-1" -> "1-0"
            else -> "1/2-1/2"
        }

fun getMatchUpListConsolidated(matchUpStatsList: List<MatchUpStats>): List<MatchUpStatsConsolidated> {
    val map = mutableMapOf<String, MatchUpStatsConsolidated>()

    matchUpStatsList.forEach { matchUp ->
        if (!matchUp.engine1.equals(matchUp.engine2)) {

            val localEngine1 = if (matchUp.engine1.compareTo(matchUp.engine2) < 0) matchUp.engine1 else matchUp.engine2
            val localEngine2 = if (matchUp.engine1.compareTo(matchUp.engine2) > 0) matchUp.engine1 else matchUp.engine2
            val localResult = if (localEngine1.equals(matchUp.engine1)) matchUp.result else reverseResult(matchUp.result)

            val key = "${localEngine1}_v_${localEngine2}"

            val consolidatedStats = map.getOrDefault(
                    key,
                    MatchUpStatsConsolidated(localEngine1, localEngine2,0,0,0,0)
            )

            val engine1AsWhiteCount = consolidatedStats.engine1AsWhiteCount + (engine1AsWhiteInc(matchUp.engine1, localEngine1) * matchUp.cnt)
            val engine1Wins = consolidatedStats.engine1Wins + (engine1Inc(localResult) * matchUp.cnt)
            val engine2Wins = consolidatedStats.engine2Wins + (engine2Inc(localResult) * matchUp.cnt)
            val draws = consolidatedStats.draws + (drawInc(localResult) * matchUp.cnt)
            val total = engine1Wins + draws + engine2Wins

            map.put(key, MatchUpStatsConsolidated(
                    consolidatedStats.engine1,
                    consolidatedStats.engine2,
                    engine1Wins,
                    draws,
                    engine2Wins,
                    engine1AsWhiteCount)
            )

            val reverseKey = "${localEngine2}_v_${localEngine1}"

            map.put(reverseKey, MatchUpStatsConsolidated(
                    consolidatedStats.engine2,
                    consolidatedStats.engine1,
                    engine2Wins,
                    draws,
                    engine1Wins,
                    total - engine1AsWhiteCount
            ))
        }
    }

    return map.values.toList().sortedBy { "${it.engine1} - ${it.engine2}" }
}