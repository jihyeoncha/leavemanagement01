package com.example.project01.repositories;

import com.example.project01.entity.Notice;
import com.example.project01.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ReqeustRepository extends JpaRepository<Request,Integer> {
    Request findByRno(int rno);

    @Query("SELECT COUNT(rno) FROM Request")
    int countAll();

    @Query("SELECT COUNT(rno) FROM Request WHERE approval = '0'")
    int countApprovalYet();

    @Query("SELECT COUNT(rno) FROM Request WHERE approval = '1' OR approval = '2'")
    int countApproval();

    @Query("SELECT r FROM Request r WHERE r.approval = '0'")
    Page<Request> findApprovalYet(Pageable pageable);

    @Query("SELECT r FROM Request r WHERE r.mno = :mno")
    Page<Request> findByOneMnoLimits(Pageable pageable, @Param(value="mno")int mno);

    @Query("SELECT r FROM Request r WHERE r.mno = :mno order by r.rno desc")
    List<Request> findByOneMnoList(@Param(value="mno")int mno);

    @Query("SELECT r FROM Request r WHERE r.approval = '1' OR r.approval = '2'")
    Page<Request> findApproval(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Request SET approval = '1' WHERE rno = :rno")
    void updateYes(@Param(value="rno")int rno);

    @Transactional
    @Modifying
    @Query("UPDATE Request SET approval = '2' WHERE rno = :rno")
    void updateNo(@Param(value="rno")int rno);

    @Transactional
    void deleteByRno(int rno);




}