package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * X to One (ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    // 단순히 배열을 return 할 시
    // Order 내의 Member에서 다시 Order를 참조하면서 무한루프에 빠진다.
    // 이를 방지하기 위해선 JsonIgnore를 사용해야 한다.
    // 허나 Order의 Member는 Lazy fetch 이기 때문에 Proxy Member를 생성하여 넣어놓는다.
    // 이를 JSON 변환하는 과정에서 에러를 발생시킴
    // 해결방안 : Hibernate5Module이 필요
    @GetMapping("api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        return all;
    }

    @GetMapping("api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        
        // ORDER 2개
        // N + 1 문제 -> 주문 목록 조회 1 + 회원 조회 2 + 배송 조회 2 = 5번 쿼리 발생 (최악의 경우)
        // EAGER Fetch로 바꾸어도 최적화 문제를 해결하기는 어려움
        // (지연로딩인 영속성 컨텍스트이므로 이미 조회한 자료는 쿼리를 발생시키진 않는다)
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        
        // 루프
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

    }

    @GetMapping("api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        
        // 최초 order 리스트를 요청하는 쿼리 단 한개만 발생함
        // 실무에서 정말 자주 사용하는 fetch join

        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
    }

    @GetMapping("api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {

        // 쿼리를 직접 작성하므로써 select 절에서의 색인 데이터 양을 줄일 수 있다
        // + 애플리케이션 네트워크 용량 최적화 (생각보다 미미)
        // - Repository 재사용성이 낮아짐 (Repository는 Entity를 조회하기 위해 사용하는 것임)
        // 차선책 : query용 repository를 따로 생성한다.

        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화 (Proxy를 실체화)
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }

    }

}
