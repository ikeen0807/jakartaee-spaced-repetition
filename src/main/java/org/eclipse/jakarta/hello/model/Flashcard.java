package org.eclipse.jakarta.hello.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = true)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @Column(length = 1000)
    private String question;

    @Column(length = 2000)
    private String answer;
    private double easiness;
    @Column(name = "nextReview")
    @Convert(converter = org.eclipse.jakarta.hello.util.LocalDateAttributeConverter.class)
    private LocalDateTime nextReview;
    private int repetitions;
    private double reviewInterval;

    public Flashcard() {
        this.nextReview = LocalDateTime.now();
    }

    public void getAllByUser(User user) {
    };

    public Flashcard(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.repetitions = 0;
        this.reviewInterval = 1;
        this.easiness = 2.5;
        this.nextReview = LocalDateTime.now();
    }

    public Flashcard(String question, String answer, User user) {
        this.question = question;
        this.answer = answer;
        this.repetitions = 0;
        this.reviewInterval = 1;
        this.easiness = 2.5;
        this.nextReview = LocalDateTime.now();
        this.user = user;
    }

    public Flashcard(String question, String answer, User user, Category category) {
        this.question = question;
        this.answer = answer;
        this.user = user;
        this.category = category;
        this.repetitions = 0;
        this.reviewInterval = 1;
        this.easiness = 2.5;
        this.nextReview = LocalDateTime.now();
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public double getEasiness() { return easiness; }
    public void setEasiness(double easiness) { this.easiness = easiness; }

    public LocalDateTime getNextReview() { return nextReview; }
    public void setNextReview(LocalDateTime nextReview) { this.nextReview = nextReview; }

    public int getRepetitions() { return repetitions; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    public double getReviewInterval() { return reviewInterval; }
    public void setReviewInterval(double reviewInterval) { this.reviewInterval = reviewInterval; }
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
