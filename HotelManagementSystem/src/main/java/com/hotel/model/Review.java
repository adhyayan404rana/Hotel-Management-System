package com.hotel.model;

/**
 * Review — Guest review/feedback model.
 *
 * @author Grand Vista HMS
 */
public class Review {

    private int reviewId;
    private int bookingId;
    private int customerId;
    private String customerName;
    private String bookingRef;
    private int rating;        // 1–5
    private String title;
    private String comments;
    private String category;
    private String createdAt;

    public Review() {}

    public Review(int bookingId, int customerId, int rating, String title, String comments, String category) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.rating = rating;
        this.title = title;
        this.comments = comments;
        this.category = category;
    }

    /** Returns star emojis representing the rating. */
    public String getStarDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < rating ? "★" : "☆");
        }
        return sb.toString();
    }

    // ---- Getters & Setters ----
    public int getReviewId()                   { return reviewId; }
    public void setReviewId(int id)            { this.reviewId = id; }
    public int getBookingId()                  { return bookingId; }
    public void setBookingId(int id)           { this.bookingId = id; }
    public int getCustomerId()                 { return customerId; }
    public void setCustomerId(int id)          { this.customerId = id; }
    public String getCustomerName()            { return customerName; }
    public void setCustomerName(String n)      { this.customerName = n; }
    public String getBookingRef()              { return bookingRef; }
    public void setBookingRef(String r)        { this.bookingRef = r; }
    public int getRating()                     { return rating; }
    public void setRating(int r)               { this.rating = r; }
    public String getTitle()                   { return title; }
    public void setTitle(String t)             { this.title = t; }
    public String getComments()                { return comments; }
    public void setComments(String c)          { this.comments = c; }
    public String getCategory()                { return category; }
    public void setCategory(String c)          { this.category = c; }
    public String getCreatedAt()               { return createdAt; }
    public void setCreatedAt(String c)         { this.createdAt = c; }
}
