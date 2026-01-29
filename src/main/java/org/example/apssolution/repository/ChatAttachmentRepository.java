package org.example.apssolution.repository;

import org.example.apssolution.domain.entity.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment,Long> {
}
