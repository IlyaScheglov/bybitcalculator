package sry.mail.BybitCalculator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sry.mail.BybitCalculator.dto.CreateUserRequestDto;
import sry.mail.BybitCalculator.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "active", constant = "false")
    @Mapping(target = "minPercentOfDump", constant = "5")
    @Mapping(target = "minPercentOfIncome", constant = "5")
    User mapCreateUserDtoToUserEntity(CreateUserRequestDto createUserDto);
}
