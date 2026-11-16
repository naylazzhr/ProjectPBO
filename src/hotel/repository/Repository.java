package com.grandluxury.repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    void add(T entity) throws IOException;

    void update(T entity) throws IOException;

    void delete(ID id) throws IOException;

    Optional<T> findById(ID id);

    List<T> findAll();

    void save() throws IOException;

    void load() throws IOException;
}