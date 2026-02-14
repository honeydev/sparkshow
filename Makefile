db-shell:
	PGPASSWORD=test psql -h localhost -p 5445 -U test -d test_db

db-shell-test:
	PGPASSWORD=test psql -h localhost -p 5446 -U test -d test_db

dev-up:
	docker compose up

dev-down:
	docker compose down

format-core:
	sbt "core/scalafmt"

format:
	sbt "core/scalafmt"
	sbt "common/scalafmt"
	sbt "spark/scalafmt"

format-core:
	sbt "core/scalafmt"

format-common:
	sbt "common/scalafmt"

format-spark:
	sbt "spark/scalafmt"

fix-spark:
	sbt "spark/scalafmt"

fix-core:
	sbt "core/scalafix"

fix-common:
	sbt "common/scalafix"

fix:
	sbt "core/scalafix"
	sbt "common/scalafix"
	sbt "spark/scalafmt"

web:
	./launcher :web

create-user:
	./launcher :create-user -cli-command true x --username test --password test --email test@email.com --roles ADMIN


migrate:
	./launcher :migrate
