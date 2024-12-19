package com.robotutor.premisesService.models

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
)

data class Address(val street: String, val city: String, val country: String)
data class UserWithRole(val userId: UserId, val role: RoleId)

typealias PremisesId = String
typealias UserId = String
typealias RoleId = String
