package sry.mail.BybitCalculator.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sry.mail.BybitCalculator.entity.Purchase;
import sry.mail.BybitCalculator.repository.ChartRepository;
import sry.mail.BybitCalculator.repository.PurchaseRepository;
import sry.mail.BybitCalculator.repository.UserRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final ChartRepository chartRepository;

    @Transactional
    public String createPurchase(String tgId, String symbol) {
        var user = userRepository.findByTgId(tgId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (user.getPurchases().stream().anyMatch(purchase -> Objects.equals(purchase.getSymbol(), symbol))) {
            throw new RuntimeException(String.format("Спот %s уже куплен", symbol));
        }

        var lastPrice = chartRepository.findTopBySymbolOrderByTimestampDesc(symbol)
                .orElseThrow(() -> new RuntimeException("По данному споту не найдено записей по цене")).getPrice();
        var newPurchase = new Purchase()
                .setUser(user)
                .setSymbol(symbol)
                .setBuyPrice(lastPrice);
        purchaseRepository.save(newPurchase);
        return String.format("Спот %s успешно куплен", symbol);
    }

    @Transactional
    public String deletePurchase(String tgId, String symbol) {
        var purchase = purchaseRepository.findBySymbolAndUserTgId(symbol, tgId)
                .orElseThrow(() -> new RuntimeException(String.format("Покупка пользователем спота %s не найдена", symbol)));
        purchaseRepository.delete(purchase);
        return String.format("Покупка пользователем спота %s успешно удалена", symbol);
    }
}
