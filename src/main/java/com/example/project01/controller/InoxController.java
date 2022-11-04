package com.example.project01.controller;

import com.example.project01.dto.CalendarDTO;
import com.example.project01.dto.EmployeeDTO;
import com.example.project01.dto.LeaveDTO;
import com.example.project01.entity.*;
import com.example.project01.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/inoxsoft")
public class InoxController {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    DeptRepository deptRepository;
    @Autowired
    AnnualRepository annualRepository;

    @Autowired
    LeavesRepository leavesRepository;
    @Autowired
    NoticeRepository noticeRepository;
    @Autowired
    ReqeustRepository reqeustRepository;

    void leaveCountCheck() throws ParseException {
        List<Member> memberList = memberRepository.findAll();
        for (int t = 1; t < memberList.size(); t++) {
            int mno = memberList.get(t).getMno();
            List<Leaves> leavesList = leavesRepository.findLeavesByMno(mno);
            Annual annual = annualRepository.findByMno(mno);
            double count = 0;
            if(leavesList.size() > 0) {
                int startDate = Integer.parseInt(annual.getStart_date());
                int endDate = Integer.parseInt(annual.getEnd_date());
                for(int l = 0; l < leavesList.size(); l++) {
                    int aDate = Integer.parseInt(leavesList.get(l).getUse_date());
                    double aCount = leavesList.get(l).getLeave_count();
                    if (startDate <= aDate && endDate >= aDate) {
                        count += aCount;
                    }
                }
            }
            double tnumber = annual.getTnumber();
            double lnumber = tnumber - count;
            annualRepository.updateUseNumber(count, lnumber, mno);
        }
    }

    public List EmployeeList (int mno) throws ParseException {
        Annual annual = annualRepository.findByMno(mno);
        List<Leaves> leavesList = leavesRepository.findLeavesByMno(mno);
        List<EmployeeDTO> employeeDTOList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date currentTime = new Date();
        String today = format.format(currentTime);
        Date todate = format.parse(today);
        int sDateYear = Integer.parseInt(annual.getStart_date().substring(0,4));
        int sDateMonth = Integer.parseInt(annual.getStart_date().substring(4,6));
        Date NewsDate =  format.parse(annual.getStart_date().substring(0,4) + annual.getStart_date().substring(4,6) + "01");
        Calendar cal = Calendar.getInstance();
        cal.setTime(NewsDate);
        int months = 0;
        while(true) {
            cal.add(Calendar.MONTH, 1);
            Date plusMonth = cal.getTime();
            months += 1;
            if(plusMonth.compareTo(todate) > 0){
                break;
            }
        }
        double tnumber = annual.getTnumber();
        double newTnumber = 0.0;
        for(int i = 0; i < months; i++) {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            String YearMonth = "";
            if(sDateMonth < 10) {
                employeeDTO.setMonth(sDateYear + "-0" + sDateMonth);
                YearMonth = sDateYear + "0" + sDateMonth;
            }else if(sDateMonth >= 10 && sDateMonth < 13) {
                employeeDTO.setMonth(sDateYear + "-" + sDateMonth);
                YearMonth = sDateYear + "" + sDateMonth;
            }else if(sDateMonth > 12) {
                sDateMonth = 1;
                sDateYear += 1;
                employeeDTO.setMonth(sDateYear + "-0" + sDateMonth);
                YearMonth = sDateYear + "0" + sDateMonth;
            }
            double unumber = 0.0;
            if(tnumber >= 15) {
                employeeDTO.setTnumber(tnumber);
            }else {
                employeeDTO.setTnumber(newTnumber);
            }
            if(leavesList.size() > 0) {
                for(int l = 0; l < leavesList.size(); l++) {
                    String leaveDates = leavesList.get(l).getUse_date();
                    if(leaveDates.contains(YearMonth)) {
                        unumber += leavesList.get(l).getLeave_count();
                    }
                }
            }
            employeeDTO.setUnumber(unumber);
            if(i == 0) {
                employeeDTO.setLnumber(employeeDTO.getTnumber() - unumber);
            }else {
                double beLnumber = employeeDTOList.get(i-1).getLnumber();
                if(tnumber == 15) {
                    employeeDTO.setLnumber(beLnumber - unumber);
                }else {
                    employeeDTO.setLnumber(beLnumber - unumber + 1);
                }
            }
            sDateMonth += 1;
            newTnumber += 1;
            employeeDTOList.add(employeeDTO);
        }
        Collections.reverse(employeeDTOList);
        return employeeDTOList;
    }

