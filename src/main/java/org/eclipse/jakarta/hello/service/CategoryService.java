package org.eclipse.jakarta.hello.service;

import java.util.List;

import org.eclipse.jakarta.hello.model.Category;
import org.eclipse.jakarta.hello.model.User;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@Stateless
public class CategoryService {

    @PersistenceContext(unitName = "flashcardPU")
    private EntityManager em;

    @Transactional
    public void addCategory(String name, User user) {
        Category category = new Category(name, user);
        em.persist(category);
    }

    @Transactional
public void deleteCategoryAndFlashcards(Long categoryId) {
    Category category = em.find(Category.class, categoryId);
    if (category != null) {
        em.createQuery("DELETE FROM Flashcard f WHERE f.category = :category")
          .setParameter("category", category)
          .executeUpdate();
        em.remove(category);
    }
}

    public List<Category> getAllCategoriesByUser(User user) {
        TypedQuery<Category> query = em.createQuery(
            "SELECT c FROM Category c WHERE c.user = :user", Category.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public Category getCategoryById(Long id) {
        return em.find(Category.class, id);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = em.find(Category.class, id);
        if (category != null) {
            em.remove(category);
        }
    }
}
