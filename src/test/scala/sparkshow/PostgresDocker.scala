package sparkshow

import izumi.distage.docker.{ContainerDef, Docker}

object PostgresDocker extends ContainerDef {
    val primaryPort: Docker.DockerPort = Docker.DockerPort.TCP(5432)

    override def config: Config = {
        Config(
          registry = Some("mirror.gcr.io"),
          image    = "postgres:14",
          ports    = Seq(primaryPort),
          env = Map(
            "POSTGRES_USER"     -> "test",
            "POSTGRES_PASSWORD" -> "test",
            "POSTGRES_DB"       -> "test_db",
            "POSTGRES_PORT"     -> "5466"
          )
        )
    }
}
