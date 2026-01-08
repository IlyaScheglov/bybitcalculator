package sry.mail.BybitCalculator.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class TransactionManipulator {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T doInNewTransaction(Supplier<T> supplier) {
        return supplier.get();
    }
}
