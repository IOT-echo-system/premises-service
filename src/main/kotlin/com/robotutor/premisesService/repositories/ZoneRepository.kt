package com.robotutor.premisesService.repositories

import com.robotutor.premisesService.models.PremisesId
import com.robotutor.premisesService.models.Zone
import com.robotutor.premisesService.models.ZoneId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface ZoneRepository : ReactiveCrudRepository<Zone, ZoneId> {
    fun findAllByPremisesId(premisesId: PremisesId): Flux<Zone>
    fun findByPremisesIdAndZoneId(premisesId: PremisesId, zoneId: ZoneId): Mono<Zone>
}
