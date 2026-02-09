package org.example.apssolution.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.apssolution.repository.AccountRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DeleteAccountScheduler {

    final AccountRepository accountRepository;

    @Transactional
    @Async("taskExecutor")
    @Scheduled(cron = "0 0 0 1 * *") // 매월 1일 0시 0분 0초
    public void printHelloWorld() {
        LocalDateTime threshold = LocalDateTime.now().minusYears(3);
        int deletedCount =
                accountRepository.deleteResignedAccountsBefore(threshold);
        System.out.println("[AccountCleanup] 3년 경과 탈퇴 계정 삭제 완료 : " + deletedCount + "건");
    }
}

