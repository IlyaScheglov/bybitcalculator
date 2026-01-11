package sry.mail.BybitCalculator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sry.mail.BybitCalculator.dto.PurchaseRequestDto;
import sry.mail.BybitCalculator.service.PurchaseService;
import sry.mail.BybitCalculator.util.ExceptionMessagesInterceptionUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public String getUserPurchasesInfo(@RequestParam("tgId") String tgId) {
        return ExceptionMessagesInterceptionUtils.getOrReturnExceptionMessage(
                () -> purchaseService.getUserPurchasesInfo(tgId));
    }

    @PostMapping
    public String createPurchase(@RequestBody PurchaseRequestDto requestDto) {
        return ExceptionMessagesInterceptionUtils.getOrReturnExceptionMessage(
                () -> purchaseService.createPurchase(requestDto.getTgId(), requestDto.getSymbol()));
    }

    @DeleteMapping
    public String deletePurchase(@RequestBody PurchaseRequestDto requestDto) {
        return ExceptionMessagesInterceptionUtils.getOrReturnExceptionMessage(
                () -> purchaseService.deletePurchase(requestDto.getTgId(), requestDto.getSymbol()));
    }
}
