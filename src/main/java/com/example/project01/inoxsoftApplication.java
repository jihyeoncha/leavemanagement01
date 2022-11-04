package com.example.project01;

import com.example.project01.entity.Annual;
import com.example.project01.entity.Leaves;
import com.example.project01.entity.Member;
import com.example.project01.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class inoxsoftApplication {


	@Autowired
	MemberRepository memberRepository;
	@Autowired
	DeptRepository deptRepository;
	@Autowired
	AnnualRepository annualRepository;
	@Autowired
	NoticeRepository noticeRepository;
	@Autowired
	ReqeustRepository reqeustRepository;
	@Autowired
	LeavesRepository leavesRepository;


	public static void main(String[] args) {
		SpringApplication.run(inoxsoftApplication.class, args);

	}

	@Scheduled(cron = "0 26 14 * * *") // 초 분 시 일 월 주 (년)
	void leaveCountCheck() throws ParseException {
		System.out.println("================스케줄 시작합니다================");
		List<Member> memberList = memberRepository.findAll();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date currentTime = new Date();
		String today = format.format(currentTime);
		Date todate = format.parse(today);
		for (int t = 1; t < memberList.size(); t++) {
			int mno = memberList.get(t).getMno();
			double count = 0;
			Annual annual = annualRepository.findByMno(mno);
			Calendar cal = Calendar.getInstance();
			Date entryDate = format.parse(memberList.get(t).getEntry_date());
			cal.setTime(entryDate);
			cal.add(Calendar.YEAR, 1);
			Date oneYear = cal.getTime();
			int compareEntry = oneYear.compareTo(todate);
			if (compareEntry > 0) {
				cal.setTime(entryDate);
				double newLeave = 0.0;
				while (true) {
					cal.add(Calendar.MONTH, 1);
					Date testDate = cal.getTime();
					int compareTest = testDate.compareTo(todate);
					if (compareTest > 0) {
						break;
					}
					newLeave += 1;
				}
				annualRepository.updateMonthLeave(newLeave, mno);
			} else {
				Date compareDate = format.parse(memberList.get(t).getEntry_date());
				int year = 1;
				double tnumber = 15.0;
				while (true) {
					cal.setTime(compareDate);
					cal.add(Calendar.YEAR, 1);
					Date compareYear = cal.getTime();
					int com = todate.compareTo(compareYear);
					if (com > 0) {
						year += 1;
						compareDate = compareYear;
					} else {
						break;
					}
				}
				while (true) {
					year -= 2;
					if (year > 0) {
						tnumber += 1;
					} else {
						break;
					}
				}
				Date sDate = format.parse(annual.getStart_date());
				Date eDate = format.parse(annual.getEnd_date());
				int compareS = sDate.compareTo(todate);
				int compareE = eDate.compareTo(todate);
				if (!(compareS == -1 && compareE == 1)) {
					cal.setTime(sDate);
					cal.add(Calendar.YEAR, 1);
					String newsDate = format.format(cal.getTime());
					cal.setTime(eDate);
					cal.add(Calendar.YEAR, 1);
					String neweDate = format.format(cal.getTime());
					annualRepository.updateStartAndEndDate(newsDate, neweDate, tnumber, mno);
				}
			}
			List<Leaves> leavesList = leavesRepository.findLeavesByMno(mno);
			annual = annualRepository.findByMno(mno);
			int startDate = Integer.parseInt(annual.getStart_date());
			int endDate = Integer.parseInt(annual.getEnd_date());
			for(int l = 0; l < leavesList.size(); l++) {
				String useDate = leavesList.get(l).getUse_date();
				int aDate = Integer.parseInt(useDate.substring(0, 8));
				if (startDate <= aDate && endDate >= aDate) {
					double aCount = leavesList.get(l).getLeave_count();
					count += aCount;
				}
			}
			double tnumber = annual.getTnumber();
			double lnumber = tnumber - count;
			annualRepository.updateUseNumber(count, lnumber, mno);
		}
		System.out.println("================스케줄 종료합니다================");
	}
}