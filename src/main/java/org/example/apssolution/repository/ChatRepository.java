package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, String> {
    Chat findBySignature(String signature);

    @Query("""
    SELECT c FROM Chat c
    JOIN c.chatMembers cm
    WHERE cm.account.id = :accountId
      AND cm.leftAt IS NULL
""")
    List<Chat> findAllByMemberAccountId(@Param("accountId") String accountId);

}
