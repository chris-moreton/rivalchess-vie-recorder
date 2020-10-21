package com.netsensia.rivalchess.recorder

import com.google.gson.Gson
import com.netsensia.rivalchess.recorder.model.Result
import com.netsensia.rivalchess.recorder.service.ResultService
import com.netsensia.rivalchess.utils.JmsReceiver
import com.netsensia.rivalchess.vie.model.MatchResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component

@SpringBootApplication
@Component
class SpringBootConsoleApplication : CommandLineRunner {

    @Autowired
    private lateinit var resultService: ResultService

    override fun run(vararg args: String) {
        do {
            val gson = Gson()
            val message = JmsReceiver.receive("MatchResults")
            val matchResult = gson.fromJson(message, MatchResult::class.java)
            var entity = Result(
                    engine1 = matchResult.engineSettings.engine1.version,
                    engine2 = matchResult.engineSettings.engine2.version,
                    nodes1 = matchResult.engineSettings.engine1.maxNodes,
                    nodes2 = matchResult.engineSettings.engine2.maxNodes,
                    millis1 = matchResult.engineSettings.engine1.maxMillis,
                    millis2 = matchResult.engineSettings.engine2.maxMillis,
                    pgn = matchResult.pgn
            )
            resultService.save(entity)
        } while (true)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(SpringBootConsoleApplication::class.java, *args)
        }
    }
}
