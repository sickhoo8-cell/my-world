
package com.myworld.app.controller;

import com.myworld.app.entity.Memo;
import com.myworld.app.repository.MemberRepository;
import com.myworld.app.service.MemoService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/memos")
public class MemoController {

    private final MemoService memoService;
    private final MemberRepository memberRepository;

    public MemoController(
            MemoService memoService,
            MemberRepository memberRepository
    ) {
        this.memoService = memoService;
        this.memberRepository = memberRepository;
    }

    // 본인의 메모 목록 조회
    @GetMapping
    public List<MemoResponse> getMyMemos(
            HttpSession session
    ) {
        Long memberId = getLoginMemberId(session);

        return memoService.findMyMemos(memberId)
                .stream()
                .map(MemoResponse::from)
                .toList();
    }

    // 새 메모 저장
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemoResponse createMemo(
            @RequestBody MemoRequest request,
            HttpSession session
    ) {
        Long memberId = getLoginMemberId(session);

        Memo saved = memoService.create(
                memberId,
                request.title(),
                request.content()
        );

        return MemoResponse.from(saved);
    }

    // 내 메모 수정
    @PutMapping("/{memoId}")
    public MemoResponse updateMemo(
            @PathVariable Long memoId,
            @RequestBody MemoRequest request,
            HttpSession session
    ) {
        Long memberId = getLoginMemberId(session);

        Memo updated = memoService.update(
                memberId,
                memoId,
                request.title(),
                request.content()
        );

        return MemoResponse.from(updated);
    }

    // 내 메모 삭제
    @DeleteMapping("/{memoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMemo(
            @PathVariable Long memoId,
            HttpSession session
    ) {
        Long memberId = getLoginMemberId(session);

        memoService.delete(memberId, memoId);
    }

    // 로그인 여부 확인
    private Long getLoginMemberId(HttpSession session) {
        Object value = session.getAttribute("loginMemberId");

        if (!(value instanceof Long memberId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }

        if (!memberRepository.existsById(memberId)) {
            session.invalidate();

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "다시 로그인해 주세요."
            );
        }

        return memberId;
    }

    // 요청 데이터
    public record MemoRequest(
            String title,
            String content
    ) {
    }

    // 응답 데이터
    public record MemoResponse(
            Long id,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static MemoResponse from(Memo memo) {
            return new MemoResponse(
                    memo.getId(),
                    memo.getTitle(),
                    memo.getContent(),
                    memo.getCreatedAt(),
                    memo.getUpdatedAt()
            );
        }
    }
}
