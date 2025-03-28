package org.eclipse.jakarta.hello.controller;

import java.io.IOException;
import java.util.List;

import org.eclipse.jakarta.hello.model.Category;
import org.eclipse.jakarta.hello.model.User;
import org.eclipse.jakarta.hello.service.CategoryService;
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

@WebServlet({"/categories", "/categories/delete"})
public class CategoryController extends HttpServlet {

    @Inject
    private CategoryService categoryService;

    private TemplateEngine templateEngine;

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
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        List<Category> categories = categoryService.getAllCategoriesByUser(user);

        Context context = new Context();
        context.setVariable("categories", categories);

        resp.setContentType("text/html;charset=UTF-8");
        templateEngine.process("categories", context, resp.getWriter());
    }

    @Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session == null || session.getAttribute("user") == null) {
        resp.sendRedirect(req.getContextPath() + "/login");
        return;
    }

    String path = req.getServletPath();
    User user = (User) session.getAttribute("user");

    if ("/categories/delete".equals(path)) {
        String categoryIdStr = req.getParameter("id");
        if (categoryIdStr != null) {
            try {
                Long categoryId = Long.parseLong(categoryIdStr);
                Category category = categoryService.getCategoryById(categoryId);
                if (category != null && category.getUser().getId().equals(user.getId())) {
                    categoryService.deleteCategoryAndFlashcards(categoryId);
                }
            } catch (NumberFormatException ignored) {
                //TODO Logging
            }
        }
    } else {
        String name = req.getParameter("name");
        if (name != null && !name.isBlank()) {
            categoryService.addCategory(name, user);
        }
    }

    resp.sendRedirect(req.getContextPath() + "/categories");
}
}
