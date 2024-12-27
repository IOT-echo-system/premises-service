package com.robotutor.premisesService.controllers.view

import com.robotutor.premisesService.models.Address
import com.robotutor.premisesService.models.Premises
import com.robotutor.premisesService.models.PremisesId
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class PremisesRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 4, max = 30, message = "Name should not be less than 4 char or more than 30 char")
    val name: String,

    @field:Valid
    val address: AddressRequest,
)

data class AddressRequest(
    @field:NotBlank(message = "Address1 is required")
    @field:Size(min = 4, max = 50, message = "Address1 should not be less than 4 characters or more than 50 characters")
    val address1: String,
    val address2: String? = null,

    @field:NotBlank(message = "District is required")
    @field:Size(min = 4, max = 30, message = "District should not be less than 4 characters or more than 30 characters")
    val district: String,

    @field:NotBlank(message = "State is required")
    @field:Size(min = 2, max = 30, message = "State should not be less than 2 characters or more than 30 characters")
    val state: String,

    @field:Min(100000, message = "Pincode should be at least 6 digits")
    @field:Max(999999, message = "Pincode should not exceed 6 digits")
    val pincode: Int
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
