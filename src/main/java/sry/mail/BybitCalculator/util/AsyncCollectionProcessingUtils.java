package sry.mail.BybitCalculator.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class AsyncCollectionProcessingUtils {

    @Qualifier("asyncExecutor")
    private final Executor executor;

    public <I> void runForEachElementAsync(Collection<I> inputCollection, Consumer<I> inputElementTask) {
        var completableFutures = new CopyOnWriteArrayList<CompletableFuture<Void>>();

        for (var el : inputCollection) {
            completableFutures.add(CompletableFuture.runAsync(() -> inputElementTask.accept(el), executor));
        }

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
    }
}
