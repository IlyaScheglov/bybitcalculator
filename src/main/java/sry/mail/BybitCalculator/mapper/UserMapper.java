package sry.mail.BybitCalculator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sry.mail.BybitCalculator.dto.CreateUserRequestDto;
import sry.mail.BybitCalculator.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "active", constant = "false")
    @Mapping(target = "longPercent", constant = "3")
    @Mapping(target = "longMinutes", constant = "3")
    @Mapping(target = "shortPercent", constant = "30")
    @Mapping(target = "shortMinutes", constant = "10")
    @Mapping(target = "dumpPercent", constant = "10")
    @Mapping(target = "dumpMinutes", constant = "15")
    User mapCreateUserDtoToUserEntity(CreateUserRequestDto createUserDto);
}
