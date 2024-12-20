package com.robotutor.premisesService.services

import com.robotutor.iot.exceptions.DataNotFoundException
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.utils.createMonoError
import com.robotutor.iot.utils.models.UserData
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
) {
    fun createPremises(premisesRequest: PremisesRequest, userData: UserData): Mono<Premises> {
        return idGeneratorService.generateId(IdType.PREMISES_ID).flatMap { premisesId ->
            val premises = Premises(
                premisesId = premisesId,
                name = premisesRequest.name,
                address = Address(street = "", city = "", country = ""),
                users = listOf(UserWithRole(userId = userData.userId, role = "PREMISES_OWNER")),
                createdBy = userData.userId
            )
            premisesRepository.save(premises)
        }
            .logOnSuccess("Successfully created premises!")
            .logOnSuccess("Failed to create premises!")
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
}

