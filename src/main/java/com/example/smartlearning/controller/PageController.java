// Đặt tại: src/main/java/com/example/smartlearning/controller/PageController.java
package com.example.smartlearning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String showDashboardPage() {
        return "dashboard";
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
    @GetMapping("/my-subject/{userSubjectId}")
    public String showSubjectPage(@PathVariable Integer userSubjectId) {
        return "my-subject";
    }
    @GetMapping("/quiz/{quizId}")
    public String showQuizPage(@PathVariable Integer quizId) {
        return "quiz";
    }
    @GetMapping("/flashcards/{setId}")
    public String showFlashcardPage(@PathVariable Integer setId) {
        return "flashcards";
    }
    @GetMapping("/profile")
    public String showProfilePage() {
        return "profile";
    }

    @GetMapping("/flashcards/review/due")
    public String showFlashcardReviewPage() {
        // Trả về file "flashcards-review.html" (trang ôn tập bị động)
        return "flashcards-review";
    }

    @GetMapping("/flashcards/review/milestone/{boxNumber}")
    public String showFlashcardMilestoneReviewPage(@PathVariable Integer boxNumber) {
        // Trả về file "flashcards-milestone-review.html" (file mới ở bước 6)
        return "flashcards-milestone-review";
    }
}