package com.robotutor.premisesService.controllers

import com.robotutor.iot.utils.filters.annotations.RequirePolicy
import com.robotutor.iot.utils.models.PremisesData
import com.robotutor.iot.utils.models.UserData
import com.robotutor.premisesService.controllers.view.ZoneRequest
import com.robotutor.premisesService.controllers.view.ZoneView
import com.robotutor.premisesService.models.ZoneId
import com.robotutor.premisesService.services.ZoneService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/zones")
class ZoneController(private val zoneService: ZoneService) {

    @RequirePolicy("ZONE:CREATE")
    @PostMapping
    fun createZone(
        @RequestBody @Validated zoneRequest: ZoneRequest,
        userData: UserData,
        premisedData: PremisesData
    ): Mono<ZoneView> {
        return zoneService.createZone(zoneRequest, userData, premisedData).map { ZoneView.from(it) }
    }

    @RequirePolicy("ZONE:READ")
    @GetMapping
    fun getZones(userData: UserData, premisesData: PremisesData): Flux<ZoneView> {
        return zoneService.getZonesByPremisesId(premisesData.premisesId, userData).map { ZoneView.from(it) }
    }

    @RequirePolicy("ZONE:READ")
    @GetMapping("/{zoneId}")
    fun getZone(@PathVariable zoneId: ZoneId, userData: UserData, premisesData: PremisesData): Mono<ZoneView> {
        return zoneService.getZoneByZoneId(premisesData.premisesId, zoneId, userData).map { ZoneView.from(it) }
    }

    @RequirePolicy("ZONE:UPDATE")
    @PutMapping("/{zoneId}/name")
    fun updateZoneName(
        @PathVariable zoneId: ZoneId,
        @RequestBody @Validated zoneRequest: ZoneRequest,
        userData: UserData,
        premisesData: PremisesData
    ): Mono<ZoneView> {
        return zoneService.updateZoneName(zoneId, zoneRequest, userData, premisesData).map { ZoneView.from(it) }
    }
}
