package com.jtok.spring.domainevent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Random;

public class DomainEventExporterTask implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(DomainEventExporterTask.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private DomainEventRepository repository;

    private int partition;
    private String role;
    private boolean isRunning;
    private Context context;
    private long expirationMills;

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public void setRepository(DomainEventRepository repository) {
        this.repository = repository;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Scheduled(fixedDelay = 2000)
    public void reportCurrentTime() {

        if (this.isRunning) {
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Exporter " + this + " partition " + partition + " RUN");
//            this.repository.findAll().forEach( domainEvent -> {
//                log.info("Event " + domainEvent);
//            });

            if (System.currentTimeMillis() > this.expirationMills) {
                log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Exporter " + this + " partition " + partition + " YIELD");
                this.context.yield();
            }
        }

    }



    @Override
    public void start() {
//        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Exporter " + this + " partition " + partition + " STARTED");
        this.isRunning = true;
    }

    @Override
    public void stop() {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Exporter " + this + " partition " + partition + " STOPPED");
        this.isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @EventListener
    void handleOnGrantedEvent(OnGrantedEvent event) {
        if (event.getRole().equals(this.role)) {
            this.context = event.getContext();
            int i = this.randomNumber(10000, 300000);
            log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Exporter " + this + " partition " + partition + " expiring in " + (i / 1000) + " secs");
            this.expirationMills = System.currentTimeMillis() + i;
        }
    }

    public int randomNumber(int min, int max) {
        int n = (int)(Math.random() * ((max - min) + 1)) + min;
        return n;
    }
}
