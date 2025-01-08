package com.robotutor.premisesService.controllers

import com.robotutor.iot.utils.filters.annotations.RequirePolicy
import com.robotutor.iot.utils.models.UserData
import com.robotutor.premisesService.controllers.view.ZoneRequest
import com.robotutor.premisesService.controllers.view.ZoneView
import com.robotutor.premisesService.models.PremisesId
import com.robotutor.premisesService.models.ZoneId
import com.robotutor.premisesService.services.ZoneService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/premises/{premisesId}/zones")
class ZoneController(private val zoneService: ZoneService) {

    @RequirePolicy("ZONE:CREATE")
    @PostMapping
    fun createZone(
        @PathVariable premisesId: PremisesId,
        @RequestBody @Validated zoneRequest: ZoneRequest,
        userData: UserData
    ): Mono<ZoneView> {
        return zoneService.createZone(premisesId, zoneRequest, userData).map { ZoneView.from(it) }
    }

    @RequirePolicy("ZONE:READ")
    @GetMapping
    fun getZones(@PathVariable premisesId: PremisesId, userData: UserData): Flux<ZoneView> {
        return zoneService.getZonesByPremisesId(premisesId, userData).map { ZoneView.from(it) }
    }

    @RequirePolicy("ZONE:READ")
    @GetMapping("/{zoneId}")
    fun getZone(@PathVariable zoneId: ZoneId, userData: UserData, @PathVariable premisesId: String): Mono<ZoneView> {
        return zoneService.getZoneByZoneId(premisesId, zoneId, userData).map { ZoneView.from(it) }
    }
}
