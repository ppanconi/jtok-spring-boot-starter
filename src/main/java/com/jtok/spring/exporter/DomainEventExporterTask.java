package com.jtok.spring.exporter;

import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.scheduling.annotation.Scheduled;

public class DomainEventExporterTask implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(DomainEventExporterTask.class);

    private GroupMember groupMember;
    private DomainEventExporter exporter;
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

    public void setRole(String role) {
        this.role = role;
    }

    public void setExporter(DomainEventExporter exporter) {
        this.exporter = exporter;
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

        if (isRunning) {
            log.info("Exporter partition " + partition + " RUN");

            exporter.export(partition);

            if (System.currentTimeMillis() > evaluationExpirationMills) {
                log.info("Exporter partition " + partition + " evaluation ...");
                calcEvaluationExpirationMills();
                int balancedLimit = (totalNumberOfPartitions + numberOfCurrentGroupMembers() - 1) / numberOfCurrentGroupMembers();
                if (numberOfLocalLeaders > balancedLimit) {
                    log.info("Exporter partition " + partition + " YIELD");
                    context.yield();
                }
            }
        }

    }

    @Override
    public void start() {
        log.info("Exporter partition " + partition + " STARTED");
        isRunning = true;
    }

    @Override
    public void stop() {
        log.info("Exporter partition " + partition + " STOPPED");
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @EventListener
    void handleOnGrantedEvent(OnGrantedEvent event) {
        if (event.getRole().equals(role)) {
            context = event.getContext();
            calcEvaluationExpirationMills();
        }

        if (event.getRole().startsWith("domainEventExporter")) {
            numberOfLocalLeaders++;
            log.info("GrantedEvent Exporter partition " + partition + " numberOfLocalLeaders " + numberOfLocalLeaders);
        }
    }

    @EventListener
    void handleOnRevokedEvent(OnRevokedEvent event) {
        if (event.getRole().startsWith("domainEventExporter")) {
            numberOfLocalLeaders--;
            log.info("RevokedEvent Exporter partition " + partition + " numberOfLocalLeaders " + numberOfLocalLeaders);
        }
    }

    private void calcEvaluationExpirationMills() {
        int i = randomNumber(5000, 30000);
        log.info("Exporter partition " + partition + " evaluation in " + (i / 1000) + " secs");
        evaluationExpirationMills = System.currentTimeMillis() + i;
    }

    public int randomNumber(int min, int max) {
        int n = (int)(Math.random() * ((max - min) + 1)) + min;
        return n;
    }
}
