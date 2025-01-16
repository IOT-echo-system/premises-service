package com.robotutor.premisesService.services

import com.robotutor.iot.auditOnError
import com.robotutor.iot.auditOnSuccess
import com.robotutor.iot.exceptions.DataNotFoundException
import com.robotutor.iot.exceptions.UnAuthorizedException
import com.robotutor.iot.service.CacheService
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.utils.createMono
import com.robotutor.iot.utils.createMonoError
import com.robotutor.iot.utils.models.UserData
import com.robotutor.iot.utils.utils.toMap
import com.robotutor.loggingstarter.Logger
import com.robotutor.loggingstarter.logOnError
import com.robotutor.loggingstarter.logOnSuccess
import com.robotutor.premisesService.controllers.view.PremisesRequest
import com.robotutor.premisesService.exceptions.IOTError
import com.robotutor.premisesService.models.*
import com.robotutor.premisesService.repositories.PremisesRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PremisesService(
    private val premisesRepository: PremisesRepository,
    private val idGeneratorService: IdGeneratorService,
    private val cacheService: CacheService
) {
    val logger = Logger(this::class.java)
    fun createPremises(premisesRequest: PremisesRequest, userData: UserData): Mono<Premises> {
        val premisesRequestMap = premisesRequest.toMap().toMutableMap()
        return cacheService.evict("premises::${userData.userId}")
            .flatMap {
                idGeneratorService.generateId(IdType.PREMISES_ID).flatMap { premisesId ->
                    premisesRequestMap["premisesId"] = premisesId
                    val premises = Premises.from(premisesId, premisesRequest, userData.userId)
                    cacheService.update("premises::$premisesId-${userData.userId}") {
                        premisesRepository.save(premises)
                    }
                }
            }
            .auditOnSuccess("PREMISES_CREATE", premisesRequestMap)
            .auditOnError("PREMISES_CREATE", premisesRequestMap)
            .logOnSuccess(logger, "Successfully created premises!")
            .logOnError(logger, "", "Failed to create premises!")
    }


    fun getAllPremises(userData: UserData): Flux<Premises> {
        return cacheService.retrieves("premises::${userData.userId}", Premises::class.java) {
            premisesRepository.findAllByUsers_UserId(userData.userId)
        }
    }

    fun getPremises(premisesId: PremisesId, userData: UserData): Mono<Premises> {
        return cacheService.retrieve("premises::$premisesId-${userData.userId}", Premises::class.java) {
            premisesRepository.findByPremisesIdAndUsers_UserId(premisesId, userData.userId)
                .switchIfEmpty {
                    createMonoError(DataNotFoundException(IOTError.IOT0401))
                }
        }
    }

    fun updatePremises(premisesId: PremisesId, premisesRequest: PremisesRequest, userData: UserData): Mono<Premises> {
        val premisesRequestMap = premisesRequest.toMap().toMutableMap()
        premisesRequestMap["premisesId"] = premisesId
        return this.getPremisesForOwner(premisesId, userData)
            .flatMap { premises ->
                cacheService.update("premises::$premisesId-${userData.userId}") {
                    premisesRepository.save(premises.update(premisesRequest))
                }
            }
            .auditOnSuccess("PREMISES_UPDATE", premisesRequestMap)
            .auditOnError("PREMISES_UPDATE", premisesRequestMap)
            .logOnSuccess(logger, "Successfully updated premises!", additionalDetails = premisesRequestMap)
            .logOnError(logger, "", "Failed to update premises!", additionalDetails = premisesRequestMap)
    }

    fun addZone(zone: Zone, userData: UserData): Mono<Premises> {
        return premisesRepository.findByPremisesIdAndUsers_UserId(zone.premisesId, userData.userId)
            .flatMap { premises ->
                cacheService.update("premises::${premises.premisesId}-${userData.userId}") {
                    premisesRepository.save(premises.addZone(zone.zoneId))
                }
            }
    }

    private fun getPremisesForOwner(premisesId: PremisesId, userData: UserData): Mono<Premises> {
        return premisesRepository.findByPremisesIdAndUsers_UserId(premisesId, userData.userId)
            .flatMap { premises ->
                val currentUser = premises.users.find { it.userId == userData.userId }!!
                if (currentUser.role == Role.OWNER) {
                    createMono(premises)
                } else {
                    createMonoError(UnAuthorizedException(IOTError.IOT0402))
                }
            }
            .switchIfEmpty {
                createMonoError(DataNotFoundException(IOTError.IOT0401))
            }
    }
}

