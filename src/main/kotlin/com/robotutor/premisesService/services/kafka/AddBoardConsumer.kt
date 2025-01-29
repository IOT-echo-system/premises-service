package com.robotutor.premisesService.services.kafka

import com.robotutor.iot.models.KafkaTopicName
import com.robotutor.iot.models.Message
import com.robotutor.iot.services.KafkaConsumer
import com.robotutor.iot.utils.models.PremisesData
import com.robotutor.premisesService.models.PremisesId
import com.robotutor.premisesService.services.PremisesService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono


@Service
class AddBoardConsumer(private val kafkaConsumer: KafkaConsumer, private val premisesService: PremisesService) {

    @PostConstruct
    fun consume() {
        kafkaConsumer.consume(listOf(KafkaTopicName.ADD_BOARD), AddBoardMessage::class.java) {
            Mono.deferContextual { ctx ->
                val premisesData = ctx.get(PremisesData::class.java)
                premisesService.addBoard(it.message, premisesData)
            }
        }
            .subscribe()
    }
}


data class AddBoardMessage(
    val boardId: String,
    val premisesId: PremisesId,
    val name: String,
) : Message()
