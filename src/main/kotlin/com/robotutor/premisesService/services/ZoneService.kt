package com.robotutor.premisesService.services

import com.robotutor.iot.auditOnError
import com.robotutor.iot.auditOnSuccess
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.utils.models.UserData
import com.robotutor.iot.utils.utils.toMap
import com.robotutor.loggingstarter.logOnError
import com.robotutor.loggingstarter.logOnSuccess
import com.robotutor.premisesService.controllers.view.ZoneRequest
import com.robotutor.premisesService.models.IdType
import com.robotutor.premisesService.models.PremisesId
import com.robotutor.premisesService.models.Zone
import com.robotutor.premisesService.models.ZoneId
import com.robotutor.premisesService.repositories.ZoneRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ZoneService(
    private val premisesService: PremisesService,
    private val zoneRepository: ZoneRepository,
    private val idGeneratorService: IdGeneratorService,
) {
    fun createZone(zoneRequest: ZoneRequest, userData: UserData): Mono<Zone> {
        val zoneRequestMap = zoneRequest.toMap().toMutableMap()
        return premisesService.getPremises(zoneRequest.premisesId, userData)
            .flatMap { idGeneratorService.generateId(IdType.ZONE_ID) }
            .flatMap { zoneId ->
                zoneRequestMap["zoneId"] = zoneId
                val zone = Zone(zoneId = zoneId, premisesId = zoneRequest.premisesId, name = zoneRequest.name)
                zoneRepository.save(zone)
                    .auditOnSuccess("ZONE_CREATE", zoneRequestMap)
            }
            .auditOnError("ZONE_CREATE", zoneRequestMap)
            .logOnSuccess("Successfully added a zone in premises ${zoneRequest.premisesId}")
            .logOnError("", "Failed to add a zone in premises ${zoneRequest.premisesId}")
    }

    fun getZonesByPremisesId(premisesId: PremisesId, userData: UserData): Flux<Zone> {
        return premisesService.getPremises(premisesId, userData)
            .flatMapMany {
                zoneRepository.findAllByPremisesId(premisesId)
            }
    }

    fun getZoneByZoneId(zoneId: ZoneId, userData: UserData): Mono<Zone> {
        return zoneRepository.findByZoneId(zoneId)
            .flatMap { zone ->
                premisesService.getPremises(zone.premisesId, userData).map { zone }
            }
    }
}

