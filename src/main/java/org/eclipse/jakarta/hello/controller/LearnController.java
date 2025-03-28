package org.eclipse.jakarta.hello.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jakarta.hello.model.Category;
import org.eclipse.jakarta.hello.model.Flashcard;
import org.eclipse.jakarta.hello.model.User;
import org.eclipse.jakarta.hello.service.CategoryService;
import org.eclipse.jakarta.hello.service.FlashcardService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/learn")
public class LearnController extends HttpServlet {

    @Inject
    private FlashcardService flashcardService;

    private TemplateEngine templateEngine;
    @Inject
    private CategoryService categoryService;

    @Override
    public void init() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
    
        User user = (User) session.getAttribute("user");
        String categoryIdParam = req.getParameter("categoryId");
    
        List<Flashcard> dueCards = List.of();
        List<Long> sessionQueue = (List<Long>) session.getAttribute("sessionQueue");
        List<Category> userCategories = categoryService.getAllCategoriesByUser(user);
    
        Context context = new Context();
        context.setVariable("categories", userCategories);
    
        Long categoryId = null;
        boolean isNoCategory = false;
    
        if ("none".equals(categoryIdParam)) {
            isNoCategory = true;
            categoryId = -1L;
        } else if (categoryIdParam != null && !categoryIdParam.isBlank()) {
            try {
                categoryId = Long.parseLong(categoryIdParam);
            } catch (NumberFormatException e) {
                resp.sendRedirect(req.getContextPath() + "/learn");
                return;
            }
        } else {
            context.setVariable("card", null);
            context.setVariable("showSelectCategory", true);
            resp.setContentType("text/html;charset=UTF-8");
            templateEngine.process("learn", context, resp.getWriter());
            return;
        }
    
        if (!isNoCategory && (categoryService.getCategoryById(categoryId) == null ||
                !categoryService.getCategoryById(categoryId).getUser().getId().equals(user.getId()))) {
            resp.sendRedirect(req.getContextPath() + "/learn");
            return;
        }
    
        context.setVariable("selectedCategoryId", categoryId);
    
        if (sessionQueue == null || sessionQueue.isEmpty() ||
            !categoryId.equals(session.getAttribute("activeCategoryId"))) {
    
            if (isNoCategory) {
                dueCards = flashcardService.getDueFlashcards(user).stream()
                    .filter(card -> card.getCategory() == null)
                    .collect(Collectors.toList());
            } else {
                final Long finalCategoryId = categoryId;
                dueCards = flashcardService.getDueFlashcards(user).stream()
                    .filter(card -> card.getCategory() != null && card.getCategory().getId().equals(finalCategoryId))
                    .collect(Collectors.toList());
            }
    
            sessionQueue = dueCards.stream()
                .map(Flashcard::getId)
                .collect(Collectors.toCollection(LinkedList::new));
    
            session.setAttribute("sessionQueue", sessionQueue);
            session.setAttribute("initialQueue", new ArrayList<>(sessionQueue));
            session.setAttribute("activeCategoryId", categoryId);
        }
    
        if (sessionQueue.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/review-done");
            return;
        }
    
        Long currentCardId = sessionQueue.get(0);
        Flashcard currentCard = flashcardService.getFlashcardById(currentCardId);
    
        List<Long> initialQueue = (List<Long>) session.getAttribute("initialQueue");
        int total = (initialQueue != null) ? initialQueue.size() : sessionQueue.size();
        int remaining = sessionQueue.size();
        int current = total - remaining + 1;
    
        context.setVariable("onlyOneCard", sessionQueue.size() == 1);
        context.setVariable("card", currentCard);
        context.setVariable("current", current);
        context.setVariable("total", total);
        context.setVariable("showSelectCategory", true);
    
        resp.setContentType("text/html;charset=UTF-8");
        templateEngine.process("learn", context, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
    
        Long id = Long.parseLong(req.getParameter("id"));
        String rating = req.getParameter("rating");
        String categoryId = req.getParameter("categoryId");
        String redirectUrl = req.getContextPath() + "/learn";
    
        flashcardService.updateAfterReviewWithRating(id, rating);
    
        List<Long> sessionQueue = (List<Long>) session.getAttribute("sessionQueue");
    
        if (sessionQueue != null) {
            sessionQueue.remove(id);
    
            if ("again".equals(rating)) {
                sessionQueue.add(id);
            }
    
            session.setAttribute("sessionQueue", sessionQueue);
        }
    
        if (sessionQueue == null || sessionQueue.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/review-done");
        } else {
            if (categoryId != null && !categoryId.isBlank()) {
                redirectUrl += "?categoryId=" + categoryId;
            }
            resp.sendRedirect(redirectUrl);
        }
    }
}
