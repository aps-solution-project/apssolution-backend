package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember,Long> {
    Optional<ChatMember> findByChatIdAndAccountId(String chatId, String accountId);

    long countByChat_IdAndLeftAtIsNull(String chatId);

}
