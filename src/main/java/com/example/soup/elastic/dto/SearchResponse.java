package com.example.soup.elastic.dto;

import com.example.soup.domain.entity.elasticsearch.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class SearchResponse {
    private String prdname;
    private Page<Product> result;
}
