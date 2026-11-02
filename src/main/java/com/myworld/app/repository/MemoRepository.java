
package com.myworld.app.repository;

import com.myworld.app.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemoRepository extends JpaRepository<Memo, Long> {

    List<Memo> findByMemberIdOrderByUpdatedAtDescIdDesc(
            Long memberId
    );

    Optional<Memo> findByIdAndMemberId(
            Long id,
            Long memberId
    );
}
