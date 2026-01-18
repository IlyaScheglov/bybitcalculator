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

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USER_SETTINGS_RESPONSE_FORMAT = """
            Текущие настройки пользователя: 
            Процент лонг: %s,
            Минуты проверки лонг: %s,
            Процент шорт: %s,
            Минуты проверки шорт: %s,
            Процент дамп: %s,
            Минуты проверки дамп: %s
            """;

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

    public String getUserSettings(String tgId) {
        var user = userRepository.findByTgId(tgId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return String.format(USER_SETTINGS_RESPONSE_FORMAT, user.getLongPercent(), user.getLongMinutes(),
                user.getShortPercent(), user.getShortMinutes(), user.getDumpPercent(), user.getDumpMinutes());
    }

    @Transactional
    public String changeUserSetting(ChangeUserSettingsDto requestDto) {
        try {
            var user = userRepository.findByTgId(requestDto.getTgId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            user.setLongPercent(CodeHelperUtils.oldOrNewValue(user.getLongPercent(), requestDto.getLongPercent()))
                    .setLongMinutes(CodeHelperUtils.oldOrNewValue(user.getLongMinutes(), requestDto.getLongMinutes()))
                    .setShortPercent(CodeHelperUtils.oldOrNewValue(user.getShortPercent(), requestDto.getShortPercent()))
                    .setShortMinutes(CodeHelperUtils.oldOrNewValue(user.getShortMinutes(), requestDto.getShortMinutes()))
                    .setDumpPercent(CodeHelperUtils.oldOrNewValue(user.getDumpPercent(), requestDto.getDumpPercent()))
                    .setDumpMinutes(CodeHelperUtils.oldOrNewValue(user.getDumpMinutes(), requestDto.getDumpMinutes()));
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
