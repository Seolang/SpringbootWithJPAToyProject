package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // findById와 findAll은 JpaRepository의 기본 함수를 상속받는다

    // findBy* 형식으로 함수를 지정하면 지금과 같은 경우
    // select m from Member m where m.name = ?
    // 라는 JPQL을 자동으로 생성해준다
    List<Member> findByName(String name); // 이러면 실행됩니다...
    
}
