.PHONY: clean, build, up_local, down_local

clean:
	./mvnw clean || .\mvnw clean

build: clean
	./mvnw package || .\mvnw package

docker_build: build
	docker build -t gabjea/weekend-warriors-backend:latest .

up_local: down_local docker_build
	docker run --name backend -d -p 8080:8080 gabjea/weekend-warriors-backend:latest

docker_stop:
	-docker stop backend || true

down_local: docker_stop
	-docker rm backend || true


