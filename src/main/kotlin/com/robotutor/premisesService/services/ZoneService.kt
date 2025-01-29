package com.robotutor.premisesService.services

import com.robotutor.iot.auditOnError
import com.robotutor.iot.auditOnSuccess
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.utils.filters.validatePremisesOwner
import com.robotutor.iot.utils.models.PremisesData
import com.robotutor.iot.utils.models.UserData
import com.robotutor.iot.utils.utils.toMap
import com.robotutor.loggingstarter.Logger
import com.robotutor.loggingstarter.logOnError
import com.robotutor.loggingstarter.logOnSuccess
import com.robotutor.premisesService.controllers.view.ZoneRequest
import com.robotutor.premisesService.models.IdType
import com.robotutor.premisesService.models.PremisesId
import com.robotutor.premisesService.models.Zone
import com.robotutor.premisesService.models.ZoneId
import com.robotutor.premisesService.repositories.ZoneRepository
import com.robotutor.premisesService.services.kafka.AddWidgetMessage
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ZoneService(
    private val premisesService: PremisesService,
    private val zoneRepository: ZoneRepository,
    private val idGeneratorService: IdGeneratorService,
) {
    val logger = Logger(this::class.java)
    fun createZone(zoneRequest: ZoneRequest, userData: UserData, premisesData: PremisesData): Mono<Zone> {
        val zoneRequestMap = userData.toMap().toMutableMap()
        zoneRequestMap["premisesId"] = premisesData.premisesId
        return validatePremisesOwner(premisesData) { idGeneratorService.generateId(IdType.ZONE_ID) }
            .flatMap { zoneId ->
                zoneRequestMap["zoneId"] = zoneId
                val zone = Zone(zoneId = zoneId, premisesId = premisesData.premisesId, name = zoneRequest.name)
                zoneRepository.save(zone)
            }
            .flatMap { zone ->
                premisesService.addZone(zone, userData).map { zone }
            }
            .auditOnSuccess("ZONE_CREATE", zoneRequestMap)
            .auditOnError("ZONE_CREATE", zoneRequestMap)
            .logOnSuccess(logger, "Successfully added a zone in premises ${premisesData.premisesId}")
            .logOnError(logger, "", "Failed to add a zone in premises ${premisesData.premisesId}")
    }

    fun getZonesByPremisesId(premisesId: PremisesId, userData: UserData): Flux<Zone> {
        return premisesService.getPremises(premisesId, userData)
            .flatMapMany {
                zoneRepository.findAllByPremisesId(premisesId)
            }
    }

    fun getZoneByZoneId(premisesId: PremisesId, zoneId: ZoneId, userData: UserData): Mono<Zone> {
        return premisesService.getPremises(premisesId, userData)
            .flatMap { zoneRepository.findByPremisesIdAndZoneId(premisesId, zoneId) }
    }

    fun updateZoneName(
        zoneId: ZoneId,
        zoneRequest: ZoneRequest,
        userData: UserData,
        premisesData: PremisesData
    ): Mono<Zone> {
        val zoneRequestMap = zoneRequest.toMap().toMutableMap()
        zoneRequestMap["zoneId"] = zoneId
        zoneRequestMap["premisesId"] = premisesData.premisesId

        return validatePremisesOwner(premisesData) {
            zoneRepository.findByPremisesIdAndZoneId(premisesData.premisesId, zoneId)
        }
            .flatMap { zone ->
                zoneRepository.save(zone.updateName(zoneRequest.name))
            }
            .auditOnSuccess("ZONE_UPDATE", zoneRequestMap)
            .auditOnError("ZONE_UPDATE", zoneRequestMap)
            .logOnSuccess(logger, "Successfully updated a zone name", additionalDetails = zoneRequestMap)
            .logOnError(logger, "", "Failed to update zone name", additionalDetails = zoneRequestMap)
    }

    fun addWidget(message: AddWidgetMessage, premisesData: PremisesData): Mono<Zone> {
        return validatePremisesOwner(premisesData) {
            zoneRepository.findByPremisesIdAndZoneId(premisesData.premisesId, message.zoneId)
        }
            .flatMap {
                zoneRepository.save(it.addWidget(message.widgetId))
            }
            .auditOnSuccess("ADD_WIDGET_IN_ZONE", message.toMap())
            .auditOnError("ADD_WIDGET_IN_ZONE", message.toMap())
            .logOnSuccess(logger, "Successfully added new widget in zone", additionalDetails = message.toMap())
            .logOnError(logger, "", "Failed to add new widget in zone", additionalDetails = message.toMap())
    }
}

