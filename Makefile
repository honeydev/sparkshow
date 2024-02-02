db-shell:
	PGPASSWORD=test psql -h localhost -p 5445 -U test -d test_db

dev-up:
	docker compose up

format:
	sbt scalafmtOnly
