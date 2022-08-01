package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;



    // 준영속 엔티티 저장 방법
    // 1. 변경감지기능 사용
    @Transactional
    public void updateItem(Long itemId, Book param) {
        Item findItem = itemRepository.findOne(itemId);
        findItem.setPrice(param.getPrice());
        findItem.setName(param.getName());
        findItem.setStockQuantity(param.getStockQuantity());

        // 함수가 끝나면 Spring의 Transactional annotation에 의해 JPA commit 및 flush가 발생
        // JPA에서 변경된 값을 찾은 후 DB에 전달하여 적용함
    }

    // 2. 병합(Merge) 사용
    @Transactional  // 없으면 readonly라 저장이 안됨
    public void saveItem(Item item) {
        itemRepository.save(item);
    }
    // 병합의 주의점
    // 변경감지는 원하는 값만 교체하지만, 병합은 모든 값을 바꾸며, 값이 없을 시 NULL로 교체할 가능성이 있다.
    // 따라서 실무에서는 변경감지를 추천하는 편

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

}
