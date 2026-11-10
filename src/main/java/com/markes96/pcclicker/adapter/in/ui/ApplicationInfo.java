package com.markes96.pcclicker.adapter.in.ui;

import java.util.Objects;

/** Facts about the build shown in the About page and the title bar. */
public record ApplicationInfo(String name, String version, String author, String buildDate, String repositoryUrl) {

    public ApplicationInfo {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(buildDate, "buildDate");
        Objects.requireNonNull(repositoryUrl, "repositoryUrl");
    }
}
