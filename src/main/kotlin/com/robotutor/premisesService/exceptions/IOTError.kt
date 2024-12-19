package com.robotutor.premisesService.exceptions

import com.robotutor.iot.exceptions.ServiceError


enum class IOTError(override val errorCode: String, override val message: String) : ServiceError {
    IOT0401("IOT-0401", "Premises not found"),

}
