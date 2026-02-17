DOCKER_COMPOSE ?= docker compose
SERVICE_API ?= api

.PHONY: help build test lint run up down stop restart logs ps clean

help:
	@echo "Available targets:"
	@echo "  make build    - Build the project JAR with Maven"
	@echo "  make test     - Run tests with Maven"
	@echo "  make lint     - Run spotless linter with Maven"
	@echo "  make run      - Run API locally with Maven"
	@echo "  make up       - Start API + MySQL with Docker Compose"
	@echo "  make down     - Stop and remove Docker Compose resources"
	@echo "  make stop     - Stop Docker Compose services"
	@echo "  make restart  - Restart Docker Compose services"
	@echo "  make logs     - Stream Docker Compose logs"
	@echo "  make ps       - Show Docker Compose service status"
	@echo "  make clean    - Stop services and remove volumes"

build:
	./mvnw clean package -DskipTests

test:
	./mvnw test

lint:
	./mvnw spotless:apply

run:
	./mvnw spring-boot:run

up:
	$(DOCKER_COMPOSE) up -d

down:
	$(DOCKER_COMPOSE) down

stop:
	$(DOCKER_COMPOSE) stop

restart:
	$(DOCKER_COMPOSE) restart

logs:
	$(DOCKER_COMPOSE) logs -f

ps:
	$(DOCKER_COMPOSE) ps

clean:
	$(DOCKER_COMPOSE) down -v --remove-orphans
