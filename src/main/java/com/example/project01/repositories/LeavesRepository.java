package com.example.project01.repositories;

import com.example.project01.entity.Leaves;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface LeavesRepository extends JpaRepository<Leaves,Integer> {
    List<Leaves> findLeavesByMno(@Param(value = "mno")int mno);
    List<Leaves> findLeavesByRno(@Param(value = "rno")int rno);
}