    @GetMapping("")
    String login() throws ParseException {
        return "login";
    }

    @GetMapping("/error")
    public String logout(HttpServletRequest httpServletRequest) {
      return "error";
    };

    @RequestMapping(value="/login.do" , method = RequestMethod.POST)
    @ResponseBody
    Object loginCheck
            (@RequestBody Map<String,String> loginData, Model model,
             HttpServletRequest httpServletRequest) throws Exception{
        HttpSession session = httpServletRequest.getSession();
        if(session.getAttribute("user") != null) {
            session.removeAttribute("user");
        }
        String email = loginData.get("email");
        String pw = loginData.get("pw");
        try {
            Member member = memberRepository.findMemberByEmailAndPw(email,pw);
            if(member != null) {
                if(member.getActivate() == 0) {
                    session.setAttribute("user" , member);
                    session.setAttribute("check" , true);
                    return 1;
                }else {
                    return 2;
                }
            }else {
                return 0;
            }
        } catch (Exception e) {
            return 9;
        }
    }
    @GetMapping( "/home")
    public String main(Model model,HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member)session.getAttribute("user");
        int mno = member.getMno();
        if(mno == 99) {
            int first = reqeustRepository.countAll();
            int second = reqeustRepository.countApprovalYet();
            int third = reqeustRepository.countApproval();
            model.addAttribute("first" , first);
            model.addAttribute("second" , second);
            model.addAttribute("third" , third);
        }else {
            Annual annual = annualRepository.findByMno(mno);
            double first = annual.getLnumber();
            double second = annual.getUnumber();
            double third = annual.getTnumber();
            model.addAttribute("first" , first);
            model.addAttribute("second" , second);
            model.addAttribute("third" , third);
        }
        model.addAttribute("home" , true);
        return "main";
    }

    @GetMapping( "/mypage")
    public String mypage(Model model,HttpServletRequest httpServletRequest) throws ParseException {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member)session.getAttribute("user");
        int mno = member.getMno();
        String entry_date = member.getEntry_date();
        entry_date = entry_date.substring(0,4) + "년" + entry_date.substring(4,6) + "월" + entry_date.substring(6,8)+ "일";
        Annual annual = annualRepository.findByMno(mno);
        List<Request> requestList = reqeustRepository.findByOneMnoList(mno);
        if(requestList.size() > 0) {
            int count = 3;
            if(requestList.size() < 3 ) {
                count = requestList.size();
            }
            ArrayList<Request> requestArrayList = new ArrayList<>();
            for(int i = 0; i < count; i++) {
                requestArrayList.add(requestList.get(i));
            }
            model.addAttribute("requestArrayList" , requestArrayList);
        }
        List<EmployeeDTO> employeeDTOList = EmployeeList(mno);
        if(employeeDTOList.size() > 0) {
            int count = 3;
            if(employeeDTOList.size() < 3 ) {
                count = employeeDTOList.size();
            }
            ArrayList<EmployeeDTO> employeeDTOArrayList = new ArrayList<>();
            for(int i = 0; i < count; i++) {
                employeeDTOArrayList.add(employeeDTOList.get(i));
            }
            model.addAttribute("employeeDTOArrayList" , employeeDTOArrayList);
        }
        model.addAttribute("size" , requestList.size());
        model.addAttribute("sizeDTO" , employeeDTOList.size());
        Dept dept = deptRepository.findByDno(member.getDno());
        model.addAttribute("dName" , dept.getDname());
        model.addAttribute("entry_date" , entry_date);
        model.addAttribute("mypage" , true);
        model.addAttribute("mypageHome" , true);
        return "main";
    }

    @GetMapping( "/mypage/list")
    public String mypageList(Model model,HttpServletRequest httpServletRequest) throws ParseException {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member)session.getAttribute("user");
        int mno = member.getMno();
        List<EmployeeDTO> employeeDTOList = EmployeeList(mno);
        model.addAttribute("employeeDTOList" , employeeDTOList);
        model.addAttribute("mypage" , true);
        model.addAttribute("mypageList" , true);
        return "main";
    }

    @GetMapping( "/mypage/request/list")
    public String mypageRequestList(Model model,HttpServletRequest httpServletRequest,
                                  @PageableDefault(page = 0,size=10, sort="rno", direction = Sort.Direction.DESC) Pageable pageable) {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member)session.getAttribute("user");
        int mno = member.getMno();
        Page<Request> requestList = reqeustRepository.findByOneMnoLimits(pageable,mno);
        int totalPages = requestList.getTotalPages();
        model.addAttribute("requestList" , requestList);
        model.addAttribute("totalPages" , totalPages);
        model.addAttribute("mypage" , true);
        model.addAttribute("mypageReqeust" , true);
        return "main";
    }

    @GetMapping( "/mypage/change")
    public String mypageChange(Model model,HttpServletRequest httpServletRequest) throws ParseException {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member)session.getAttribute("user");
        int mno = member.getMno();
        Dept dept = deptRepository.findByDno(member.getDno());
        model.addAttribute("mypage" , true);
        model.addAttribute("mypageChange" , true);
        return "main";
    }

    @PostMapping("/mypage/change/submit")
    public String mypageChangeSubmit(Model model,HttpServletRequest httpServletRequest,
                                     @RequestParam(value="newPw",required=false) String newPw) {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member)session.getAttribute("user");
        int mno = member.getMno();
        memberRepository.updatePw(newPw,mno);
        return "redirect:/inoxsoft/mypage";
    }

    @RequestMapping(value="/mypage/change/oldPwCheck" , method = RequestMethod.POST)
    @ResponseBody
    int oldPwCheck
            (@RequestBody Map<String,String> checkPw, Model model,
             HttpServletRequest httpServletRequest) throws Exception{
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member)session.getAttribute("user");
        String oldPw = checkPw.get("oldPw");
        try {
            if(member.getPw().equals(oldPw)) {
                return 1;
            }else {
                return 0;
            }
        } catch (Exception e) {
            return 9;
        }
    }

    @GetMapping( "/leave")
    public String leaveList(Model model,
                            @PageableDefault(page = 0,size=10, sort="rno", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Request> requestList = reqeustRepository.findAll(pageable);

        model.addAttribute("leave" , true);
        model.addAttribute("leaveList" , true);
        model.addAttribute("requestList" , requestList);

        /* paging */
        int totalPages = requestList.getTotalPages();
        int nowPage = requestList.getPageable().getPageNumber() +1;
        int endPage = (int) (Math.ceil(nowPage/10.0)*10);
        int startPage = endPage-10;

        //전체 마지막 페이지가 endPage보다 작은 경우, endPage 값 조정
        if(totalPages < endPage) {
            endPage = totalPages;
        }

        //totalPage
        model.addAttribute("totalPages" , totalPages);

        //previous & next
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());

        // now & start & end
        model.addAttribute("nowPage", nowPage); //(현재 사용 중)
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        //has (현재 사용 중)
        model.addAttribute("hasNext", requestList.hasNext());
        model.addAttribute("hasPrev", requestList.hasPrevious());

        return "main";
    }

    @GetMapping( "/leave/list/approval/yet")
    public String leaveListApprovalYet(Model model,
                            @PageableDefault(page = 0,size=10, sort="rno", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Request> requestList = reqeustRepository.findApprovalYet(pageable);

        model.addAttribute("leave" , true);
        model.addAttribute("leaveList" , true);
        model.addAttribute("requestList" , requestList);

        int totalPages = requestList.getTotalPages();
        int nowPage = requestList.getPageable().getPageNumber() +1;
        int endPage = (int) (Math.ceil(nowPage/10.0)*10);
        int startPage = endPage-10;

        //전체 마지막 페이지가 endPage보다 작은 경우, endPage 값 조정
        if(totalPages < endPage) {
            endPage = totalPages;
        }

        //totalPage
        model.addAttribute("totalPages" , totalPages);

        //previous & next
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());

        // now & start & end
        model.addAttribute("nowPage", nowPage); //(현재 사용 중)
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        //has (현재 사용 중)
        model.addAttribute("hasNext", requestList.hasNext());
        model.addAttribute("hasPrev", requestList.hasPrevious());

        return "main";
    }

    @GetMapping( "/leave/list/approval")
    public String leaveListApproval(Model model,
                                       @PageableDefault(page = 0,size=10, sort="rno", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Request> requestList = reqeustRepository.findApproval(pageable);
        int totalPages = requestList.getTotalPages();
        model.addAttribute("leave" , true);
        model.addAttribute("leaveList" , true);
        model.addAttribute("requestList" , requestList);

        int nowPage = requestList.getPageable().getPageNumber() +1;
        int endPage = (int) (Math.ceil(nowPage/10.0)*10);
        int startPage = endPage-10;

        //전체 마지막 페이지가 endPage보다 작은 경우, endPage 값 조정
        if(totalPages < endPage) {
            endPage = totalPages;
        }

        //totalPage
        model.addAttribute("totalPages" , totalPages);

        //previous & next
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());

        // now & start & end
        model.addAttribute("nowPage", nowPage); //(현재 사용 중)
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        //has (현재 사용 중)
        model.addAttribute("hasNext", requestList.hasNext());
        model.addAttribute("hasPrev", requestList.hasPrevious());

        return "main";
    }

    @GetMapping( "/leave/list/read")
    public String leaveListRead(Model model,HttpServletRequest httpServletRequest) {
        int rno = Integer.parseInt(httpServletRequest.getParameter("rno"));
        Request request = reqeustRepository.findByRno(rno);
        String leaveDate = request.getLeave_date();
        double count = request.getLeave_count();
        int count1 = 1;
        if(leaveDate.contains("/")) {
            String [] leaveArr = leaveDate.split("/");
            count1 = leaveArr.length-1;
            leaveArr[0] = leaveArr[0].substring(0,4) + "년 " + leaveArr[0].substring(4,6) + "월 " + leaveArr[0].substring(6,8) + "일 ";
            leaveArr[count1] = leaveArr[count1].substring(0,4) + "년 " + leaveArr[count1].substring(4,6) + "월 " + leaveArr[count1].substring(6,8) + "일 ";
            leaveDate = leaveArr[0] + " ~ " + leaveArr[count1];
            model.addAttribute("leaveDate",leaveDate);
        }else {
            leaveDate = leaveDate.substring(0,4) + "년 " + leaveDate.substring(4,6) + "월 " + leaveDate.substring(6,8) + "일";
            model.addAttribute("leaveDate",leaveDate);
        }
        String leaveType = "연차";
        if(request.getType().contains("half")) {
            if(request.getType().contains("after")) {
                leaveType = "오후반차";
            }else {
                leaveType = "오전반차";
            }
        }else if(request.getType().contains("mon")) {
            leaveType = "월차";
        } else {
            if(request.getType().contains("official")) {
                leaveType = "공가";
            }else if(request.getType().contains("sub")) {
                leaveType = "대체휴가";
            }
        }
        model.addAttribute("leaveType" , leaveType);
        model.addAttribute("count" , count);
        model.addAttribute("aRequest",request);
        model.addAttribute("leave" , true);
        model.addAttribute("leaveRequestRead" , true);
        return "main";
    }

    @GetMapping( "/leave/list/read/no")
    public String leaveListReadNo(Model model, HttpServletRequest httpServletRequest) throws ParseException {
        int rno = Integer.parseInt(httpServletRequest.getParameter("rno"));
        reqeustRepository.updateNo(rno);
        Request request = reqeustRepository.findByRno(rno);
        List<Leaves> leavesList = leavesRepository.findLeavesByRno(rno);
        for(int l = 0; l < leavesList.size(); l++) {
            leavesRepository.delete(leavesList.get(l));
        }
        leaveCountCheck();
        return "redirect:/inoxsoft/leave/list/read?rno=" + rno;
    }

    @GetMapping( "/leave/list/read/yes")
    public String leaveListReadYes(Model model,HttpServletRequest httpServletRequest) throws ParseException {
        int rno = Integer.parseInt(httpServletRequest.getParameter("rno"));
        reqeustRepository.updateYes(rno);
        Request request = reqeustRepository.findByRno(rno);
        String requestDates = request.getLeave_date();
        String type = request.getType();
        double count = 0.0;
        if(type.contains("day_off")) {
            count = 1.0;
        }else if(type.contains("half")) {
            count = 0.5;
        }else if(type.contains("mon")) {
            count = 1.0;
        }
        if(requestDates.contains("/")) {
            String[] requestDatesList = requestDates.split("/");
            for(int i = 0; i < requestDatesList.length; i++) {
                Leaves leaves = new Leaves(rno,request.getMno(),requestDatesList[i],type,count);
                leavesRepository.save(leaves);
            }
        }else {
            Leaves leaves = new Leaves(rno,request.getMno(),request.getLeave_date(),request.getType(),request.getLeave_count());
            leavesRepository.save(leaves);
        }
        leaveCountCheck();
        return "redirect:/inoxsoft/leave/list/read?rno=" + rno;
    }

    @GetMapping( "/leave/list/read/delete")
    public String leaveListReadDelete(Model model,HttpServletRequest httpServletRequest) {
        int rno = Integer.parseInt(httpServletRequest.getParameter("rno"));
        reqeustRepository.deleteById(rno);
        return "redirect:/inoxsoft/leave";
    }

    @RequestMapping( "/leave/request/submit")
    public String leaveRequestSubmit(Model model,HttpServletRequest httpServletRequest,
                                     @RequestParam(value="dates",required=false) String dates,
                                     @RequestParam(value="leave_count",required=false) double leave_count,
                                     @RequestParam(value="type",required=false) String type,
                                     @RequestParam(value="reason",required=false) String reason) {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member) session.getAttribute("user");
        int dno = member.getDno();
        String writer = member.getName();
        if(dno == 0) {
            writer += "[경영지원본부]";
        }else if(dno == 1) {
            writer += "[GxP사업본부]";
        }else if(dno == 2) {
            writer += "[ICT사업본부]";
        }

        String typeKo = "";
        if(type.equals("day_off")) {
            typeKo = "연차";
        }else if(type.equals("half_day")) {
            typeKo = "오전반차";
            leave_count /= 2;
        }else if(type.equals("half_after")) {
            typeKo = "오후반차";
            leave_count /= 2;
        }else if(type.equals("official")) {
            typeKo = "공가";
            leave_count = 0;
        }else if(type.equals("mon_off")) {
            typeKo = "월차";
        } else if(type.equals("sub_off")) {
            typeKo = "대체휴가";
            leave_count = 0;
        }
        String title = "[휴가원] " + member.getName() + " " + typeKo + " " + "신청서";
        Request request = new Request(member.getMno(),title,writer,leave_count,type,reason,dates);
        reqeustRepository.save(request);
        return "redirect:/inoxsoft/leave";
    }

    @GetMapping( "/leave/request")
    public String leaveReqeust(Model model,HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member) session.getAttribute("user");
        Annual annual = annualRepository.findByMno(member.getMno());
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = now.format(formatter);
        model.addAttribute("today" , today);
        model.addAttribute("annual" , annual);
        model.addAttribute("leave" , true);
        model.addAttribute("leaveRequest" , true);
        return "main";
    }

    @RequestMapping(value = "/leave/calendar", method = RequestMethod.POST)
    @ResponseBody
    Object leaveCalendar(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        Member memberUser = (Member) session.getAttribute("user");
/*        List<Annual> annualList = new ArrayList<>();*/
        List<CalendarDTO> calendarList = new ArrayList<>();
        List<Leaves> leavesListAll = leavesRepository.findAll();
        List<Leaves> leavesList = new ArrayList<>();
        if(memberUser.getDno() != 0) {
            int dnoUser = memberUser.getDno();
            List<Member> memberList = memberRepository.findAll();
            for(int i = 0; i < memberList.size(); i++) {
                Member memberOne = memberList.get(i);
                int mnoOne = memberOne.getMno();
                int dnoOne = memberOne.getDno();
                if(dnoUser == dnoOne) {
                    for(int l = 0; l < leavesListAll.size(); l++) {
                        if(leavesListAll.get(l).getMno() == mnoOne) {
                            leavesList.add(leavesListAll.get(l));
                        }
                    }
                }
            }
        }else {
            leavesList = leavesListAll;
        }
        for(int i = 0; i < leavesList.size(); i++) {
            Leaves leaves = leavesList.get(i);
            Member member = memberRepository.findNameByMno(leaves.getMno());
            String rgbColor = "";
            if(member.getDno() == 0) {
                rgbColor = "rgb(253,124,21)";
            }else if(member.getDno() == 1) {
                rgbColor = "rgb(32,66,208)";
            }else if(member.getDno() == 2) {
                rgbColor = "rgb(47,194,13)";
            }
            CalendarDTO calendarDTO = new CalendarDTO();
            calendarDTO.setStart(leaves.getUse_date());
            if(leaves.getType().equals("day_off")) {
                calendarDTO.setTitle(member.getName() + "[연차]");
            }else if(leaves.getType().equals("mon_off")) {
                calendarDTO.setTitle(member.getName() + "[월차]");
            }else if(leaves.getType().equals("half_day")) {
                calendarDTO.setTitle(member.getName() + "[오전반차]");
            }else if(leaves.getType().equals("half_after")) {
                calendarDTO.setTitle(member.getName() + "[오후반차]");
            }else if(leaves.getType().equals("official")) {
                calendarDTO.setTitle(member.getName() + "[공가]");
            }else if(leaves.getType().equals("sub_off")) {
                calendarDTO.setTitle(member.getName() + "[대체휴가]");
            }
            calendarDTO.setBackgroundColor(rgbColor);
            calendarDTO.setBorderColor(rgbColor);
            calendarList.add(calendarDTO);
        }
        return calendarList;
    }

    @GetMapping( "/notice")
    public String notice(Model model,
                         @PageableDefault(page = 0, size=10, sort="bno", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Notice> noticeList = noticeRepository.findAll(pageable);

        model.addAttribute("notice" , true);
        model.addAttribute("noticeHome" , true);
        model.addAttribute("noticeList", noticeList);


        /* paging */
        int totalPages = noticeList.getTotalPages();
        int nowPage = noticeList.getPageable().getPageNumber() +1;
        int endPage = (int) (Math.ceil(nowPage/10.0)*10);
        int startPage = endPage-10;

        //전체 마지막 페이지가 endPage보다 작은 경우, endPage 값 조정
        if(totalPages < endPage) {
            endPage = totalPages;
        }

        //totalPage
        model.addAttribute("totalPages" , totalPages);

        //previous & next
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());

        // now & start & end
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        //has (현재 이부분만 사용)
        model.addAttribute("hasNext", noticeList.hasNext());
        model.addAttribute("hasPrev", noticeList.hasPrevious());

        return "main";
    }

    @GetMapping( "/notice/write")
    public String noticeWrite(Model model) {
        model.addAttribute("notice" , true);
        model.addAttribute("noticeWrite" , true);
        return "main";
    }

    @RequestMapping(value = "/notice/register", method = RequestMethod.POST)
    public String noticeRegister(Model model,HttpServletRequest httpServletRequest,
                                 @RequestParam(value="title",required=false) String title,
                                 @RequestParam(value="content",required=false) String content) {
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member) session.getAttribute("user");
        Notice notice =  new Notice(member.getMno(),member.getName(),title,content);
        noticeRepository.save(notice);
        model.addAttribute("notice" , true);
        return "redirect:/inoxsoft/notice";
    }

    @GetMapping("/notice/read")
    public String noticeRead(Model model,HttpServletRequest httpServletRequest) {
        int bno = Integer.parseInt(httpServletRequest.getParameter("bno"));
        noticeRepository.updateViews(bno);
        Notice notice = noticeRepository.findByBno(bno);
        HttpSession session = httpServletRequest.getSession();
        Member member = (Member) session.getAttribute("user");
        model.addAttribute("notice" , true);
        model.addAttribute("aNotice",notice);
        model.addAttribute("noticeRead" , true);
        return "main";
    }

    @GetMapping("/notice/delete")
    public String noticeDelete(Model model,HttpServletRequest httpServletRequest) {
        int bno = Integer.parseInt(httpServletRequest.getParameter("bno"));
        noticeRepository.deleteByBno(bno);
        return "redirect:/inoxsoft/notice";
    }

    @GetMapping("/notice/modify")
    public String noticeModify(Model model,HttpServletRequest httpServletRequest){
        int bno = Integer.parseInt(httpServletRequest.getParameter("bno"));
        Notice notice = noticeRepository.findByBno(bno);
        model.addAttribute("notice" , true);
        model.addAttribute("aNotice",notice);
        model.addAttribute("noticeModify" , true);
        return "main";
    }

    @RequestMapping(value = "/notice/modify/submit", method = RequestMethod.POST)
    public String noticeModifySubimit(Model model,HttpServletRequest httpServletRequest,
                                 @RequestParam(value="bno",required=false) int bno,
                                 @RequestParam(value="title",required=false) String title,
                                 @RequestParam(value="content",required=false) String content) {
        noticeRepository.updateNotice(bno,title,content);
        noticeRepository.updateTime(bno);
        Notice notice = noticeRepository.findByBno(bno);
        model.addAttribute("notice" , true);
        model.addAttribute("aNotice",notice);
        model.addAttribute("noticeRead" , true);
        return "redirect:/inoxsoft/notice/read?bno=" + bno;
    }

    @GetMapping( "/employee")
    public String employee(Model model,
                            @RequestParam(value="dno",required=false) int dno) {
        List<Annual> annualList = new ArrayList<>();
        List<LeaveDTO> leaveDTOList = new ArrayList<>();
        if(dno != 3) {
            List<Member> memberList = memberRepository.findMemberByDno(dno);
            for(int i = 0; i < memberList.size(); i++) {
                Member member = memberList.get(i);
                if(member.getMno() != 99) {
                    Annual annual = annualRepository.findByMno(member.getMno());
                    annualList.add(annual);
                }
            }
        }else {
            annualList = annualRepository.findAll();
        }
        for(int i = 0; i < annualList.size(); i++) {
            Annual annual = annualList.get(i);
            Member member = memberRepository.findNameByMno(annual.getMno());
            LeaveDTO leaveDTO = new LeaveDTO();
            leaveDTO.setMno(member.getMno());
            leaveDTO.setName(member.getName());
            leaveDTO.setLnumber(annual.getLnumber());
            leaveDTO.setTnumber(annual.getTnumber());
            leaveDTO.setUnumber(annual.getUnumber());
            String sDate = annual.getStart_date();
            String eDate = annual.getEnd_date();
            String period = "";
            period = sDate.substring(0,4) + "-" + sDate.substring(4,6) + "-" + sDate.substring(6,8) + " ~ ";
            period += eDate.substring(0,4) + "-" + eDate.substring(4,6) + "-" + eDate.substring(6,8);
            leaveDTO.setPeriod(period);
            leaveDTO.setActivate(member.getActivate());
            leaveDTOList.add(leaveDTO);
        }
        model.addAttribute("leaveDTOList" , leaveDTOList);
        model.addAttribute("employee" , true);
        model.addAttribute("employeeHome" , true);
        return "main";
    }

    @GetMapping( "/employee/activate")
    public String employeeActivate(@RequestParam(value="mno")int mno ,
                                   @RequestParam(value="activate")int activate) {
        memberRepository.updateActivate(activate,mno);
        return "redirect:/inoxsoft/employee?dno=3";
    }

    @GetMapping("/employee/details")
    public String employeeDetails(Model model,
                                 @RequestParam(value="mno",required=false) int mno) throws ParseException {
        Member member = memberRepository.findNameByMno(mno);
        List<EmployeeDTO> employeeDTOList = EmployeeList(mno);
        model.addAttribute("employeeDTOList" , employeeDTOList);
        model.addAttribute("name" , member.getName());
        model.addAttribute("employee" , true);
        model.addAttribute("employeeDetails" , true);
        return "main";
    }

    @GetMapping("/employee/register")
    public String employeeRegister(Model model,
                                  @RequestParam(value="dno",required=false) int dno) {
        model.addAttribute("dno" , dno);
        model.addAttribute("employee" , true);
        model.addAttribute("employeeRegister" , true);
        return "main";
    }

    @RequestMapping(value="/employee/register/submit",method = RequestMethod.POST)
    public String employeeRegisterSubmit(Model model,
                                         @RequestParam(value="email",required=false) String email,
                                         @RequestParam(value="name",required=false) String name,
                                         @RequestParam(value="entry_date",required=false) String entry_date,
                                         @RequestParam(value="dno",required=false) int dno) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date entryDate = new Date(entry_date);
        entry_date = format.format(entryDate);
        Member member = new Member(email,name,entry_date,dno);
        memberRepository.save(member);
        int newMno = memberRepository.findMemberByMaxMno();
        Calendar cal = Calendar.getInstance();
        cal.setTime(entryDate);
        cal.add(Calendar.YEAR, 1);
        Date oneYear = cal.getTime();
        cal.setTime(oneYear);
        cal.add(Calendar.DATE, -1);
        Date endDate = cal.getTime();
        String end_date = format.format(endDate);
        Annual annual = new Annual(newMno,entry_date,end_date);
        annualRepository.save(annual);
        return  "redirect:/inoxsoft/employee?dno=" + dno;
    }
}
