package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 protected로 설정, 생성을 static함수를 이용해서만 할 수 있도록 제약
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // X to Many 는 기본 fetch가 Lazy 이다.
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)   //CASCADE를 하므로써 연관관계에 있는 다른 엔티티를 동시에 PERSIST 한다.
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;    // 주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status;         // 주문상태 [ORDER, CANCEL]

    //==연관관계 메서드==//
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
       Order order = new Order();
       order.setMember(member);
       order.setDelivery(delivery);
       for (OrderItem orderItem : orderItems) {
           order.addOrderItem(orderItem);
       }
       order.setStatus(OrderStatus.ORDER);
       order.setOrderDate(LocalDateTime.now());
       return order;
    }

    //==비즈니스 로직==//
    /**
     * 주문 취소
     */
    public void cancel() {
        if (this.delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("Already Completed Item can't be cancelled");
        }

        this.setStatus(OrderStatus.CANCEL);
        for(OrderItem orderItem : this.orderItems) {
            orderItem.cancel();
        }

    }

    //==조회 로직==//

    /**
     * 전체 주문 가격
     */
    public int getTotalPrice() {
        /*
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
        */

        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }


}
