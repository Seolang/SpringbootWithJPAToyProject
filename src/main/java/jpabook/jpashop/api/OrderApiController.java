package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByCriteria(new OrderSearch());
        for (Order order : all) {
           order.getMember().getName();
           order.getDelivery().getAddress();

           List<OrderItem> orderItems = order.getOrderItems();
           orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByCriteria(new OrderSearch());
        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {

        // order와 orderItems를 join fetch 하는 과정에서 데이터가 중복 발생한다.
        // (order가 각각의 orderItems만큼 뻥튀기된다)
        // 따라서 distinct 문구를 넣으므로써 중복을 제거한다.

        // 1대다 패치조인에서는 페이징을 쓸 수 없다(메모리 오버플로 발생할 수 있음()
        // 또한 distinct 전 순서로 페이징하므로 부정확함

        // 2개 이상의 1대다 관계에 대한 패치조인은 사용해선 안된다.

        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_paging(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {

        // 1. toOne 관계인 member와 delivery에 대한 패치조인만 진행한다.(페이징이 가능하다)
        // 2. hibernate의 batch size 설정을 한다. => in 쿼리를 사용하여 orderID를 가지는 orderItems 를 batch size 만큼 한번에 가져온다.
        //                                          또한 orderItemsID를 가지는 item 을 한번에 가져온다.
        // 결과적으로 1+N+N 쿼리를 1+1+1 쿼리 요청으로 바꾸어준다.

        // %% default batch fetch size 옵션은 컬렉션이나 프록시 객체들을 in 쿼리를 사용하여 한꺼번에 조회하는 설정이다.

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }
    
    // 챕터 결론
    // toOne 관계는 fetch join을 이용하여 최적화 하고,
    // 컬렉션, toMany 등 나머지는 hibernate의 default_batch_fetch_size 옵션으로 최적화하자

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;


        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }

    }

    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;


        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

}
