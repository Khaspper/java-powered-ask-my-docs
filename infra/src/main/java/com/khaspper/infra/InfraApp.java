package com.khaspper.infra;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class InfraApp {

    public static void main(String[] args) {
        App app = new App();

        new AskmydocsStack(app, "AskmydocsStack", StackProps.builder()
                .env(Environment.builder()
                        .account("161828268748")
                        .region("us-east-1")
                        .build())
                .build());

        app.synth();
    }
}
