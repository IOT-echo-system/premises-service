package com.robotutor.premisesService.services

import com.robotutor.iot.auditOnError
import com.robotutor.iot.auditOnSuccess
import com.robotutor.iot.exceptions.DataNotFoundException
import com.robotutor.iot.exceptions.UnAuthorizedException
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.utils.createMono
import com.robotutor.iot.utils.createMonoError
import com.robotutor.iot.utils.models.UserData
import com.robotutor.iot.utils.utils.toMap
import com.robotutor.loggingstarter.logOnError
import com.robotutor.loggingstarter.logOnSuccess
import com.robotutor.premisesService.controllers.view.PremisesRequest
import com.robotutor.premisesService.exceptions.IOTError
import com.robotutor.premisesService.models.IdType
import com.robotutor.premisesService.models.Premises
import com.robotutor.premisesService.models.PremisesId
import com.robotutor.premisesService.models.Role
import com.robotutor.premisesService.repositories.PremisesRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class PremisesService(
    private val premisesRepository: PremisesRepository,
    private val idGeneratorService: IdGeneratorService,
) {
    fun createPremises(premisesRequest: PremisesRequest, userData: UserData): Mono<Premises> {
        val premisesRequestMap = premisesRequest.toMap().toMutableMap()
        return idGeneratorService.generateId(IdType.PREMISES_ID).flatMap { premisesId ->
            premisesRequestMap["premisesId"] = premisesId
            val premises = Premises.from(premisesId, premisesRequest, userData.userId)
            premisesRepository.save(premises)
                .auditOnSuccess("PREMISES_CREATE", premisesRequestMap)
        }
            .auditOnError("PREMISES_CREATE", premisesRequestMap)
            .logOnSuccess("Successfully created premises!")
            .logOnError("", "Failed to create premises!")
    }

    fun getAllPremises(userData: UserData): Flux<Premises> {
        return premisesRepository.findAllByUsers_UserId(userData.userId)
    }

    fun getPremises(premisesId: PremisesId, userData: UserData): Mono<Premises> {
        return premisesRepository.findByPremisesIdAndUsers_UserId(premisesId, userData.userId)
            .switchIfEmpty {
                createMonoError(DataNotFoundException(IOTError.IOT0401))
            }
    }

    fun getPremisesForOwner(premisesId: PremisesId, userData: UserData): Mono<Premises> {
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

    fun updatePremises(premisesId: PremisesId, premisesRequest: PremisesRequest, userData: UserData): Mono<Premises> {
        val premisesRequestMap = premisesRequest.toMap().toMutableMap()
        premisesRequestMap["premisesId"] = premisesId
        return this.getPremisesForOwner(premisesId, userData)
            .flatMap {
                premisesRepository.save(it.update(premisesRequest))
            }
            .auditOnSuccess("PREMISES_UPDATE", premisesRequestMap)
            .auditOnError("PREMISES_UPDATE", premisesRequestMap)
            .logOnSuccess("Successfully updated premises!", additionalDetails = premisesRequestMap)
            .logOnError("", "Failed to update premises!", additionalDetails = premisesRequestMap)
    }
}

