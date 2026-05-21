package id.ac.ui.cs.advprog.bidmartwalletservice.serenity;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.service.WalletAuctionService;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.annotations.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SerenityJUnit5Extension.class)
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:wallet-serenity;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class WalletConcurrencySerenityTest {

    private static final Long USER_ID = 7101L;
    private static final Long INITIAL_BALANCE = 1000L;
    private static final Long HOLD_AMOUNT = 700L;

    @Autowired
    private WalletAuctionService walletAuctionService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    @Title("Concurrent backend hold requests reserve funds only once")
    void concurrentHoldRequestsReserveFundsOnlyOnce() throws Exception {
        givenWalletWithAvailableBalance();

        List<Throwable> outcomes = whenTwoHoldRequestsArriveAtTheSameTime();

        thenOnlyOneHoldRequestSucceeds(outcomes);
        thenWalletBalanceRemainsConsistent();
        thenOnlyOneHoldTransactionIsRecorded();
    }

    private void givenWalletWithAvailableBalance() {
        walletRepository.saveAndFlush(Wallet.builder()
                .userId(USER_ID)
                .balance(INITIAL_BALANCE)
                .heldBalance(0L)
                .build());
    }

    private List<Throwable> whenTwoHoldRequestsArriveAtTheSameTime() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        List<Callable<Throwable>> requests = IntStream.range(0, 2)
                .mapToObj(index -> (Callable<Throwable>) () -> executeHoldRequest(index, ready, start))
                .toList();

        List<Future<Throwable>> futures = requests.stream()
                .map(executor::submit)
                .toList();

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();

        try {
            return futures.stream()
                    .map(this::getOutcome)
                    .toList();
        } finally {
            executor.shutdownNow();
        }
    }

    private Throwable executeHoldRequest(int index, CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        ready.countDown();
        assertTrue(start.await(5, TimeUnit.SECONDS));

        try {
            walletAuctionService.holdAmount(USER_ID, HOLD_AMOUNT, "serenity-hold-" + index);
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }

    private Throwable getOutcome(Future<Throwable> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            return exception;
        }
    }

    private void thenOnlyOneHoldRequestSucceeds(List<Throwable> outcomes) {
        long successfulRequests = outcomes.stream()
                .filter(Objects::isNull)
                .count();

        List<Throwable> failedRequests = outcomes.stream()
                .filter(Objects::nonNull)
                .toList();

        assertEquals(1, successfulRequests);
        assertEquals(1, failedRequests.size());
        assertTrue(failedRequests.get(0) instanceof IllegalStateException);
        assertEquals("Saldo tidak cukup untuk melakukan bid", failedRequests.get(0).getMessage());
    }

    private void thenWalletBalanceRemainsConsistent() {
        Wallet wallet = walletRepository.findById(USER_ID).orElseThrow();

        assertEquals(300L, wallet.getBalance());
        assertEquals(700L, wallet.getHeldBalance());
    }

    private void thenOnlyOneHoldTransactionIsRecorded() {
        List<Transaction> holdTransactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(USER_ID).stream()
                .filter(transaction -> transaction.getType() == TransactionType.HOLD)
                .toList();

        assertEquals(1, holdTransactions.size());
        assertEquals(HOLD_AMOUNT, holdTransactions.get(0).getAmount());
        assertEquals(300L, holdTransactions.get(0).getBalanceAfter());
        assertEquals(700L, holdTransactions.get(0).getHeldBalanceAfter());
    }
}
