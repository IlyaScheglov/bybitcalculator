package sry.mail.BybitCalculator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sry.mail.BybitCalculator.dto.ChangeUserSettingsDto;
import sry.mail.BybitCalculator.dto.CreateUserRequestDto;
import sry.mail.BybitCalculator.mapper.UserMapper;
import sry.mail.BybitCalculator.repository.UserRepository;
import sry.mail.BybitCalculator.util.CodeHelperUtils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${api-key}")
    private String apiKey;

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Transactional
    public String createNewUser(CreateUserRequestDto requestDto) {
        if (!Objects.equals(requestDto.getApiKey(), apiKey)) {
            throw new RuntimeException("API ключ задан неверно, ваш пользователь не зарегестрирован");
        }

        var optionalUserInDb = userRepository.findByTgId(requestDto.getTgId());
        if (optionalUserInDb.isPresent()) {
            throw new RuntimeException("Пользователь для вашего телеграм аккаунта уже создан");
        }
        userRepository.save(userMapper.mapCreateUserDtoToUserEntity(requestDto));
        return "Пользователь успешно создан с выключенным состоянием поиска";
    }

    @Transactional
    public String changeUserSetting(ChangeUserSettingsDto requestDto) {
        try {
            var user = userRepository.findByTgId(requestDto.getTgId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            user.setMinPercentOfDump(CodeHelperUtils.oldOrNewValue(user.getMinPercentOfDump(), requestDto.getMinPercentOfDump()))
                    .setMinPercentOfIncome(CodeHelperUtils.oldOrNewValue(user.getMinPercentOfIncome(), requestDto.getMinPercentOfIncome()));
            userRepository.save(user);
            return "Настройки пользователя изменены";
        } catch (Exception ex) {
            throw new RuntimeException("Похоже изменение настроек произошло некорректно");
        }
    }

    @Transactional
    public String changeUserActivityStatus(String tgId, Boolean isActive) {
        var user = userRepository.findByTgId(tgId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        userRepository.save(user.setActive(isActive));
        return "Состояние поиска для пользователя изменено на " + isActive;
    }
}
