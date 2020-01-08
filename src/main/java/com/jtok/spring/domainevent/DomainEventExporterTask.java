package com.jtok.spring.domainevent;

import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;

public class DomainEventExporterTask implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(DomainEventExporterTask.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private DomainEventRepository repository;
    private GroupMember groupMember;
    private int partition;
    private String role;
    private int totalNumberOfPartitions;

    private boolean isRunning;
    private Context context;
    private long evaluationExpirationMills;
    private int numberOfLocalLeaders;

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public void setRepository(DomainEventRepository repository) {
        this.repository = repository;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setGroupMember(GroupMember groupMember) {
        this.groupMember = groupMember;
    }

    public void setTotalNumberOfPartitions(int totalNumberOfPartitions) {
        this.totalNumberOfPartitions = totalNumberOfPartitions;
    }

    private int numberOfCurrentGroupMembers() {
        return this.groupMember.getCurrentMembers().keySet().size();
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Scheduled(fixedDelay = 2000)
    public void reportCurrentTime() {

        if (this.isRunning) {
            log.info("Exporter partition " + partition + " RUN");

//            this.repository.findAll().forEach( domainEvent -> {
//                log.info("Event " + domainEvent);
//            });

            if (System.currentTimeMillis() > this.evaluationExpirationMills) {
                log.info("Exporter partition " + partition + " evaluation ...");
                calcEvaluationExpirationMills();
                int balancedLimit = (this.totalNumberOfPartitions + this.numberOfCurrentGroupMembers() - 1) / this.numberOfCurrentGroupMembers();
                if (this.numberOfLocalLeaders > balancedLimit) {
                    log.info("Exporter partition " + partition + " YIELD");
                    this.context.yield();
                }
            }
        }

    }

    @Override
    public void start() {
        log.info("Exporter partition " + partition + " STARTED");
        this.isRunning = true;
    }

    @Override
    public void stop() {
        log.info("Exporter partition " + partition + " STOPPED");
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
            calcEvaluationExpirationMills();
        }

        if (event.getRole().startsWith("domainEventExporter")) {
            this.numberOfLocalLeaders++;
            log.info("GrantedEvent Exporter partition " + partition + " numberOfLocalLeaders " + this.numberOfLocalLeaders);
        }
    }

    @EventListener
    void handleOnRevokedEvent(OnRevokedEvent event) {
        if (event.getRole().startsWith("domainEventExporter")) {
            this.numberOfLocalLeaders--;
            log.info("RevokedEvent Exporter partition " + partition + " numberOfLocalLeaders " + this.numberOfLocalLeaders);
        }
    }

    private void calcEvaluationExpirationMills() {
        int i = this.randomNumber(5000, 30000);
        log.info("Exporter partition " + partition + " evaluation in " + (i / 1000) + " secs");
        this.evaluationExpirationMills = System.currentTimeMillis() + i;
    }

    public int randomNumber(int min, int max) {
        int n = (int)(Math.random() * ((max - min) + 1)) + min;
        return n;
    }
}
