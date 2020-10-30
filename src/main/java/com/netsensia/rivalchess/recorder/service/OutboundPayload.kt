package com.netsensia.rivalchess.recorder.service

import com.netsensia.rivalchess.vie.model.EngineRanking
import com.netsensia.rivalchess.vie.model.MatchUpStats
import com.netsensia.rivalchess.vie.model.MatchUpStatsConsolidated

data class OutboundPayload (
    val matchUps: List<MatchUpStats>,
    val rankings: List<EngineRanking>,
    val matchUpsConsolidated: List<MatchUpStatsConsolidated>
)