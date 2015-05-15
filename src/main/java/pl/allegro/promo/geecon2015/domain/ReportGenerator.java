package pl.allegro.promo.geecon2015.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.allegro.promo.geecon2015.domain.stats.FinancialStatisticsRepository;
import pl.allegro.promo.geecon2015.domain.transaction.TransactionRepository;
import pl.allegro.promo.geecon2015.domain.transaction.UserTransaction;
import pl.allegro.promo.geecon2015.domain.user.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Spliterator;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Component
public class ReportGenerator {

    private final FinancialStatisticsRepository financialStatisticsRepository;

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    @Autowired
    public ReportGenerator(FinancialStatisticsRepository financialStatisticsRepository,
                           UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.financialStatisticsRepository = financialStatisticsRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public Report generate(ReportRequest request) {
        final Report report = new Report();
        try {
            StreamSupport.stream(financialStatisticsRepository
                    .listUsersWithMinimalIncome(request.getMinimalIncome(), request.getUsersToCheck()).spliterator(), false)
                    .map(uuid -> new ReportedUser(uuid, getUserName(uuid), sum(uuid)))
                    .forEach(u -> report.add(u));
            return report;
        } catch (Exception ex) {
            ex.printStackTrace();
            return report;
        }
    }

    private BigDecimal sum(UUID uuid) {
        try {
            return transactionRepository
                    .transactionsOf(uuid)
                    .getTransactions()
                    .stream()
                    .map(UserTransaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception ex) {
            return null;
        }
    }

    private String getUserName(UUID uuid) {
        try {
            return userRepository.detailsOf(uuid).getName();
        } catch (Exception e) {
            return "<failed>";
        }
    }

}

