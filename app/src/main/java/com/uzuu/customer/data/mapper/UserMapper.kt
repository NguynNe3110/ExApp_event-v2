package com.uzuu.customer.data.mapper

import com.uzuu.customer.data.local.entity.UsersEntity
import com.uzuu.customer.data.remote.dto.response.UserResponseDto
import com.uzuu.customer.domain.model.Users


fun Users.userdomainToEntity(old: UsersEntity?): UsersEntity{
    return UsersEntity(
        id = id,
        username = username,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        address = address,
        url = old?.url
    )
}

fun UsersEntity.userEntityToDomain(): Users {
    return Users(
        id = id,
        username = username,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        address = address
    )
}

fun UserResponseDto.userDtoToDomain(): Users{
    return Users(
        id = id,
        username = username,
        email = email,
        fullName = fullName,
        phoneNumber = phone,
        address = address,
    )
}