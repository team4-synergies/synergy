package com.synergies.synergy.controller;

import com.synergies.synergy.domain.dto.AssignmentResponseDto;
import com.synergies.synergy.domain.dto.NotificationDto;
import com.synergies.synergy.domain.dto.TodoDeleteRequestDto;
import com.synergies.synergy.domain.dto.TodoDto;
import com.synergies.synergy.domain.vo.LoginUserInfoVo;
import com.synergies.synergy.service.AssignmentService;
import com.synergies.synergy.service.NotificationService;
import com.synergies.synergy.service.TodoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/student")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AssignmentService assignmentService;

    private List<TodoDto> changeDateFormat(List<TodoDto> list) throws ParseException {
        String[] date;
        Calendar getToday = Calendar.getInstance();
        getToday.setTime(new Date());
        long diffSec;
        long diffDays;
        Calendar cmpDate;

        for (TodoDto vo : list) {
            date = vo.getEndDate().split(" ");
            Date end = new SimpleDateFormat("yyyy-MM-dd").parse(date[0]);
            cmpDate = Calendar.getInstance();
            cmpDate.setTime(end); //특정 일자

            diffSec = (cmpDate.getTimeInMillis() - getToday.getTimeInMillis()) / 1000;
            diffDays = diffSec / (24 * 60 * 60) + 1; //일자수 차이

            if (diffDays < 0) {
                vo.setEndDate("기간 만료 " + diffDays + "|" + vo.getEndDate());
            } else if (diffDays == 0) {
                vo.setEndDate("D-day" + "|" + vo.getEndDate());
            } else {
                vo.setEndDate("D-day: " + diffDays + "|" + vo.getEndDate());
            }
        }

        return list;
    }

    @GetMapping("/home")
    public String getAll(Model model, HttpSession session) throws ParseException {
        log.info("서비스 접속 시 loginUserInfo null 확인 : " + !((LoginUserInfoVo) session.getAttribute("loginUserInfo")).getId().equals(null));

        List<TodoDto> todoList = changeDateFormat(todoService.readAllTodo(
                ((LoginUserInfoVo) session.getAttribute("loginUserInfo")).getId()));
        log.info("서비스 접속 시 ToDoList null 확인 : " + !todoList.isEmpty());

        List<NotificationDto> notiList = notificationService.readNotificationList();
        log.info("서비스 접속 시 공지 사항 null 확인 : " + !todoList.isEmpty());

        AssignmentResponseDto.AssignmentContent assignment = assignmentService.readAssignmentRecentDetails();
        log.info("서비스 접속 시 과제 null 확인 : " + !(assignment == null));

        if (assignment == null) {
            model.addAttribute("assignId", 0);
        } else {
            model.addAttribute("assignId", assignment.getId());
        }


        model.addAttribute("todo", new TodoDto());
        model.addAttribute("notiList", notiList);

        List<AssignmentResponseDto.AssignmentDetail> assignmentToday = assignmentService.readTodayAssignment();

        model.addAttribute("sig", true);
        if (assignmentToday.size() == 0) {
            model.addAttribute("sig", false);
        }

        if (notiList.isEmpty()) {
            model.addAttribute("notiList", null);
        }

        if (todoList.isEmpty()) {
            model.addAttribute("todoList", null);
            return "pages/student/studentMain";
        }

        model.addAttribute("todoList", todoList);
        return "pages/student/studentMain";
    }

    @PostMapping("/todo/insert")
    public String todoInsert(@ModelAttribute("todo") TodoDto todo, HttpSession session) {
        if (todo.getContent().isBlank() || todo.getEndDate().isBlank()) {
            return "redirect:/home";
        }
        Date curDate = new Date();
        todo.setRegDate(curDate);
        todo.setRefUserId(((LoginUserInfoVo) session.getAttribute("loginUserInfo")).getId());
        todoService.createTodo(todo);
        log.info("Todo insert 성공 + Todo 데이터 : " + todo);
        return "redirect:/student/home";
    }


    @PostMapping("/todo/update/{id}")
    public String todoUpdate(@PathVariable int id, @ModelAttribute("todo") TodoDto todo,
                             HttpSession session) {
        if (todo.getContent().isBlank() || todo.getEndDate().isBlank()) {
            return "redirect:/student/home";
        }
        todo.setId(id);
        todo.setRefUserId(((LoginUserInfoVo) session.getAttribute("loginUserInfo")).getId());
        todoService.updateTodo(todo);
        log.info("Todo update 성공 + Todo 데이터 : " + todo);
        return "redirect:/student/home";
    }

    @PostMapping("/todo/delete")
    public String todoDelete(int id, HttpSession session, RedirectAttributes redirectAttributes) {
        todoService.deleteTodo(new TodoDeleteRequestDto(id,
                ((LoginUserInfoVo) session.getAttribute("loginUserInfo")).getId()));

        log.info("Todo delete 성공 + Todo id : " + id);
        redirectAttributes.addFlashAttribute("message", "todo를 완료하셨습니다!");
        return "redirect:/student/home";
    }
}
