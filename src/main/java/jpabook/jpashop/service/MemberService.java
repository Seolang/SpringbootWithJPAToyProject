package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // 읽기전용으로 조회 성능 최적화
@RequiredArgsConstructor // final 변수에 대하여 생성자를 만들어줌
public class MemberService {
//    Field injection 방식
//    @Autowired
//    private MemberRepository memberRepository;

    // Constructor Injection 방식
    private final MemberRepository memberRepository;

    //@Autowired 없어도 작동한다
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }
    
    //회원 가입
    @Transactional  // makes readOnly = false, higher optional priority
    public Long join(Member member) {
        validateDuplicateMember(member);    // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    // validation을 하더라도 거의 동시에 등록한다면 통과될 수도 있음. 따라서 name을 unique로 만드는 것을 추천
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());

        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("Already exist member.");
        }
    }
    
    //회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);

        // 변경감지를 이용한 update
    }
}
