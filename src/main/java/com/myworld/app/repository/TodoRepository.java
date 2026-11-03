
package com.myworld.app.repository;

import com.myworld.app.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoRepository
        extends JpaRepository<Todo, Long> {

    List<Todo> findByMemberIdOrderByCreatedAtDescIdDesc(
            Long memberId
    );

    Optional<Todo> findByIdAndMemberId(
            Long id,
            Long memberId
    );
}
