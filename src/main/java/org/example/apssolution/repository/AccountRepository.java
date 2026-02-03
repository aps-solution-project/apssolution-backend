package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // 채팅방 멤버 초대 리스트용: 아직 퇴사하지 않은 사람만 조회
    List<Account> findAllByResignedAtIsNull();
}
