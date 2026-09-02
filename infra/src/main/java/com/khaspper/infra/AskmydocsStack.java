package com.khaspper.infra;

import java.util.List;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.AmazonLinuxCpuType;
import software.amazon.awscdk.services.ec2.AmazonLinux2023ImageSsmParameterProps;
import software.amazon.awscdk.services.ec2.Instance;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.MachineImage;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.S3DownloadOptions;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.UserData;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.s3.assets.Asset;
import software.constructs.Construct;

public class AskmydocsStack extends Stack {

    public AskmydocsStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        String geminiKey = System.getenv("GEMINI_API_KEY");
        if (geminiKey == null || geminiKey.isBlank()) {
            throw new IllegalStateException("Set GEMINI_API_KEY before running cdk");
        }

        // Subnet
        Vpc vpc = Vpc.Builder.create(this, "Vpc")
                .maxAzs(1)
                .natGateways(0)
                .subnetConfiguration(List.of(SubnetConfiguration.builder()
                        .name("public")
                        .subnetType(SubnetType.PUBLIC)
                        .build()))
                .build();

        // Port 80
        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "SecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "web");

        // The four files AWS needs
        Asset jar = Asset.Builder.create(this, "Jar")
                .path("../target/askmydocs-0.0.1-SNAPSHOT.jar")
                .build();
        Asset dockerfile = Asset.Builder.create(this, "Dockerfile")
                .path("../Dockerfile")
                .build();
        Asset compose = Asset.Builder.create(this, "Compose")
                .path("../compose.prod.yaml")
                .build();
        Asset initSql = Asset.Builder.create(this, "InitSql")
                .path("../db/enable-pgvector.sql")
                .build();

        UserData userData = UserData.forLinux();
        userData.addCommands(
                "set -eux",
                "dnf install -y docker",
                "systemctl enable --now docker",
                "mkdir -p /usr/libexec/docker/cli-plugins",
                "curl -SL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-aarch64"
                        + " -o /usr/libexec/docker/cli-plugins/docker-compose",
                "chmod +x /usr/libexec/docker/cli-plugins/docker-compose",
                "curl -SL https://github.com/docker/buildx/releases/download/v0.36.1/buildx-v0.36.1.linux-arm64"
                        + " -o /usr/libexec/docker/cli-plugins/docker-buildx",
                "chmod +x /usr/libexec/docker/cli-plugins/docker-buildx",
                "mkdir -p /app/target /app/db");
        userData.addS3DownloadCommand(S3DownloadOptions.builder()
                .bucket(jar.getBucket()).bucketKey(jar.getS3ObjectKey())
                .localFile("/app/target/askmydocs-0.0.1-SNAPSHOT.jar").build());
        userData.addS3DownloadCommand(S3DownloadOptions.builder()
                .bucket(dockerfile.getBucket()).bucketKey(dockerfile.getS3ObjectKey())
                .localFile("/app/Dockerfile").build());
        userData.addS3DownloadCommand(S3DownloadOptions.builder()
                .bucket(compose.getBucket()).bucketKey(compose.getS3ObjectKey())
                .localFile("/app/compose.prod.yaml").build());
        userData.addS3DownloadCommand(S3DownloadOptions.builder()
                .bucket(initSql.getBucket()).bucketKey(initSql.getS3ObjectKey())
                .localFile("/app/db/enable-pgvector.sql").build());
        userData.addCommands(
                "cd /app",
                // compose reads this file on its own and fills in the key
                "echo GEMINI_API_KEY=" + geminiKey + " > /app/.env",
                "docker compose -f compose.prod.yaml up -d --build");

        Instance server = Instance.Builder.create(this, "Server")
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE4_GRAVITON, InstanceSize.MICRO))
                .machineImage(MachineImage.latestAmazonLinux2023(
                        AmazonLinux2023ImageSsmParameterProps.builder()
                                .cpuType(AmazonLinuxCpuType.ARM_64)
                                .build()))
                .securityGroup(securityGroup)
                .userData(userData)
                .userDataCausesReplacement(true)
                .build();

        // Lets the machine read the four files
        jar.grantRead(server.getRole());
        dockerfile.grantRead(server.getRole());
        compose.grantRead(server.getRole());
        initSql.grantRead(server.getRole());
        server.getRole().addManagedPolicy(
                ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore"));

        CfnOutput.Builder.create(this, "Address")
                .value("http://" + server.getInstancePublicIp())
                .build();
    }
}
