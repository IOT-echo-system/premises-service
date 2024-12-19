package com.robotutor.premisesService.models

import com.robotutor.iot.service.IdSequenceType


enum class IdType(override val length: Int) : IdSequenceType {
    PREMISES_ID(8),
    ZONE_ID(12),
}
