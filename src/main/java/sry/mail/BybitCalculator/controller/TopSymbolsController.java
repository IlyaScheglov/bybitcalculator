package sry.mail.BybitCalculator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sry.mail.BybitCalculator.service.CalculationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/top-symbols")
public class TopSymbolsController {

    private final CalculationService calculationService;

    @GetMapping
    public String getTopPumpsAndDumpsForMinutes(@RequestParam("minutes") Integer minutes) {
        return calculationService.getTopSymbolsByPumpsAndDumpsForMinutes(minutes);
    }
}
