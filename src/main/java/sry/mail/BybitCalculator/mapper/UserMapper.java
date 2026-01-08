package sry.mail.BybitCalculator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sry.mail.BybitCalculator.dto.CreateUserRequestDto;
import sry.mail.BybitCalculator.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "active", constant = "false")
    User mapCreateUserDtoToUserEntity(CreateUserRequestDto createUserDto);
}
