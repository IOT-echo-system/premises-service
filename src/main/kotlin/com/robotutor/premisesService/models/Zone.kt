package com.robotutor.premisesService.models

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

const val ZONE_COLLECTION = "zones"

@TypeAlias("Zone")
@Document(ZONE_COLLECTION)
data class Zone(
    @Id
    var id: ObjectId? = null,
    @Indexed(unique = true)
    val zoneId: ZoneId,
    val premisesId: PremisesId,
    var name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val widgets: Set<String> = emptySet()
) {
    fun updateName(name: String): Zone {
        this.name = name
        return this;
    }

    fun addWidget(widgetId: String): Zone {
        this.widgets.plus(widgetId)
        return this
    }
}

typealias ZoneId = String
