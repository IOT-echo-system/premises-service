package com.robotutor.premisesService.controllers.view

import com.robotutor.premisesService.models.Address
import com.robotutor.premisesService.models.Premises
import com.robotutor.premisesService.models.PremisesId
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class PremisesRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 4, max = 30, message = "Name should not be less than 4 char or more than 30 char")
    val name: String,
)

data class PremisesView(
    val premisesId: PremisesId,
    val name: String,
    val address: Address,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(premises: Premises): PremisesView {
            return PremisesView(
                premisesId = premises.premisesId,
                name = premises.name,
                address = premises.address,
                createdAt = premises.createdAt
            )
        }
    }
}
