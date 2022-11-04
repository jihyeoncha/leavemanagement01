package com.example.project01.repositories;

import com.example.project01.entity.Annual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface AnnualRepository extends JpaRepository<Annual,Integer> {
    Annual findByMno(int mno);

    @Transactional
    @Modifying
    @Query("UPDATE Annual SET unumber = :unumber , lnumber = :lnumber WHERE mno = :mno")
    void updateUseNumber(@Param(value="unumber")double unumber, @Param(value="lnumber")double lnumber, @Param(value="mno")int mno);

    @Transactional
    @Modifying
    @Query("UPDATE Annual SET start_date = :start_date , end_date = :end_date, tnumber = :tnumber ,lnumber = :tnumber WHERE mno = :mno")
    void updateStartAndEndDate(@Param(value="start_date")String start_date, @Param(value="end_date")String end_date, @Param(value="tnumber")double tnumber, @Param(value="mno")int mno);

    @Transactional
    @Modifying
    @Query("UPDATE Annual SET tnumber = :tnumber ,lnumber = :tnumber WHERE mno = :mno")
    void updateMonthLeave(@Param(value="tnumber")double tnumber, @Param(value="mno")int mno);

}
