package com.vqms.config;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Bean
    public void initializeTwilio() {
        Twilio.init(accountSid, authToken);
    }

    // Optional: Bean to provide Twilio properties if needed elsewhere
    @Bean
    public TwilioProperties twilioProperties() {
        return new TwilioProperties(accountSid, authToken);
    }

    // Inner class for Twilio properties
    public static class TwilioProperties {
        private final String accountSid;
        private final String authToken;

        public TwilioProperties(String accountSid, String authToken) {
            this.accountSid = accountSid;
            this.authToken = authToken;
        }

        public String getAccountSid() {
            return accountSid;
        }

        public String getAuthToken() {
            return authToken;
        }
    }
}
