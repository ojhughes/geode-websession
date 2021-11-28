package me.ohughes.cache;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.gemfire.AbstractGemFireOperationsSessionRepository.GemFireSession;
import org.springframework.session.data.gemfire.AbstractGemFireOperationsSessionRepository.GemFireSessionAttributes;
import org.springframework.session.data.gemfire.GemFireOperationsSessionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Component
public class GemfireSessionRepository implements ReactiveSessionRepository<GemFireSession<GemFireSessionAttributes>> {

    private final GemFireOperationsSessionRepository sessionRepository;
    private final ThreadPoolTaskExecutor taskExecutor;

    public GemfireSessionRepository(GemFireOperationsSessionRepository sessionRepository, ThreadPoolTaskExecutor taskExecutor) {
        this.sessionRepository = sessionRepository;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public Mono<GemFireSession<GemFireSessionAttributes>> createSession() {
        return Mono.fromCallable(() -> {
                    Callable<GemFireSession<GemFireSessionAttributes>> createSession = GemFireSession::create;
                    return taskExecutor.submit(createSession).get();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> save(GemFireSession<GemFireSessionAttributes> session) {
        return Mono.fromCallable(() -> {
                    
                    Callable<Void> saveGemfireSession = () -> {
                        sessionRepository.save(session);
                        return null;
                    };
                    return taskExecutor.submit(saveGemfireSession).get();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<GemFireSession<GemFireSessionAttributes>> findById(String id) {
        return Mono.fromCallable(() -> {
                    Callable<GemFireSession<GemFireSessionAttributes>> saveGemfireSession = () -> {
                        Session gemfireSession = sessionRepository.findById(id);
                        if (gemfireSession != null) {
                            return GemFireSession.from(gemfireSession);
                        }
                        else {
                            return null;
                        }
                    };
                    return taskExecutor.submit(saveGemfireSession).get();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.fromCallable(() -> {
                    Callable<Void> saveGemfireSession = () -> {
                        sessionRepository.deleteById(id);
                        return null;
                    };
                    return taskExecutor.submit(saveGemfireSession).get();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

}
