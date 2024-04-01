package goorm.tricount.repository;

import goorm.tricount.model.Member;

import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByLoginId(String loginId);
}
