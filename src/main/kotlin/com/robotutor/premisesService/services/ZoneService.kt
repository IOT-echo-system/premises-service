package com.robotutor.premisesService.services

import com.robotutor.iot.auditOnError
import com.robotutor.iot.auditOnSuccess
import com.robotutor.iot.exceptions.UnAuthorizedException
import com.robotutor.iot.models.AddWidgetMessage
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.utils.createMono
import com.robotutor.iot.utils.createMonoError
import com.robotutor.iot.utils.exceptions.IOTError
import com.robotutor.iot.utils.gateway.views.PremisesRole
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
        return createMono(premisesData.user.role == PremisesRole.OWNER)
            .flatMap {
                if (it) idGeneratorService.generateId(IdType.ZONE_ID)
                else createMonoError(UnAuthorizedException(IOTError.IOT0104))
            }
            .flatMap { zoneId ->
                zoneRequestMap["zoneId"] = zoneId
                val zone = Zone(zoneId = zoneId, premisesId = premisesData.premisesId, name = zoneRequest.name)
                zoneRepository.save(zone)
                    .auditOnSuccess("ZONE_CREATE", zoneRequestMap)
            }
            .flatMap { zone ->
                premisesService.addZone(zone, userData).map { zone }
            }
            .auditOnError("ZONE_CREATE", zoneRequestMap)
            .logOnSuccess(logger, "Successfully added a zone in premises $zoneRequest")
            .logOnError(logger, "", "Failed to add a zone in premises $zoneRequest")
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

        return createMono(premisesData.user.role == PremisesRole.OWNER)
            .flatMap {
                if (it) zoneRepository.findByPremisesIdAndZoneId(premisesData.premisesId, zoneId)
                else createMonoError(UnAuthorizedException(IOTError.IOT0104))
            }
            .flatMap { zone ->
                zoneRepository.save(zone.updateName(zoneRequest.name))
                    .auditOnSuccess("ZONE_UPDATE", zoneRequestMap)
            }
            .auditOnError("ZONE_UPDATE", zoneRequestMap)
            .logOnSuccess(logger, "Successfully updated a zone name", additionalDetails = zoneRequestMap)
            .logOnError(logger, "", "Failed to update zone name", additionalDetails = zoneRequestMap)
    }

    fun addWidget(message: AddWidgetMessage, premisesData: PremisesData, userData: UserData): Mono<Zone> {
        return zoneRepository.findByPremisesIdAndZoneId(premisesData.premisesId, message.zoneId)
            .flatMap {
                zoneRepository.save(it.addWidget(message.widgetId))
            }
    }
}

