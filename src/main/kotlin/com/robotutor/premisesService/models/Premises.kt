package com.robotutor.premisesService.models

import com.robotutor.premisesService.controllers.view.AddressRequest
import com.robotutor.premisesService.controllers.view.PremisesRequest
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

const val PREMISES_COLLECTION = "premises"

@TypeAlias("Premises")
@Document(PREMISES_COLLECTION)
data class Premises(
    @Id
    var id: ObjectId? = null,
    @Indexed(unique = true)
    val premisesId: PremisesId,
    val name: String,
    val createdBy: UserId,
    val address: Address,
    val users: List<UserWithRole>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun from(premisesId: String, premisesRequest: PremisesRequest, userId: String): Premises {
            return Premises(
                premisesId = premisesId,
                name = premisesRequest.name,
                address = Address.from(premisesRequest.address),
                users = listOf(UserWithRole(userId, Role.OWNER)),
                createdBy = userId
            )
        }
    }
}

data class Address(
    val address1: String,
    val address2: String? = null,
    val city: String,
    val district: String,
    val state: String,
    val zipCode: Int
) {
    companion object {
        fun from(address: AddressRequest): Address {
            return Address(
                address1 = address.address1,
                address2 = address.address2,
                city = address.city,
                district = address.district,
                state = address.state,
                zipCode = address.zipCode
            )
        }
    }
}

data class UserWithRole(val userId: UserId, val role: Role)

enum class Role {
    OWNER, ADMIN, USER
}

typealias PremisesId = String
typealias UserId = String

