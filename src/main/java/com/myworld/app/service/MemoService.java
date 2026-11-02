
package com.myworld.app.service;

import com.myworld.app.entity.Memo;
import com.myworld.app.repository.MemoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MemoService {

    private final MemoRepository memoRepository;

    public MemoService(MemoRepository memoRepository) {
        this.memoRepository = memoRepository;
    }

    public List<Memo> findMyMemos(Long memberId) {
        return memoRepository
                .findByMemberIdOrderByUpdatedAtDescIdDesc(memberId);
    }

    @Transactional
    public Memo create(
            Long memberId,
            String title,
            String content
    ) {
        String safeTitle = validateTitle(title);
        String safeContent = validateContent(content);

        Memo memo = new Memo(
                memberId,
                safeTitle,
                safeContent
        );

        return memoRepository.save(memo);
    }

    @Transactional
    public Memo update(
            Long memberId,
            Long memoId,
            String title,
            String content
    ) {
        Memo memo = findOwnedMemo(memberId, memoId);

        String safeTitle = validateTitle(title);
        String safeContent = validateContent(content);

        memo.update(safeTitle, safeContent);

        return memo;
    }

    @Transactional
    public void delete(Long memberId, Long memoId) {
        Memo memo = findOwnedMemo(memberId, memoId);

        memoRepository.delete(memo);
    }

    private Memo findOwnedMemo(Long memberId, Long memoId) {
        return memoRepository
                .findByIdAndMemberId(memoId, memberId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "메모를 찾을 수 없습니다."
                        )
                );
    }

    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "메모 제목을 입력해 주세요."
            );
        }

        String value = title.trim();

        if (value.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "제목은 100자 이하로 입력해 주세요."
            );
        }

        return value;
    }

    private String validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "메모 내용을 입력해 주세요."
            );
        }

        String value = content.trim();

        if (value.length() > 2000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "내용은 2000자 이하로 입력해 주세요."
            );
        }

        return value;
    }
}
