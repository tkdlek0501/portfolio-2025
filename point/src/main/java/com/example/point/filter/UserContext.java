package com.example.point.filter;

public class UserContext {
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userNameHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userNicknameHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userRoleHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userGradeHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> expirationHolder = new ThreadLocal<>();


    public static void set(Long userId, String userName, String userNickname, String userGrade, String role, String expirationHeader) {
        userIdHolder.set(userId);
        userNameHolder.set(userName);
        userNicknameHolder.set(userNickname);
        userGradeHolder.set(userGrade);
        userRoleHolder.set(role);
        expirationHolder.set(expirationHeader);
    }

    public static Long getId() {
        return userIdHolder.get();
    }

    public static String getName() {
        return userNameHolder.get();
    }

    public static String getNickname() {
        return userNicknameHolder.get();
    }

    public static String getRole() {
        return userRoleHolder.get();
    }

    public static String getGrade() {
        return userGradeHolder.get();
    }

    public static String getExpiration() {
        return expirationHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
        userNameHolder.remove();
        userNicknameHolder.remove();
        userGradeHolder.remove();
        userRoleHolder.remove();
        expirationHolder.remove();
    }
}
