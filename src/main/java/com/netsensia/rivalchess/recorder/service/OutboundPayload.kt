package com.netsensia.rivalchess.recorder.service

import com.netsensia.rivalchess.vie.model.MatchUpStats

data class OutboundPayload (
    val matchUps: List<MatchUpStats>
)