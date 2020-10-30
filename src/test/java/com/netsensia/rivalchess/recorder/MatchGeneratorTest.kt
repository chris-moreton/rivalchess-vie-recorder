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
}