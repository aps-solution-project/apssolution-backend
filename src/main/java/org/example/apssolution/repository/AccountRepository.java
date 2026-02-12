package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // 채팅방 멤버 초대 리스트용: 아직 퇴사하지 않은 사람만 조회
    List<Account> findAllByResignedAtIsNull();
    @Modifying
    @Query("""
        delete from Account a
        where a.resignedAt is not null
          and a.resignedAt < :threshold
    """)
    int deleteResignedAccountsBefore(@Param("threshold") LocalDateTime threshold);
    @Query("""
    select distinct a
    from Account a
    where a.id like %:keyword%
       or a.name like %:keyword%
       or a.email like %:keyword%
       or cast(a.role as string) like %:keyword%
    order by a.workedAt desc
""")
    List<Account> search(@Param("keyword") String keyword, Pageable pageable);

}
