package com.netsensia.rivalchess.recorder

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.netsensia.rivalchess.vie.model.MatchUpStats
import java.lang.reflect.Type;
import org.junit.Assert
import org.junit.Test

class MatchGeneratorTest {
    @Test
    fun testEloCalculation() {
        Assert.assertEquals(Pair(1220, 1280), eloCalc(1200, 1300, "1-0"))
        Assert.assertEquals(Pair(1188, 1312), eloCalc(1200, 1300, "0-1"))
        Assert.assertEquals(Pair(1204, 1296), eloCalc(1200, 1300, "1/2-1/2"))
    }

    @Test
    fun testRankingsList() {
        val payload = MatchGeneratorTest::class.java.getResource("/matchUps1.json").readText()
        val listType: Type = object : TypeToken<List<MatchUpStats?>?>() {}.type
        val matchUpsList = Gson().fromJson<List<MatchUpStats>>(payload, listType)
        val engineRankings = getRankingsList(matchUpsList)
        Assert.assertEquals(engineRankings.get(0).name, "34.0.4")
    }

    @Test
    fun testConsolidatedMatchUpsList1() {
        val payload = MatchGeneratorTest::class.java.getResource("/matchUps1.json").readText()
        val listType: Type = object : TypeToken<List<MatchUpStats?>?>() {}.type
        val matchUpsList = Gson().fromJson<List<MatchUpStats>>(payload, listType)
        val consolidated = getMatchUpListConsolidated(matchUpsList)
        Assert.assertEquals(consolidated.get(0).engine1, "00.0.1")
        Assert.assertEquals(consolidated.get(0).engine2, "34.0.3")
        Assert.assertEquals(consolidated.get(0).engine1Wins, 5)
        Assert.assertEquals(consolidated.get(0).engine2Wins, 283)
        Assert.assertEquals(consolidated.get(0).draws, 15)
        Assert.assertEquals(consolidated.get(0).engine1AsWhiteCount, 150)
    }

    @Test
    fun testConsolidatedMatchUpsList2() {
        val payload = MatchGeneratorTest::class.java.getResource("/matchUps2.json").readText()
        val listType: Type = object : TypeToken<List<MatchUpStats?>?>() {}.type
        val matchUpsList = Gson().fromJson<List<MatchUpStats>>(payload, listType)
        val consolidated = getMatchUpListConsolidated(matchUpsList)
        Assert.assertEquals(consolidated.get(0).engine1, "00.0.1")
        Assert.assertEquals(consolidated.get(0).engine2, "34.0.0")
        Assert.assertEquals(consolidated.get(0).engine1Wins, 3)
        Assert.assertEquals(consolidated.get(0).engine2Wins, 138)
        Assert.assertEquals(consolidated.get(0).draws, 14)
        Assert.assertEquals(consolidated.get(0).engine1AsWhiteCount, 79)
    }

    @Test
    fun testVieScore() {
        val payload = MatchGeneratorTest::class.java.getResource("/matchUps3.json").readText()
        val listType: Type = object : TypeToken<List<MatchUpStats?>?>() {}.type
        val matchUpsList = Gson().fromJson<List<MatchUpStats>>(payload, listType)
        val vieScores = getRankingsListWithVieRanking(getRankingsList(
                matchUpsList),
                getMatchUpListConsolidated(matchUpsList))
        Assert.assertEquals("chess22k-1.14", vieScores.get(0).name)
        Assert.assertEquals("cuckoo110", vieScores.get(1).name)
        Assert.assertEquals("1.0.3", vieScores.get(2).name)
        Assert.assertEquals("00.0.1", vieScores.get(18).name)
    }
}
