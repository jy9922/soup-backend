package com.kcs.soup.api.search.repository;

import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface BaseElasticSearchRepository<T> {

    <S extends T> S save(S entity, IndexCoordinates indexName);

    <S extends T> Iterable<S> saveAll(Iterable<S> entities, IndexCoordinates indexName);

    boolean setAlias(IndexCoordinates indexNameWrapper, IndexCoordinates aliasNameWrapper);

    Set<String> findIndexNamesByAlias(IndexCoordinates aliasNameWrapper);

    boolean deleteIndex(IndexCoordinates indexNameWrapper);

}