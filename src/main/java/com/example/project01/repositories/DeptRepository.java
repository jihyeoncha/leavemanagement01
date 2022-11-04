package com.example.project01.repositories;

import com.example.project01.entity.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeptRepository extends JpaRepository<Dept,Integer> {
    Dept findByDno(int dno);
}

