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
        Assert.assertEquals(engineRankings.get(0).name, "34.0.2")
    }

    @Test
    fun testConsolidatedMatchUpsList1() {
        val payload = MatchGeneratorTest::class.java.getResource("/matchUps1.json").readText()
        val listType: Type = object : TypeToken<List<MatchUpStats?>?>() {}.type
        val matchUpsList = Gson().fromJson<List<MatchUpStats>>(payload, listType)
        val consolidated = getMatchUpListConsolidated(matchUpsList)
        Assert.assertEquals(consolidated.get(0).engine1, "34.0.3")
        Assert.assertEquals(consolidated.get(0).engine2, "34.0.4")
        Assert.assertEquals(consolidated.get(0).engine1Wins, 963)
        Assert.assertEquals(consolidated.get(0).engine2Wins, 964)
        Assert.assertEquals(consolidated.get(0).draws, 1115)
        Assert.assertEquals(consolidated.get(0).engine1AsWhiteCount, 1521)
    }

    @Test
    fun testConsolidatedMatchUpsList2() {
        val payload = MatchGeneratorTest::class.java.getResource("/matchUps2.json").readText()
        val listType: Type = object : TypeToken<List<MatchUpStats?>?>() {}.type
        val matchUpsList = Gson().fromJson<List<MatchUpStats>>(payload, listType)
        val consolidated = getMatchUpListConsolidated(matchUpsList)
        Assert.assertEquals(consolidated.get(0).engine1, "00.0.1")
        Assert.assertEquals(consolidated.get(0).engine2, "cuckoo110")
        Assert.assertEquals(consolidated.get(0).engine1Wins, 0)
        Assert.assertEquals(consolidated.get(0).engine2Wins, 148)
        Assert.assertEquals(consolidated.get(0).draws, 15)
        Assert.assertEquals(consolidated.get(0).engine1AsWhiteCount, 85)
    }
}
