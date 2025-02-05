package com.kcs.soup.api.search.controller;

import com.kcs.soup.api.search.document.Product;
import com.kcs.soup.api.search.dto.RecommendResponse;
import com.kcs.soup.api.search.dto.SearchResponse;
import com.kcs.soup.api.search.service.CollectionService;
import com.kcs.soup.api.search.service.SearchService;
import com.kcs.soup.common.dto.BaseResponse;
import com.kcs.soup.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequestMapping("/search")
@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    private final JwtTokenProvider jwtTokenProvider;

    private final CollectionService collectionService;

    @GetMapping("")
    public ResponseEntity<BaseResponse> searchQuery(
            @RequestParam(name = "q") String prdname,
            @PageableDefault(size = 10, sort = "purchase", direction = Sort.Direction.DESC) Pageable pageable) {

        boolean isUserBest = searchService.isUserLogin();
        Long memberIDX;
        Page<Product> result;
        if (isUserBest) {
            memberIDX = jwtTokenProvider.getMemberIdx();
            result = searchService.getProductPage(prdname, pageable, memberIDX);
        } else {
            result = searchService.getProductPage(prdname, pageable, null);
        }
        return ResponseEntity.ok(new BaseResponse(200, "성공", new SearchResponse(prdname, result)));
    }

    @GetMapping("/collections/user-best")
    public ResponseEntity<BaseResponse> getRecommendItemList() {
        List<Product> productList = searchService.getRecommendItemByMemberid(jwtTokenProvider.getMemberIdx());
        return ResponseEntity.ok(new BaseResponse(200, "성공", new RecommendResponse("recommend", productList)));
    }

    @GetMapping("/maincat")
    public ResponseEntity<BaseResponse> searchByMaincat(@RequestParam(name = "category") String maincat,
                                                        @PageableDefault(size = 10, sort = "purchase", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Product> result = collectionService.searchByMaincat(maincat, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "성공", new SearchResponse(maincat, result)));
    }

    @GetMapping("/subcat")
    public ResponseEntity<BaseResponse> searchBySubcat(@RequestParam(name = "category") String subcat,
                                                       @PageableDefault(size = 10, sort = "purchase", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Product> result = collectionService.searchBySubcat(subcat, pageable);
        return ResponseEntity.ok(new BaseResponse(200, "성공", new SearchResponse(subcat, result)));
    }

    @GetMapping("/collections/{themeIdx}")
    public ResponseEntity<BaseResponse> searchTheme(@PathVariable("themeIdx") Long themeIdx,
                                                    @PageableDefault(size = 10, sort = "score", direction = Sort.Direction.DESC) Pageable defaultPageable) {
        String themeTitle = collectionService.findTitleByIdx(themeIdx);
        List<String> catList = collectionService.findByThemeIdx(themeIdx);
        List<Product> prdList = new ArrayList<>();
        for (String category : catList) {
            prdList.addAll(collectionService.searchAllBySubcat(category));
        }
        Collections.sort(prdList, Collections.reverseOrder());
        final int start = (int) defaultPageable.getOffset();
        final int end = Math.min((start + defaultPageable.getPageSize()), prdList.size());
        final Page<Product> result = new PageImpl<>(prdList.subList(start, end), defaultPageable, prdList.size());
        return ResponseEntity.ok(new BaseResponse(200, "성공", new SearchResponse(themeTitle, result)));
    }

    @GetMapping("/collections/today-best")
    public ResponseEntity<BaseResponse> searchTodayBest() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("score").descending());
        List<Product> result = collectionService.findAll(pageable);
        return ResponseEntity.ok(new BaseResponse(200, "성공", result));
    }

}
