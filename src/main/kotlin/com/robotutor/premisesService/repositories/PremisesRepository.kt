package com.robotutor.premisesService.repositories

import com.robotutor.premisesService.models.Premises
import com.robotutor.premisesService.models.PremisesId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface PremisesRepository : ReactiveCrudRepository<Premises, PremisesId> {
    fun findAllByUsers_UserId(userId: String): Flux<Premises>
    fun findByPremisesIdAndUsers_UserId(premisesId: PremisesId, userId: String): Mono<Premises>
}
