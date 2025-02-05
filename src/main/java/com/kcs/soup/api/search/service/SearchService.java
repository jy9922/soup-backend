package com.kcs.soup.api.search.service;


import com.kcs.soup.api.search.document.KeywordLog;
import com.kcs.soup.api.search.document.Product;
import com.kcs.soup.api.search.repository.KeywordRepository;
import com.kcs.soup.api.search.repository.ProductRepository;
import com.kcs.soup.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SearchService {

    private final JwtTokenProvider jwtTokenProvider;
    private final ProductRepository productRepository;
    private final KeywordRepository keywordRepository;


    public Page<Product> getProductPage(String prdname, Pageable pageable, Long memberidx) {
        if (memberidx != null) {
            KeywordLog keywordObject = keywordRepository.findByMemberidxAndKeyword(memberidx, prdname);
            saveKeywordLog(prdname, memberidx, keywordObject);
        }

        return productRepository.findByPrdName(prdname, pageable);
    }

    public List<Product> getRecommendItemByMemberid(Long memberidx) {
        Sort sort = sortByCount();
        List<KeywordLog> logList = keywordRepository.findTop10ByMemberidx(memberidx, sort);
        List<KeywordLog> logListHaveSubcat = getKeywordLogHaveSubcat(logList);
        if (logListHaveSubcat.size() == 0) {
            return getRecommendWithoutLogs();
        }

        String[] keyList = new String[logListHaveSubcat.size()];
        int[] countList = new int[logListHaveSubcat.size()];
        for (int i = 0; i < logListHaveSubcat.size(); i++) {
            keyList[i] = logListHaveSubcat.get(i).getKeyword();
            countList[i] = logListHaveSubcat.get(i).getCount();
        }

        List<Integer> weigthList = getWeight(keyList, countList);
        List<String> subCatList = getSubcat(keyList);
        HashMap<String, Integer> data = new HashMap<>();
        for (int i = 0; i < weigthList.size(); i++) {

            if (data.containsKey(subCatList.get(i))) {
                data.put(subCatList.get(i), data.get(subCatList.get(i)) + weigthList.get(i));
            } else {
                data.put(subCatList.get(i), weigthList.get(i));
            }
        }

        List<Product> productList = getProductList(data);
        return productList;
    }

    private List<Product> getRecommendWithoutLogs() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("score").descending());
        return productRepository.findAll(pageable).toList();
    }

    private List<KeywordLog> getKeywordLogHaveSubcat(List<KeywordLog> logList) {
        List<KeywordLog> keyListHaveSubcat = new ArrayList<>();
        for (KeywordLog key : logList) {
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "purchase"));
            List<Product> productList = productRepository.findByPrdName(key.getKeyword(), pageable).get().collect(Collectors.toList());
            if (productList.size() > 0) {
                keyListHaveSubcat.add(key);
            }
        }
        if (keyListHaveSubcat.size() > 3) {
            keyListHaveSubcat = new ArrayList<>(keyListHaveSubcat.subList(0, 3));
        }
        return keyListHaveSubcat;
    }

    private List<Product> getProductList(HashMap<String, Integer> data) {
        List<Product> productList = new ArrayList<>();
        data.forEach((k, v) -> {
            Pageable pageable = PageRequest.of(0, v, Sort.by(Sort.Direction.DESC, "purchase"));
            Page<Product> products = productRepository.findBySubcat(k, pageable);
            for (Product product : products) {
                productList.add(product);
            }
        });

        return productList;
    }

    private List<String> getSubcat(String[] keyList) {
        List<String> subCatList = new ArrayList<>();
        for (String key : keyList) {
            Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "purchase"));
            List<Product> productList = productRepository.findByPrdName(key, pageable).get().collect(Collectors.toList());
            subCatList.add(productList.get(0).getSubcat());
        }
        return subCatList;
    }

    private List<Integer> getWeight(String[] keyList, int[] countList) {
        Integer tmp = Arrays.stream(countList).sum();
        List<Integer> weightList;
        Integer cnt = 0;
        while (true) {
            Integer re = 0;
            weightList = new ArrayList<>();
            for (int i : countList) {
                re += Integer.valueOf(i / tmp);
                weightList.add(Integer.valueOf(i / tmp));
            }
            if (re == 10) {
                break;
            } else {
                countList[cnt % countList.length] += 1;
            }
            cnt += 1;
        }
        return weightList;
    }

    private Sort sortByCount() {
        return Sort.by(Sort.Direction.DESC, "count");
    }

    private void saveKeywordLog(String prdname, Long memberidx, KeywordLog keywordObject) {
        if (keywordObject != null) {
            keywordObject.setCount(keywordObject.getCount() + 1);
            keywordRepository.save(keywordObject);
        } else {
            KeywordLog keywordLog = KeywordLog.builder()
                    .keyword(prdname)
                    .count(1).memberidx(memberidx)
                    .build();
            keywordRepository.save(keywordLog);
        }
    }

    public boolean isUserLogin() {
        Long memberIdx = jwtTokenProvider.getMemberIdxIfLogined();
        return memberIdx != null;
    }

    public boolean isUserDataExist() {
        Long memberIdx = jwtTokenProvider.getMemberIdxIfLogined();
        return keywordRepository.existsKeywordLogByMemberidx(memberIdx);
    }

}
