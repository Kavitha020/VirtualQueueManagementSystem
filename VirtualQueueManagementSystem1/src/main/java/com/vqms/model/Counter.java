package com.vqms.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Counter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String counterName;
    private String counterStatus; // "ACTIVE" or "PAUSED"
    private String currentToken;
    private LocalDateTime lastUpdated;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCounterName() {
		return counterName;
	}
	public void setCounterName(String counterName) {
		this.counterName = counterName;
	}
	public String getCounterStatus() {
		return counterStatus;
	}
	public void setCounterStatus(String counterStatus) {
		this.counterStatus = counterStatus;
	}
	public String getCurrentToken() {
		return currentToken;
	}
	public void setCurrentToken(String currentToken) {
		this.currentToken = currentToken;
	}
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
    
    
}
