package com.example.harto;

/**
 * Represents a user in the Harto application.
 * This model class is used for Firebase data storage and retrieval.
 */
public class user {

    private String username;
    private String userEmail;
    private String userPhone;
    private String userPassword;
    private String guardianName;
    private String guardianPhone;


    public user() {

    }

    public user(String username, String userEmail, String userPhone, String userPassword,
                String guardianName, String guardianPhone) {
        this.username = username;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userPassword = userPassword;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }
}
