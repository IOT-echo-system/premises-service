package com.robotutor.premisesService.controllers

import com.robotutor.iot.utils.filters.annotations.RequirePolicy
import com.robotutor.iot.utils.models.UserAuthenticationData
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
@RequestMapping("/zones")
class ZoneController(private val zoneService: ZoneService) {

    @RequirePolicy("ZONE:CREATE")
    @PostMapping
    fun createZone(@RequestBody @Validated zoneRequest: ZoneRequest, userData: UserAuthenticationData): Mono<ZoneView> {
        return zoneService.createZone(zoneRequest, userData).map { ZoneView.from(it) }
    }

    @RequirePolicy("ZONE:READ")
    @GetMapping("/premises/{premisesId}")
    fun getZones(@PathVariable premisesId: PremisesId, userData: UserAuthenticationData): Flux<ZoneView> {
        return zoneService.getZonesByPremisesId(premisesId, userData).map { ZoneView.from(it) }
    }

    @RequirePolicy("ZONE:READ")
    @GetMapping("/{zoneId}")
    fun getZone(@PathVariable zoneId: ZoneId, userData: UserAuthenticationData): Mono<ZoneView> {
        return zoneService.getZoneByZoneId(zoneId, userData).map { ZoneView.from(it) }
    }
}
