package com.robotutor.premisesService.exceptions

import com.robotutor.iot.exceptions.ServiceError


enum class IOTError(override val errorCode: String, override val message: String) : ServiceError {
    IOT0401("IOT-0401", "Premises not found"),
    IOT0402("IOT-0402", "User does not have permissions to update premises."),
}
