package com.vitoria.ctf.instance;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class InstanceCleaner {

    private final ChallengeInstanceRepository instances;
    private final DockerService docker;

    public InstanceCleaner(ChallengeInstanceRepository instances, DockerService docker) {
        this.instances = instances;
        this.docker = docker;
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanExpired() {
        for (ChallengeInstance instance : instances.findByExpiresAtBefore(Instant.now())) {
            docker.removeQuietly(instance.getContainerName());
            instances.delete(instance);
        }
    }
}
