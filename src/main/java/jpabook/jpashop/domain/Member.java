package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    //@NotEmpty
    // Entity 단에서 validation을 검증하는것은 옳은가? (Entity 스펙의 변화)
    // 화면 구성에서는 name이 필요 없을 수도 있다.
    // 또한 변수명이 바뀌면 API가 동작하지 않을 수 있음
    // 해결방안 : 별도의 Data Transfer Object(DTO)를 만든다.
    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
