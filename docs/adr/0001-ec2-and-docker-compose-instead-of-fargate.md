# Deploy to one EC2 instance with docker compose, not Fargate + RDS

Fargate needs an application load balancer to be reachable, which costs about
$16/month and is never free-tier eligible, and RDS is a second paid service on
top. Since this project exists to practise Java rather than AWS, we deploy the
app and Postgres side by side with docker compose on a single `t4g.micro` EC2
instance — free-tier eligible on a new account, and still fully defined in CDK,
so the infrastructure-as-code goal is met either way.

## Consequences

- No high availability and no automated database backups. Acceptable: this is a
  practice project, and the data can be re-uploaded.
- The VPC has no NAT gateway, because those also cost money. The instance sits
  in a public subnet with a security group instead.
