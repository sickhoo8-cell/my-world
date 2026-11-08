
package com.myworld.app.repository;

import com.myworld.app.entity.DirectMessage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageRepository
        extends JpaRepository<DirectMessage, Long> {

    @Query("""
        SELECT m
        FROM DirectMessage m
        WHERE
            (m.sender.id = :firstId
                AND m.receiver.id = :secondId)
            OR
            (m.sender.id = :secondId
                AND m.receiver.id = :firstId)
        ORDER BY m.id DESC
        """)
    List<DirectMessage> findRecentConversation(
            @Param("firstId") Long firstId,
            @Param("secondId") Long secondId,
            Pageable pageable
    );
}
