package com.longdx.forum_backend.config;

import com.github.f4b6a3.tsid.TsidFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Configuration
public class TsidConfig {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String NODE_KEY_PREFIX = "sys:tsid:node:";
    private static final int MAX_NODE_ID = 1024; // 10 bits

    @Bean
    public TsidFactory tsidFactory() {
        int nodeId = allocateNodeId();
        // Log quan trọng để debug xem instance đang chạy node nào
        System.out.println(">>> TSID Initialized with Node ID: " + nodeId);
        
        return TsidFactory.builder()
                .withNode(nodeId)
                .build();
    }

    private int allocateNodeId() {
        for (int i = 0; i < MAX_NODE_ID; i++) {
            String key = NODE_KEY_PREFIX + i;
            // Lock slot này trong 24h. 
            // Nếu app crash, slot sẽ tự nhả sau 24h (hoặc khi restart Redis)
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(key, "LOCKED", Duration.ofHours(24));

            if (Boolean.TRUE.equals(acquired)) {
                keepAlive(key); // Chạy thread ngầm để gia hạn TTL
                return i;
            }
        }
        throw new IllegalStateException("SYSTEM OVERLOAD: No available TSID Node IDs (0-1023).");
    }
    
    /**
     * Logic gia hạn heartbeat để giữ lock trong Redis
     * Chạy background thread để refresh TTL mỗi 12 giờ
     */
    private void keepAlive(String key) {
        Thread keepAliveThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(Duration.ofHours(12).toMillis());
                    redisTemplate.expire(key, Duration.ofHours(24));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        keepAliveThread.setDaemon(true);
        keepAliveThread.setName("TSID-KeepAlive-" + key);
        keepAliveThread.start();
    }
}

