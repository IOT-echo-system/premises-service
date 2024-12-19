package com.robotutor.premisesService.controllers.view

import com.robotutor.premisesService.models.PremisesId
import com.robotutor.premisesService.models.Zone
import com.robotutor.premisesService.models.ZoneId
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ZoneRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 4, max = 30, message = "Name should not be less than 4 char or more than 30 char")
    val name: String,
    @field:NotBlank(message = "Premises is required")
    val premisesId: PremisesId,
)

data class ZoneView(
    val zoneId: ZoneId,
    val premisesId: PremisesId,
    val name: String,
) {
    companion object {
        fun from(zone: Zone): ZoneView {
            return ZoneView(zoneId = zone.zoneId, premisesId = zone.premisesId, name = zone.name)
        }
    }
}
