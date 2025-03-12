package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.repository.entity.Action;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {
}