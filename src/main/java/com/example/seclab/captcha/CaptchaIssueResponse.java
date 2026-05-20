package com.example.seclab.captcha;

public record CaptchaIssueResponse(String token, String challenge) {
}
