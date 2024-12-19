package com.robotutor.premisesService.controllers

import com.robotutor.iot.utils.filters.annotations.RequirePolicy
import com.robotutor.iot.utils.models.UserAuthenticationData
import com.robotutor.premisesService.controllers.view.PremisesRequest
import com.robotutor.premisesService.controllers.view.PremisesView
import com.robotutor.premisesService.models.PremisesId
import com.robotutor.premisesService.services.PremisesService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/premises")
class PremisesController(private val premisesService: PremisesService) {

    @RequirePolicy("PREMISES:CREATE")
    @PostMapping
    fun addPremises(
        @RequestBody @Validated premisesRequest: PremisesRequest,
        userData: UserAuthenticationData
    ): Mono<PremisesView> {
        return premisesService.createPremises(premisesRequest, userData).map { PremisesView.from(it) }
    }

    @RequirePolicy("PREMISES:READ")
    @GetMapping
    fun getAllPremises(userData: UserAuthenticationData): Flux<PremisesView> {
        return premisesService.getAllPremises(userData).map { PremisesView.from(it) }
    }

    @RequirePolicy("PREMISES:READ")
    @GetMapping("/{premisesId}")
    fun getPremises(@PathVariable premisesId: PremisesId, userData: UserAuthenticationData): Mono<PremisesView> {
        return premisesService.getPremises(premisesId, userData).map { PremisesView.from(it) }
    }

}
