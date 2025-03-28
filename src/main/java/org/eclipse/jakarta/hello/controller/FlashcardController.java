package org.eclipse.jakarta.hello.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jakarta.hello.model.Category;
import org.eclipse.jakarta.hello.model.Flashcard;
import org.eclipse.jakarta.hello.model.User;
import org.eclipse.jakarta.hello.service.CategoryService;
import org.eclipse.jakarta.hello.service.FlashcardService;
import org.eclipse.jakarta.hello.service.UserService;
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

@WebServlet("/flashcards")
public class FlashcardController extends HttpServlet {

    private TemplateEngine templateEngine;

    @Inject
    private FlashcardService flashcardService;
    @Inject
    private UserService userService;
    @Inject
    private CategoryService categoryService;

    @Override
    public void init() throws ServletException {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);

        if (flashcardService.getAllFlashcards().isEmpty()) {
            User user =  userService.findById(1);
            flashcardService.addFlashcard("Was ist Jakarta EE?", "Eine moderne Java Plattform für Webanwendungen.", user);
            flashcardService.addFlashcard("Was ist Thymeleaf?", "Ein serverseitiges Java Template-Engine-System.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }


        String action = req.getParameter("action");

        if ("add".equals(action)) {
            User user = (User) session.getAttribute("user");
            List<Category> categories = categoryService.getAllCategoriesByUser(user);
            Context context = new Context();
            context.setVariable("categories", categories);
            resp.setContentType("text/html;charset=UTF-8");
            templateEngine.process("add-flashcard", context, resp.getWriter());

        } else if ("edit".equals(action)) {
            User user = (User) session.getAttribute("user");
            Long id = Long.parseLong(req.getParameter("id"));
            Flashcard card = flashcardService.getFlashcardById(id);
            List<Category> categories = categoryService.getAllCategoriesByUser(user);
            Context context = new Context();
            context.setVariable("categories", categories);
            context.setVariable("card", card);
            resp.setContentType("text/html;charset=UTF-8");
            templateEngine.process("edit-flashcard", context, resp.getWriter());

        } else {
            User user = (User) session.getAttribute("user");
            List<Flashcard> cards = flashcardService.getAllFlashcardsByUser(user);
            Context context = new Context();
    List<Category> categories = categoryService.getAllCategoriesByUser(user);
    context.setVariable("categories", categories);

    String categoryIdParam = req.getParameter("categoryId");
    Long selectedCategoryId = null;

    if (categoryIdParam != null && !categoryIdParam.isBlank()) {
        selectedCategoryId = Long.parseLong(categoryIdParam);
        Category selectedCategory = categoryService.getCategoryById(selectedCategoryId);
        cards = flashcardService.getFlashcardsByCategory(selectedCategory);
    } else {
        cards = flashcardService.getAllFlashcardsByUser(user);
    }

    Map<Long, String> nextReviews = new HashMap<>();
    for (Flashcard card : cards) {
        nextReviews.put(card.getId(), flashcardService.timeUntilNextReview(card.getNextReview()));
    }

    context.setVariable("cards", cards);
    context.setVariable("nextReviews", nextReviews);
    context.setVariable("selectedCategoryId", selectedCategoryId);

    resp.setContentType("text/html;charset=UTF-8");
    templateEngine.process("flashcards", context, resp.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }


        String action = req.getParameter("action");

        if ("update".equals(action)) {
            Long id = Long.parseLong(req.getParameter("id"));
            String question = req.getParameter("question");
            String answer = req.getParameter("answer");
            String categoryIdStr = req.getParameter("categoryId");
        
            Flashcard card = flashcardService.getFlashcardById(id);
            if (card != null) {
                card.setQuestion(question);
                card.setAnswer(answer);
        
                if (categoryIdStr != null && !categoryIdStr.isBlank()) {
                    try {
                        Long categoryId = Long.parseLong(categoryIdStr);
                        Category category = categoryService.getCategoryById(categoryId);
        
                        if (category != null && category.getUser().getId().equals(card.getUser().getId())) {
                            card.setCategory(category);
                        } else {
                            card.setCategory(null);
                        }
                    } catch (NumberFormatException e) {
                        card.setCategory(null);
                    }
                } else {
                    card.setCategory(null);
                }
        
                flashcardService.updateFlashcard(card);
            }
        } else if ("delete".equals(action)) {
        Long id = Long.parseLong(req.getParameter("id"));
        flashcardService.deleteFlashcard(id);
    }
    else {
        String question = req.getParameter("question");
        String answer = req.getParameter("answer");
        String categoryIdStr = req.getParameter("categoryId");
    
        if (question != null && answer != null && !question.isBlank() && !answer.isBlank()) {
            User user = (User) session.getAttribute("user");
    
            if (categoryIdStr == null || categoryIdStr.isBlank()) {
                flashcardService.addFlashcard(question, answer, user);
            } else {
                try {
                    Long categoryId = Long.parseLong(categoryIdStr);
                    Category category = categoryService.getCategoryById(categoryId);
    
                    if (category != null && category.getUser().getId().equals(user.getId())) {
                        flashcardService.addFlashcard(question, answer, user, category);
                    } else {
                        flashcardService.addFlashcard(question, answer, user);
                    }
                } catch (NumberFormatException e) {
                    flashcardService.addFlashcard(question, answer, user);
                }
            }
        }
    }
    
    resp.sendRedirect(req.getContextPath() + "/flashcards");
}
}
