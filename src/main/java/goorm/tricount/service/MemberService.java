package goorm.tricount.service;

import goorm.tricount.enums.TricountApiErrorCode;
import goorm.tricount.model.Member;
import goorm.tricount.repository.MemberRepository;
import goorm.tricount.util.TricountApiConst;
import goorm.tricount.util.TricountApiException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입
    public Member signup(Member member) {
        return memberRepository.save(member);
    }

    // 로그인
    public Member login(String loginId, String password) {
        // id, pw가 맞으면 통과, 맞지 않으면 로그인을 실패
        Member loginMember = memberRepository.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .orElseThrow(() -> new TricountApiException("회원을 찾을 수 없습니다. 로그인 실패", TricountApiErrorCode.ACCESS_DENIED));

        return loginMember;
    }

    // 로그아웃
    public void logout(HttpServletResponse response) {
        // 로그인 쿠키를 초기화
        expireCookie(response, TricountApiConst.LOGIN_MEMBER_COOKIE);
    }

    // 회원 조회
    public Member findMemberById(Long memberId) {
        Optional<Member> loginMember = memberRepository.findById(memberId);
        if (!loginMember.isPresent()) { // 존재하지 않으면
            throw new TricountApiException("회원을 찾을 수 없습니다!", TricountApiErrorCode.ACCESS_DENIED);
        }
        return loginMember.get();
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
