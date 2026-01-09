package sry.mail.BybitCalculator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sry.mail.BybitCalculator.dto.ChangeUserSettingsDto;
import sry.mail.BybitCalculator.dto.CreateUserRequestDto;
import sry.mail.BybitCalculator.service.UserService;
import sry.mail.BybitCalculator.util.ExceptionMessagesInterceptionUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping
    public String createUser(@RequestBody CreateUserRequestDto requestDto) {
        return ExceptionMessagesInterceptionUtils.getOrReturnExceptionMessage(
                () -> userService.createNewUser(requestDto));
    }

    @PutMapping
    public String changeUserSettings(@RequestBody ChangeUserSettingsDto requestDto) {
        return ExceptionMessagesInterceptionUtils.getOrReturnExceptionMessage(
                () -> userService.changeUserSetting(requestDto));
    }

    @PatchMapping("/make-active")
    public String makeUserActive(@RequestParam("tgId") String tgId) {
        return ExceptionMessagesInterceptionUtils.getOrReturnExceptionMessage(
                () -> userService.changeUserActivityStatus(tgId, true));
    }

    @PatchMapping("/make-inactive")
    public String makeUserInactive(@RequestParam("tgId") String tgId) {
        return ExceptionMessagesInterceptionUtils.getOrReturnExceptionMessage(
                () -> userService.changeUserActivityStatus(tgId, false));
    }
}
