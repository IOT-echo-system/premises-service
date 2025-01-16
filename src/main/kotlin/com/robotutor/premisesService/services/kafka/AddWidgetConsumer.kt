package com.robotutor.premisesService.services.kafka

import com.robotutor.iot.models.KafkaTopicName
import com.robotutor.iot.models.Message
import com.robotutor.iot.services.KafkaConsumer
import com.robotutor.iot.utils.models.PremisesData
import com.robotutor.iot.utils.models.UserData
import com.robotutor.premisesService.models.ZoneId
import com.robotutor.premisesService.services.ZoneService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AddWidgetConsumer(
    private val zoneService: ZoneService,
    private val kafkaConsumer: KafkaConsumer
) {
    @PostConstruct
    fun subscribe() {
        kafkaConsumer.consume(listOf(KafkaTopicName.ADD_WIDGET), AddWidgetMessage::class.java)
            .flatMap {
                Mono.deferContextual { ctx ->
                    val userData = ctx.get(UserData::class.java)
                    val premisesData = ctx.get(PremisesData::class.java)
                    zoneService.addWidget(it.message, premisesData, userData)
                }
            }
            .subscribe()
    }
}


data class AddWidgetMessage(
    val widgetId: String,
    val zoneId: ZoneId,
    val name: String,
) : Message()
