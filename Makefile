db-shell:
	PGPASSWORD=test psql -h localhost -p 5445 -U test -d test_db

dev-up:
	docker compose up

format:
	sbt scalafmtAll

create-user:
	./launcher :create-user -cli-command true --username test --password test --email test@email.com --roles ADMIN

