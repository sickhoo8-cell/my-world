
package com.myworld.app.service;

import com.myworld.app.entity.DirectMessage;
import com.myworld.app.entity.Member;
import com.myworld.app.repository.DirectMessageRepository;
import com.myworld.app.repository.MemberRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DirectMessageService {

    private final DirectMessageRepository messageRepository;
    private final MemberRepository memberRepository;

    public DirectMessageService(
            DirectMessageRepository messageRepository,
            MemberRepository memberRepository
    ) {
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
    }

    // =========================================
    // 최근 대화 조회
    // =========================================

    @Transactional(readOnly = true)
    public List<MessageView> getConversation(
            Long loginMemberId,
            Long partnerId
    ) {

        requireApprovedMember(loginMemberId);
        requirePartner(loginMemberId, partnerId);

        List<DirectMessage> messages =
                messageRepository.findRecentConversation(
                        loginMemberId,
                        partnerId,
                        PageRequest.of(0, 100)
                );

        // DB에서는 최신 100개를 가져오고,
        // 화면에는 오래된 메시지부터 보여준다.
        List<DirectMessage> ordered =
                new ArrayList<>(messages);

        Collections.reverse(ordered);

        return ordered.stream()
                .map(this::toView)
                .toList();
    }

    // =========================================
    // 메시지 저장
    // =========================================

    @Transactional
    public MessageView sendMessage(
            Long loginMemberId,
            Long partnerId,
            String content
    ) {

        Member sender =
                requireApprovedMember(loginMemberId);

        Member receiver =
                requirePartner(loginMemberId, partnerId);

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "메시지 내용을 입력해주세요."
            );
        }

        String validContent = content.trim();

        if (validContent.length() > 2000) {
            throw new IllegalArgumentException(
                    "메시지는 2000자 이내로 입력해주세요."
            );
        }

        DirectMessage message = new DirectMessage(
                sender,
                receiver,
                validContent
        );

        DirectMessage saved =
                messageRepository.saveAndFlush(message);

        return toView(saved);
    }

    // =========================================
    // 승인된 회원 확인
    // =========================================

    private Member requireApprovedMember(Long id) {

        if (id == null) {
            throw new SecurityException(
                    "로그인이 필요합니다."
            );
        }

        Member member = memberRepository
                .findById(id)
                .orElseThrow(() ->
                        new SecurityException(
                                "유효하지 않은 계정입니다."
                        )
                );

        if (!member.isApproved()) {
            throw new SecurityException(
                    "채팅 이용 권한이 없습니다."
            );
        }

        return member;
    }

    // =========================================
    // 대화 상대 확인
    // =========================================

    private Member requirePartner(
            Long loginMemberId,
            Long partnerId
    ) {

        if (partnerId == null) {
            throw new IllegalArgumentException(
                    "대화 상대가 필요합니다."
            );
        }

        if (partnerId.equals(loginMemberId)) {
            throw new IllegalArgumentException(
                    "자신에게 메시지를 보낼 수 없습니다."
            );
        }

        Member partner = memberRepository
                .findById(partnerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "대화 상대를 찾을 수 없습니다."
                        )
                );

        if (!partner.isApproved()) {
            throw new IllegalArgumentException(
                    "대화할 수 없는 회원입니다."
            );
        }

        return partner;
    }

    // =========================================
    // 응답 DTO 변환
    // =========================================

    private MessageView toView(DirectMessage message) {

        return new MessageView(
                message.getId(),
                message.getSender().getId(),
                message.getReceiver().getId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    public record MessageView(
            Long id,
            Long senderId,
            Long receiverId,
            String senderName,
            String content,
            Instant createdAt
    ) {
    }
}
