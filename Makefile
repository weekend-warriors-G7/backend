.PHONY: clean, build, up_local, down_local

clean:
	.\mvnw clean || ./mvnw clean

build: clean
	@if [ -f .env ]; then \
    	  export $(shell grep -v '^#' .env | xargs); \
    fi; \
    .\mvnw package || ./mvnw package

docker_build: build
	docker build -t gabjea/weekend-warriors-backend:latest .

up_local: down_local docker_build
	docker run --name backend --env-file .env --dns=8.8.8.8 --dns=8.8.4.4 -d -p 8080:8080 gabjea/weekend-warriors-backend:latest

pull_image:
	docker pull gabjea/weekend-warriors-backend

up_cloud: down_local pull_image
	docker run --name backend --env-file .env --dns=8.8.8.8 --dns=8.8.4.4 -d -p 8080:8080 gabjea/weekend-warriors-backend:latest

docker_stop:
	-docker stop backend || true


down_local: docker_stop
	-docker rm backend || true


