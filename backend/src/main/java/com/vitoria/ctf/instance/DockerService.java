package com.vitoria.ctf.instance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class DockerService {

    private static final Pattern NAME = Pattern.compile("^[a-z0-9][a-z0-9_.-]{0,62}$");
    private static final Pattern IMAGE = Pattern.compile("^ctf-[a-z0-9._-]{1,60}:[a-zA-Z0-9._-]{1,30}$");

    private final String bind;
    private final String network;

    public DockerService(@Value("${app.docker.bind}") String bind,
                         @Value("${app.docker.network}") String network) {
        this.bind = bind;
        this.network = network;
    }

    public int start(String name, String image, int containerPort, String flag) {
        if (!NAME.matcher(name).matches() || !IMAGE.matcher(image).matches()) {
            throw new IllegalStateException("nome ou imagem inválidos");
        }

        exec(List.of(
                "docker", "run", "-d", "--rm",
                "--name", name,
                "--network", network,
                "-e", "FLAG=" + flag,
                "-p", bind + "::" + containerPort,
                "--memory", "128m",
                "--cpus", "0.5",
                "--pids-limit", "64",
                "--read-only",
                "--tmpfs", "/tmp",
                "--cap-drop", "ALL",
                "--security-opt", "no-new-privileges",
                image));

        String mapping = exec(List.of("docker", "port", name, containerPort + "/tcp"));
        String first = mapping.lines().findFirst()
                .orElseThrow(() -> new IllegalStateException("porta não encontrada"));
        return Integer.parseInt(first.substring(first.lastIndexOf(':') + 1).trim());
    }

    public void removeQuietly(String name) {
        try {
            if (NAME.matcher(name).matches()) {
                exec(List.of("docker", "rm", "-f", name));
            }
        } catch (RuntimeException ignored) {
        }
    }

    private String exec(List<String> command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IllegalStateException("o docker demorou demais");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (process.exitValue() != 0) {
                throw new IllegalStateException("docker falhou: " + output);
            }
            return output;
        } catch (IOException e) {
            throw new IllegalStateException("não consegui executar o docker", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrompido", e);
        }
    }
}
