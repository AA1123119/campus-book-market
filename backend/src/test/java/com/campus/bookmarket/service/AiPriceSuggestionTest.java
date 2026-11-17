package com.campus.bookmarket.service;

import com.campus.bookmarket.dto.BookDtos;
import com.campus.bookmarket.repository.BookRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/** 定价模型的边界用例，重点覆盖评审提出的“低价教材区间倒挂”问题。 */
class AiPriceSuggestionTest {

    private AiService newService() {
        return new AiService(mock(BookRepository.class));
    }

    private BookDtos.PriceSuggestionRequest request(String originalPrice, int condition) {
        BookDtos.PriceSuggestionRequest req = new BookDtos.PriceSuggestionRequest();
        req.setOriginalPrice(new BigDecimal(originalPrice));
        req.setConditionLevel(condition);
        req.setCategory("数据结构");
        return req;
    }

    @Test
    void lowPriceBookKeepsBoundsOrdered() {
        BookDtos.PriceSuggestionResponse res = newService().suggestPrice(request("2.00", 4));

        assertTrue(res.getLowBound().compareTo(res.getHighBound()) <= 0,
                "下限不得高于上限：" + res.getLowBound() + " > " + res.getHighBound());
        assertTrue(res.getSuggestedPrice().compareTo(res.getLowBound()) >= 0,
                "建议价不得低于下限");
        assertTrue(res.getSuggestedPrice().compareTo(res.getHighBound()) <= 0,
                "建议价不得高于上限");
        assertTrue(res.getHighBound().compareTo(new BigDecimal("2.00")) <= 0,
                "上限不得超过原价");
    }

    @Test
    void normalPriceBookKeepsSuggestedWithinBounds() {
        BookDtos.PriceSuggestionResponse res = newService().suggestPrice(request("49.80", 4));

        assertTrue(res.getLowBound().compareTo(res.getHighBound()) <= 0);
        assertTrue(res.getSuggestedPrice().compareTo(res.getLowBound()) >= 0);
        assertTrue(res.getSuggestedPrice().compareTo(res.getHighBound()) <= 0);
        assertTrue(res.getSuggestedPrice().compareTo(new BigDecimal("49.80")) < 0);
    }

    @Test
    void worseConditionYieldsLowerPrice() {
        BigDecimal worn = newService().suggestPrice(request("49.80", 1)).getSuggestedPrice();
        BigDecimal likeNew = newService().suggestPrice(request("49.80", 5)).getSuggestedPrice();

        assertTrue(worn.compareTo(likeNew) < 0, "成色越差建议价应越低");
    }
}
