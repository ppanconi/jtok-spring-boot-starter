package com.jtok.spring.publisher;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.apache.curator.utils.CloseableUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.SmartLifecycle;

public class GroupMemberFactoryBean implements FactoryBean<GroupMember>, SmartLifecycle {

    private final Object lifecycleLock = new Object();

    private final GroupMember groupMember;

    /**
     * @see SmartLifecycle
     */
    private boolean autoStartup = true;

    /**
     * @see SmartLifecycle
     */
    private int phase = Integer.MIN_VALUE + 5000;

    /**
     * @see SmartLifecycle
     */
    private volatile boolean running;


    public GroupMemberFactoryBean(CuratorFramework client, String membershipPath, String thisId) {
        this.groupMember = new GroupMember(client, membershipPath, thisId);
    }

    @Override
    public int getPhase() {
        return this.phase;
    }

    /**
     * @param phase the phase
     * @see SmartLifecycle
     */
    public void setPhase(int phase) {
        this.phase = phase;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public boolean isAutoStartup() {
        return this.autoStartup;
    }

    /**
     * @param autoStartup true to automatically start
     * @see SmartLifecycle
     */
    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }


    @Override
    public GroupMember getObject() throws Exception {
        return this.groupMember;
    }

    @Override
    public Class<?> getObjectType() {
        return GroupMember.class;
    }

    @Override
    public void start() {
        synchronized (this.lifecycleLock) {
            if (!this.running) {
                if (this.groupMember != null) {
                    this.groupMember.start();
                }
                this.running = true;
            }
        }
    }

    @Override
    public void stop() {
        synchronized (this.lifecycleLock) {
            if (this.running) {
                CloseableUtils.closeQuietly(this.groupMember);
                this.running = false;
            }
        }
    }

}
