db-shell:
	PGPASSWORD=test psql -h localhost -p 5445 -U test -d test_db

db-shell-test:
	PGPASSWORD=test psql -h localhost -p 5446 -U test -d test_db

dev-up:
	docker compose up

format:
	sbt scalafmtAll

create-user:
	./launcher :create-user -cli-command true x --username test --password test --email test@email.com --roles ADMIN


migrate:
	./launcher :migrate
