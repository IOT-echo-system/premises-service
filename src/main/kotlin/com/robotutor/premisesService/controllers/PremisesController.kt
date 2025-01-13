package com.robotutor.premisesService.controllers

import com.robotutor.iot.utils.filters.annotations.RequirePolicy
import com.robotutor.iot.utils.models.UserData
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
    fun addPremises(@RequestBody @Validated premisesRequest: PremisesRequest, userData: UserData): Mono<PremisesView> {
        return premisesService.createPremises(premisesRequest, userData).map { PremisesView.from(it, userData.userId) }
    }

    @RequirePolicy("PREMISES:READ")
    @GetMapping
    fun getAllPremises(userData: UserData): Flux<PremisesView> {
        return premisesService.getAllPremises(userData).map { PremisesView.from(it, userData.userId) }
    }

    @RequirePolicy("PREMISES:READ")
    @GetMapping("/{premisesId}")
    fun getPremises(@PathVariable premisesId: PremisesId, userData: UserData): Mono<PremisesView> {
        println("------------$premisesId-----${userData}----------------")
        return premisesService.getPremises(premisesId, userData).map { PremisesView.from(it, userData.userId) }
    }

    @RequirePolicy("PREMISES:UPDATE")
    @PutMapping("/{premisesId}")
    fun updatePremises(
        @PathVariable premisesId: PremisesId,
        @RequestBody @Validated premisesRequest: PremisesRequest,
        userData: UserData,
    ): Mono<PremisesView> {
        return premisesService.updatePremises(premisesId, premisesRequest, userData).map {
            PremisesView.from(it, userData.userId)
        }
    }
}
